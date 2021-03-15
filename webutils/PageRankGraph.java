package ir.webutils;

import java.util.*;
import java.io.*;

public class PageRankGraph {

    // New Map of type PageRankNode
    private Map<String, PageRankNode> nodeMap = new HashMap<String, PageRankNode>();
    Iterator<Map.Entry<String, PageRankNode>> iterator;

    public PageRankGraph(){
        super();
    }

    // New methods with specialized PageRankNode instead of node
    public void addEdge(String xName, String yName) {
        PageRankNode xNode = getNode(xName);
        PageRankNode yNode = getNode(yName);
        xNode.addEdge(yNode);
      }


      public boolean addNode(String name) {
        PageRankNode node = getExistingNode(name);
        if (node == null) {
          node = new PageRankNode(name);
          nodeMap.put(name, node);
          return true;
        } else return false;
      }

      /**
   * Returns the node with that name, creates one if not
   * already present.
   */
  public PageRankNode getNode(String name) {
    PageRankNode node = getExistingNode(name);
    if (node == null) {
      node = new PageRankNode(name);
      nodeMap.put(name, node);
    }
    return node;
  }

  /**
   * Returns the node with that name
   */
  public PageRankNode getExistingNode(String name) {
    return nodeMap.get(name);
  }

  public PageRankNode nextNode() {
    if (iterator == null) {
      throw new IllegalStateException("Graph: Error: Iterator not set.");
    }
    if (iterator.hasNext())
      return iterator.next().getValue();
    else
      return null;
  }

  public void print() {
    PageRankNode node;
    resetIterator();
    while ((node = nextNode()) != null) {
      System.out.println(node + "->" + node.getEdgesOut(node));
    }
  }

    /**
   * Resets the iterator.
   */
  public void resetIterator() {
    iterator = nodeMap.entrySet().iterator();
  }
  
  /**
   * Returns all the nodes of the graph.
   */
  public PageRankNode[] nodeArray() {
    PageRankNode[] nodes = new PageRankNode[nodeMap.size()];
    PageRankNode node;
    int i = 0;
    resetIterator();
    while ((node = nextNode()) != null) {
      nodes[i++] = node;
    }
    return nodes;
  }

}