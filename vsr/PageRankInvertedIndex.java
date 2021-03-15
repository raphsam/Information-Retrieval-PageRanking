package ir.vsr;

import java.io.*;
import java.util.*;
import java.lang.*;

import ir.utilities.*;
import ir.classifiers.*;

public class PageRankInvertedIndex extends InvertedIndex{
    // Map from page to spidered value
    static HashMap<String, Double> pageRank;

    // input weight
    double weight;

    // page_ranks.txt file
    static File pageRanks;

    // constructor
    public PageRankInvertedIndex(File dirFile, short docType, boolean stem, boolean feedback, double w) {
        super(dirFile, docType, stem, feedback);
        pageRank = new HashMap<String, Double>();
        weight = w;
        getData();
    }

    protected void indexDocuments(){
        if (!tokenHash.isEmpty() || !docRefs.isEmpty()) {
            // Currently can only index one set of documents when an index is created
            throw new IllegalStateException("Cannot indexDocuments more than once in the same InvertedIndex");
          }
          // Get an iterator for the documents
          DocumentIterator docIter = new DocumentIterator(dirFile, docType, stem);
          System.out.println("Indexing documents in " + dirFile);
          String name, type;

          // Loop, processing each of the documents
          while (docIter.hasMoreDocuments()) {
            FileDocument doc = docIter.nextDocument();
            // Create a document vector for this document
            // get name of file and type
            name = doc.file.getName();
            type = name.substring(name.length()-5);

            // found the page_ranks.txt file
            if (name.equals("page_ranks.txt"))
                pageRanks = doc.file;

            // file to index
            if (type.equals(".html")){
                System.out.print(doc.file.getName() + ",");
                HashMapVector vector = doc.hashMapVector();
                indexDocument(doc, vector);
            }
          }
          // Now that all documents have been processed, we can calculate the IDF weights for
          // all tokens and the resulting lengths of all weighted document vectors.
          computeIDFandDocumentLengths();
          System.out.println("\nIndexed " + docRefs.size() + " documents with " + size() + " unique terms.");
    }

     // Get info from page_ranks.txt and put into map
    public static void getData () {
        Scanner sc = null;
        try {
        sc = new Scanner(pageRanks);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        // length of 2: name and score
        String[] input = new String[2];
        
        // read line, split, put into array, inset into map
        while (sc.hasNextLine()){
            String line = sc.nextLine();
            input = line.split(" ");
            pageRank.put(input[0], Double.parseDouble(input[1]));
        }
    }

    protected Retrieval getRetrieval(double queryLength, DocumentReference docRef, double score) {
        // Normalize score for the lengths of the two document vectors
        score = score / (queryLength * docRef.length);
        double rank = pageRank.get(docRef.file.getName());
        // Apply the spidered rank value into score
        score += rank*weight;
        // Add a Retrieval for this document to the result array
        return new Retrieval(docRef, score);
      }

    public static void main(String[] args) {
            // Parse the arguments into a directory name and optional flag
    double weight = 0;

    String dirName = args[args.length - 1];
    short docType = DocumentIterator.TYPE_TEXT;
    boolean stem = false, feedback = false;
    for (int i = 0; i < args.length - 1; i++) {
      String flag = args[i];
      if (flag.equals("-html"))
        // Create HTMLFileDocuments to filter HTML tags
        docType = DocumentIterator.TYPE_HTML;
      else if (flag.equals("-stem"))
        // Stem tokens with Porter stemmer
        stem = true;
      else if (flag.equals("-feedback"))
        // Use relevance feedback
        feedback = true;
        // Weight to use when using spidered rank
      else if (flag.equals("-weight")){
          weight = Double.parseDouble(args[i+1]);
          i++;
      }
      else {
        throw new IllegalArgumentException("Unknown flag: "+ flag);
      }
    }


    // Create an inverted index for the files in the given directory.
    PageRankInvertedIndex index = new PageRankInvertedIndex(new File(dirName), docType, stem, feedback, weight);
    // index.print();
    // Interactively process queries to this index.
    index.processQueries();
    }
}