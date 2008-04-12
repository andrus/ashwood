package org.objectstyle.ashwood.test;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.objectstyle.ashwood.graph.*;
import org.objectstyle.ashwood.graph.graphml.*;
import org.objectstyle.ashwood.graph.layout.*;
import org.xml.sax.*;

public class GraphmlDataLoader {
  private SAXParserFactory parserFactory = SAXParserFactory.newInstance();

  public GraphmlDataLoader() {
    parserFactory.setValidating(false);
  }

  public Set loadGraphs(InputStream in, GraphmlFactory factory)
      throws IOException, SAXException, ParserConfigurationException {
    GraphmlContext context = new GraphmlContext();
    GraphmlHandler handler = new GraphmlHandler(context, factory);
    SAXParser parser = parserFactory.newSAXParser();
    parser.parse(in, handler);
    return context.getGraphSet();
  }

  public Object loadAttTestGraph(File directory,
                                 int vertexCount,
                                 int graphIndex,
                                 GraphmlFactory factory)
      throws IOException, SAXException, ParserConfigurationException {
    File graphmlFile = new File(
        directory, "g." + vertexCount + "." + graphIndex + ".graphml");
    if (!graphmlFile.isFile()) return null;
    FileInputStream fin = new FileInputStream(graphmlFile);
    BufferedInputStream in = new BufferedInputStream(fin);
    Set graphSet = loadGraphs(in, factory);
    in.close();
    fin.close();
    return (!graphSet.isEmpty() ? graphSet.iterator().next() : null);
  }

  public static void main(String[] argv) throws Exception {
    File dir = new File("D:/temp/graph-data/random-dag");
    File imageDir = new File("D:/temp/image");
    int vertexCount = 40;
    GraphPainter painter = new GraphPainter();
    GraphmlFactory graphFactory = new StringDigraphFactory();
    GraphmlDataLoader dataLoader = new GraphmlDataLoader();

    double horisontalSpacing = 40;
    double verticalSpacing = 10;
    int adjustingPassCount = 20;
    double movePrecision = 5;
    double stepSize = 5;
    boolean rootsUpwards = true;
    SugiyamaLayout layout = new SugiyamaLayout();
    layout.setAdjustingPassCount(adjustingPassCount);
    layout.setHorizontalSpacing(horisontalSpacing);
    layout.setVerticalSpacing(verticalSpacing);
    layout.setMovePrecision(movePrecision);
    layout.setStepSize(stepSize);
    layout.setRootsUpwards(rootsUpwards);

    painter.setLayout(layout);

    for (int i = 1; i <= 10; i++) {
      Digraph digraph = (Digraph)dataLoader.loadAttTestGraph(dir, vertexCount, i, graphFactory);
      if (digraph == null) continue;
      painter.setImageFile(new File(imageDir, "g." + vertexCount + "." + i + ".png"));
      painter.paint(digraph);
    }
  }

}