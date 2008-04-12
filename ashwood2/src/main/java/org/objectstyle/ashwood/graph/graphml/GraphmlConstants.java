package org.objectstyle.ashwood.graph.graphml;

public class GraphmlConstants {
  public static final String DATA = "data";
  public static final String DATA_KEY = "key";
  public static final String EDGE = "edge";
  public static final String EDGE_DIRECTED = "directed";
  public static final String EDGE_SOURCE = "source";
  public static final String EDGE_TARGET = "target";
  public static final String EDGEDEFAULT = "edgedefault";
  public static final String EDGEDEFAULT_DIRECTED = "directed";
  public static final String EDGEDEFAULT_UNDIRECTED = "undirected";
  public static final String ENDPOINT = "endpoint";
  public static final int ENDPOINT_IN = 1;
  public static final String ENDPOINT_NODE = "node";
  public static final int ENDPOINT_OUT = 2;
  public static final String ENDPOINT_PORT = "port";
  public static final String ENDPOINT_TYPE = "type";
  public static final String ENDPOINT_TYPE_IN = "in";
  public static final String ENDPOINT_TYPE_OUT = "out";
  public static final String ENDPOINT_TYPE_UNDIR = "undir";
  public static final int ENDPOINT_UNDIR = 0;
  public static final String FOR = "for";
  public static final String GRAPH = "graph";
  public static final String GRAPHML = "graphml";
  public static final String HYPEREDGE = "hyperedge";
  public static final String ID = "id";
  public static final String KEY = "key";
  public static final String NODE = "node";
  public static final String PORT = "port";
  public static final int SCOPE_ALL = 0;
  public static final int SCOPE_EDGE = 2;
  public static final int SCOPE_ENDPOINT = 5;
  public static final int SCOPE_GRAPH = 3;
  public static final int SCOPE_HYPEREDGE = 4;
  public static final int SCOPE_NODE = 1;
  public static final int SCOPE_PORT = 6;
  public static final String SCOPE_TYPE_ALL = "all";
  public static final String SCOPE_TYPE_EDGE = "edge";
  public static final String SCOPE_TYPE_ENDPOINT = "endpoint";
  public static final String SCOPE_TYPE_GRAPH = "graph";
  public static final String SCOPE_TYPE_HYPEREDGE = "hyperedge";
  public static final String SCOPE_TYPE_NODE = "node";
  public static final String SCOPE_TYPE_PORT = "port";
  public static final String SOURCEPORT = "sourceport";
  public static final String TARGETPORT = "targetport";

  private GraphmlConstants() {}

  public static boolean isGraphml(String element) {
    return GRAPHML.equalsIgnoreCase(element);
  }

  public static boolean isGraph(String element) {
    return GRAPH.equalsIgnoreCase(element);
  }

  public static boolean isNode(String element) {
    return NODE.equalsIgnoreCase(element);
  }

  public static boolean isEdge(String element) {
    return EDGE.equalsIgnoreCase(element);
  }
}