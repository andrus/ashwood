package org.objectstyle.ashwood.graph.graphml;

import org.objectstyle.ashwood.graph.*;
import org.apache.commons.lang.*;

public class StringDigraphFactory implements GraphmlFactory {
  public Object createGraphml(GraphmlContext context) {
    return null;
  }
  public Object createGraph(GraphmlContext context, String id, String edgedefault) {
    Digraph digraph = new MapDigraph(MapDigraph.TREEMAP_FACTORY);
    context.setGraph(digraph);
    return digraph;
  }
  public Object createNode(GraphmlContext context, String id) {
    Digraph digraph = (Digraph)context.getGraph();
    digraph.addVertex(id);
    return id;
  }
  public Object createEdge(GraphmlContext context, String id, String source, String target, String sourceport, String targetport, String directed) {
    Digraph digraph = (Digraph)context.getGraph();
    digraph.putArc(source, target, StringUtils.defaultString(id));
    return id;
  }

  public Object createPort(GraphmlContext context, String name) {
    return null;
  }
  public Object createHyperEdge(GraphmlContext context, String id) {
    return null;
  }
  public Object createEndPoint(GraphmlContext context, String id, String node, String port, String type) {
    return null;
  }
}