package org.objectstyle.ashwood.graph.graphml;

public interface GraphmlFactory {
  Object createGraphml(GraphmlContext context);
  Object createGraph(GraphmlContext context, String id, String edgedefault);
  Object createNode(GraphmlContext context, String id);
  Object createEdge(GraphmlContext context,
                    String id,
                    String source,
                    String target,
                    String sourceport,
                    String targetport,
                    String directed);
  Object createPort(GraphmlContext context, String name);
  Object createHyperEdge(GraphmlContext context, String id);
  Object createEndPoint(GraphmlContext context,
                        String id,
                        String node,
                        String port,
                        String type);
}