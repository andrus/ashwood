package org.objectstyle.ashwood.test;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

import com.jgraph.*;
import com.jgraph.graph.*;
import org.objectstyle.ashwood.graph.*;
import org.objectstyle.ashwood.graph.ArcIterator;
import org.objectstyle.ashwood.graph.layout.*;
import org.objectstyle.ashwood.util.*;

public class GraphPainter {
  private String formatName = "png";
  private File imageFile;
  private DefaultGraphModel model = new DefaultGraphModel();
  private JGraph surface = new JGraph(model);
  private DigraphLayout layout;

  public void setFormatName(String formatName) {
    this.formatName = formatName;
  }
  public String getFormatName() {
    return formatName;
  }

  public void setImageFile(File imageFile) {
    this.imageFile = imageFile;
  }
  public File getImageFile() {
    return imageFile;
  }

  public void setLayout(DigraphLayout layout) {
    this.layout = layout;
  }
  public DigraphLayout getLayout() {
    return layout;
  }

  public void paint(JGraph graph) {
    BufferedImage image = new BufferedImage(
        Math.max(graph.getWidth(), 16),
        Math.max(graph.getHeight(), 16),
        BufferedImage.TYPE_INT_RGB);
    Graphics g = image.createGraphics();
    graph.paint(g);
    try {
      ImageIO.write(image, formatName, imageFile);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public void paint(Digraph digraph) {
    Object[] cells = model.getRoots(model);
    cells = model.getDescendants(model, cells).toArray();
    model.remove(cells);
    configure(digraph);
    paint(surface);
  }

  private void configure(Digraph digraph) {
    java.util.List cells = new ArrayList();
    ConnectionSet cs = new ConnectionSet();
    Map attrMap = new Hashtable();
    Map vertexMap = new HashMap(digraph.order());
    Map vertexShapeMap = new HashMap(digraph.order());
    Color borderColor = Color.BLACK;
    Point location = new Point(0, 0);
    DefaultGraphCell stampCell = new DefaultGraphCell();
    VertexView stampView = new VertexView(
        stampCell, surface, surface.getGraphLayoutCache());
    VertexRenderer renderer = (VertexRenderer)stampView.getRenderer();
    Map stampViewAttributes = stampView.getAllAttributes();
    GraphConstants.setBorderColor(stampViewAttributes, borderColor);
    for (Iterator i = digraph.vertexIterator(); i.hasNext(); ) {
      Object vertex = i.next();
      DefaultGraphCell cell = new DefaultGraphCell(vertex);
      cells.add(cell);
      vertexMap.put(vertex, cell);
      //set up attributes
      Map vertexAttributes = GraphConstants.createMap();
      GraphConstants.setBorderColor(vertexAttributes, borderColor);
      stampCell.setUserObject(vertex);
      GraphConstants.setValue(stampViewAttributes, vertex);
      renderer = (VertexRenderer)renderer.getRendererComponent(surface, stampView, false, false, false);
      Dimension cellSize = renderer.getPreferredSize();
      Rectangle bounds = new Rectangle(location, cellSize);
      GraphConstants.setBounds(vertexAttributes, bounds);
      attrMap.put(cell, vertexAttributes);
      vertexShapeMap.put(vertex, bounds);
      location.translate(10, 10);
    }
    Digraph edgeDigraph = new MapDigraph();
    for (ArcIterator i = digraph.arcIterator(); i.hasNext(); ) {
      Object arc = i.next();
      Object origin = i.getOrigin();
      Object dst = i.getDestination();
      DefaultGraphCell srcCell = (DefaultGraphCell)vertexMap.get(origin);
      DefaultGraphCell dstCell = (DefaultGraphCell)vertexMap.get(dst);
      DefaultPort srcPort = new DefaultPort();
      DefaultPort dstPort = new DefaultPort();
      srcCell.add(srcPort);
      dstCell.add(dstPort);
      DefaultEdge edge = new DefaultEdge(arc);
      cs.connect(edge, srcPort, dstPort);
      cells.add(edge);
      edgeDigraph.putArc(origin, dst, edge);
      setEdgeAttributes(attrMap, edge);
    }
    Object[] cellsToInsert = cells.toArray();
    if (layout != null) {
      Attribute vertexShape = new MapAttribute(vertexShapeMap);
      layout.setDigraph(digraph);
      layout.setVertexShape(vertexShape);
      layout.doLayout();
      Rectangle2D areaBounds = layout.getAreaBounds();
      for (Iterator i = vertexMap.values().iterator(); i.hasNext(); ) {
        DefaultGraphCell vertexCell = (DefaultGraphCell)i.next();
        Object userObject = vertexCell.getUserObject();
        Rectangle bounds = (Rectangle)vertexShape.get(userObject);
        bounds = (Rectangle)bounds.clone();
        bounds.translate(-(int)areaBounds.getMinX() + 10, -(int)areaBounds.getMinY() + 10);
        Map vertexAttributes = (Map)attrMap.get(vertexCell);
        GraphConstants.setBounds(vertexAttributes, bounds);
      }
      Digraph arcGeometry = layout.getArcGeometry();
      if (arcGeometry != null && !arcGeometry.isEmpty()) {
        for (ArcIterator i = arcGeometry.arcIterator(); i.hasNext(); ) {
          java.util.List points = (java.util.List)i.next();
          for (int j = 0; j < points.size(); j++) {
            Point p = (Point)points.get(j);
            p.translate(-(int)areaBounds.getMinX() + 10, -(int)areaBounds.getMinY() + 10);
          }
          Object origin = i.getOrigin();
          Object dst = i.getDestination();
          DefaultEdge edge = (DefaultEdge)edgeDigraph.getArc(origin, dst);
          Map edgeAttributes = (Map)attrMap.get(edge);
          GraphConstants.setPoints(edgeAttributes, points);
        }
      }
    }
    //finally draw graph
    model.insert(cellsToInsert, attrMap, cs, null, null);
    Dimension prefSize = surface.getPreferredSize();
    surface.setSize((int)prefSize.getWidth() + 10, (int)prefSize.getHeight() + 10);
  }

  private void setEdgeAttributes(Map attrMap, Object edgeCell) {
    Map edgeAttributes = GraphConstants.createMap();
    GraphConstants.setLineEnd(edgeAttributes, GraphConstants.ARROW_SIMPLE);
    attrMap.put(edgeCell, edgeAttributes);
  }
}