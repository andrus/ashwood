package org.objectstyle.ashwood.graph.graphml;

import org.xml.sax.helpers.*;
import org.xml.sax.*;


public class GraphmlHandler extends DefaultHandler {
  private GraphmlContext context;
  private GraphmlFactory factory;

  public GraphmlHandler(GraphmlContext context, GraphmlFactory factory) {
    if (context == null) throw new NullPointerException("Context is null");
    if (factory == null) throw new NullPointerException("Factory is null");
    this.context = context;
    this.factory = factory;
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws org.xml.sax.SAXException {
    String element = qName;
    if (GraphmlConstants.isGraphml(element)) {
      factory.createGraphml(context);
    } else if (GraphmlConstants.isGraph(element)) {
      String id = attributes.getValue(GraphmlConstants.ID);
      String edgedefault = attributes.getValue(GraphmlConstants.EDGEDEFAULT);
      factory.createGraph(context, id, edgedefault);
    } else if (GraphmlConstants.isNode(element)) {
      String id = attributes.getValue(GraphmlConstants.ID);
      factory.createNode(context, id);
    } else if (GraphmlConstants.isEdge(element)) {
      String id = attributes.getValue(GraphmlConstants.ID);
      String source = attributes.getValue(GraphmlConstants.EDGE_SOURCE);
      String target = attributes.getValue(GraphmlConstants.EDGE_TARGET);
      String sourcePort = attributes.getValue(GraphmlConstants.SOURCEPORT);
      String targetPort = attributes.getValue(GraphmlConstants.TARGETPORT);
      String directed = attributes.getValue(GraphmlConstants.EDGE_DIRECTED);
      factory.createEdge(context, id, source, target, sourcePort, targetPort, directed);
    }
  }
  public void endElement(String uri, String localName, String qName) throws org.xml.sax.SAXException {
  }

  public InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException {
    InputSource local = new InputSource(GraphmlHandler.class.getResourceAsStream("graphml.dtd"));
    return local;
  }

}