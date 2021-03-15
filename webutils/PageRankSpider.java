package ir.webutils;

import java.util.*;
import java.io.*;
import java.text.*;

import ir.utilities.*;
import java.math.*;

/**
 * Spider defines a framework for writing a web crawler.  Users can
 * change the behavior of the spider by overriding methods.
 * Default spider does a breadth first crawl starting from a
 * given URL up to a specified maximum number of pages, saving (caching)
 * the pages in a given directory.  Also adds a "BASE" HTML command to
 * cached pages so links can be followed from the cached version.
 *
 * @author Ted Wild and Ray Mooney
 */

public class PageRankSpider extends Spider{
    public double alpha = 0.15;
    public int iter = 50;
    
    // Page Rank Graph
    public PageRankGraph PRGraph;

    // Map page number to a link
    public HashMap<String, Link> PRMap;
    
    // Store pages already indexed
    public HashSet<HTMLPage> indexedPages;

    //Store links already indexed
    public HashSet<Link> indexedLinks;

    // Constructor
    public PageRankSpider () {
		PRGraph = new PageRankGraph();
		PRMap = new HashMap<String, Link>();
		indexedPages = new HashSet<HTMLPage>();
    indexedLinks = new HashSet<Link>();
    }
    
      /**
   * Checks command line arguments and performs the crawl. This
   * implementation calls processArgs, doCrawl, and calculates Page
   * Rank with c
   *
   * @param args Command line arguments.
   */
  public void go(String[] args) {
    processArgs(args);
    doCrawl();
    createGraph();
    doPageRank();
    outputPR();
    PRGraph.print();
  }

  public void doCrawl(){
    if (linksToVisit.size() == 0) {
        System.err.println("Exiting: No pages to visit.");
        System.exit(0);
      }
      visited = new HashSet<Link>();
      while (linksToVisit.size() > 0 && count < maxCount) {
        // Pause if in slow mode
        if (slow) {
          synchronized (this) {
            try {
              wait(1000);
            }
            catch (InterruptedException e) {
            }
          }
        }
        // Take the top link off the queue
        Link link = linksToVisit.remove(0);
        link.cleanURL(); // Standardize and clean the URL for the link
        System.out.println("Trying: " + link);
        // Skip if already visited this page
        if (!visited.add(link)) {
          System.out.println("Already visited");
          continue;
        }
        if (!linkToHTMLPage(link)) {
          System.out.println("Not HTML Page");
          continue;
        }
        HTMLPage currentPage = null;
        // Use the page retriever to get the page
        try {
          currentPage = retriever.getHTMLPage(link);
        }
        catch (PathDisallowedException e) {
          System.out.println(e);
          continue;
        }
        if (currentPage.empty()) {
          System.out.println("No Page Found");
          continue;
        }
        if (currentPage.indexAllowed()) {
          count++;
          System.out.println("Indexing" + "(" + count + "): " + link);
          indexPage(currentPage);
          // When indexing, also keeps track of page and link for making the graph
          indexedPages.add(currentPage);
          indexedLinks.add(currentPage.getLink());
        }
        if (count < maxCount) {
          List<Link> newLinks = getNewLinks(currentPage);
          // System.out.println("Adding the following links" + newLinks);
          // Add new links to end of queue
          linksToVisit.addAll(newLinks);
        }
      }
      
  }

  public void createGraph() {
    // iterates through all indexed pages
    for (HTMLPage page: indexedPages){
        // iterates through outlinks on page
        List<Link> outLinks = getNewLinks(page);
        for (Link outLink: outLinks){
            // if link is not a self link and is indexed, then add edge
            if (!(page.getLink()).equals(outLink) && indexedLinks.contains(outLink)){
              PRGraph.addEdge(page.getLink().toString(),outLink.toString());
            }
        }
    }
  }

  protected void indexPage(HTMLPage page) {
    // index page for page_ranks.txt
    String ID = "P" + MoreString.padWithZeros(count, (int) Math.floor(MoreMath.log(maxCount, 10)) + 1);
    PRMap.put(ID + ".html", page.getLink());
    page.write(saveDir, ID);
  }
  
  public void doPageRank(){
    PageRankNode[] nodes = PRGraph.nodeArray();
    int size = nodes.length;
    double sum = 0.0;
    double c = 1.0;
    double e = 1.0*alpha/size;

    // initiliazes node ranks
    for (int x = 0; x < size; x++){
        nodes[x].rank = 1.0/size;
    }

    // gets new rank
    for (int y = 0; y < iter; y++){
        double[] updatedRanks = new double [size];
        for (int z = 0; z < size; z++){
            sum = 0.0;
            if (nodes[z].edgesIn.size() > 0)
                for (PageRankNode in: nodes[z].edgesIn)
                    sum += in.rank/in.edgesOut.size();
            updatedRanks[z] = (1-alpha)*sum + e;
        }

        // normalizes rank
        sum = 0;
        for (int a = 0; a < size; a++)
            sum += updatedRanks[a];
        c = 1.0/sum;

        for (int b = 0; b < size; b++)
            nodes[b].rank = c*updatedRanks[b]; 

    }
  }

  public void outputPR(){
    PageRankNode[] nodes = PRGraph.nodeArray();
    try{
      PrintWriter write = new PrintWriter(new FileWriter(new File(saveDir, "page_ranks.txt")));
      SortedSet<String> keys = new TreeSet<String>(PRMap.keySet());
      for (String key: keys) {
        // iterate through keys and output to page_ranks.txt
          PageRankNode node = PRGraph.getNode(PRMap.get(key).toString());
          String decimalOutput = new DecimalFormat("#.##########").format(new BigDecimal(node.rank));
          write.print(key + " " + decimalOutput + "\n");
      }
      write.close();
    }
    catch (IOException exception){
      System.err.println("HTMLPage.write(): " + exception);
    }
  }
  public static void main(String args[]) {
    new PageRankSpider().go(args);
  }

}