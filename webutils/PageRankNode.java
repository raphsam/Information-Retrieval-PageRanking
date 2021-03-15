package ir.webutils;

import java.util.*;
import java.io.*;

public class PageRankNode extends Node{
    // specialize node to add rank value
    double rank;

    // modified lists to work with specialized node
    List<PageRankNode> edgesOut = new ArrayList<PageRankNode>();
    List<PageRankNode> edgesIn = new ArrayList<PageRankNode>();

    public PageRankNode(String name){
        super(name);
        rank = 0.0;
    }

    /**
   * Adds an outgoing edge for PageRankNode
   */
  public void addEdge(PageRankNode node) {
    edgesOut.add(node);
    node.addEdgeFrom(this);
  }

  /**
   * Adds an outgoing edge for PageRankNode
   */
    void addEdgeFrom(PageRankNode node) {
        edgesIn.add(node);
      }

    /**
   * Gives the list of outgoing edges of type PageRankNode
   */
  public List<PageRankNode> getEdgesOut(PageRankNode node) {
    return edgesOut;
  }

  /**
   * Gives the list of incoming edges of type PageRankNode
   */
  public List<PageRankNode> getEdgesIn(PageRankNode node) {
    return edgesIn;
  }
}