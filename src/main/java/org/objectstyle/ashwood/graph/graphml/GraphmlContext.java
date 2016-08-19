package org.objectstyle.ashwood.graph.graphml;

import java.util.*;

public class GraphmlContext {
  private Set graphSet = new HashSet();
  private Object graph;

  public void setGraph(Object graph) {
    this.graph = graph;
    if (graph != null) graphSet.add(graph);
  }

  public Object getGraph() {
    return graph;
  }

  public Set getGraphSet() {
    return Collections.unmodifiableSet(graphSet);
  }
}