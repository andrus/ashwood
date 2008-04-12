package org.objectstyle.ashwood.test;

import java.io.*;
import java.util.*;
import org.objectstyle.ashwood.graph.*;
import org.objectstyle.ashwood.graph.graphml.*;
import org.objectstyle.ashwood.graph.layout.*;

public class HierarchyLayoutTest {
  public static void main(String[] args) throws Exception {
    File dir = new File("D:/temp/graph-data/random-dag");
    File dir1 = new File("D:/temp/graph-data/requirements");
    File imageDir = new File("D:/temp/image");
    int vertexCount = 30;
    GraphPainter painter = new GraphPainter();
    GraphmlFactory graphFactory = new StringDigraphFactory();
    GraphmlDataLoader dataLoader = new GraphmlDataLoader();

    double horizontalSpacing = 20;
    double verticalSpacing = 25;
    double verticalAlignment = LayoutConstants.CENTER;

    HierarchyLayout layout = new HierarchyLayout();
    layout.setHorizontalSpacing(horizontalSpacing);
    layout.setVerticalSpacing(verticalSpacing);

    painter.setLayout(layout);

//    for (int i = 1; i <= 10; i++) {
//      Digraph digraph = (Digraph)dataLoader.loadAttTestGraph(dir, vertexCount, i, graphFactory);
//      if (digraph == null) continue;
//      painter.setImageFile(new File(imageDir, "g." + vertexCount + "." + i + ".png"));
//      painter.paint(digraph);
//    }

//    Set digraphSet = dataLoader.loadGraphs(new FileInputStream(new File(dir1, "r.1.graphml")), graphFactory);
//    int index = 0;
//    for (Iterator i = digraphSet.iterator(); i.hasNext(); ) {
//      Digraph digraph = (Digraph)i.next();
//      painter.setImageFile(new File(imageDir, "req." + (index++) + ".png"));
//      painter.paint(new ReversedDigraph(digraph));
//    }

    Digraph reqGraph = new MapDigraph();
    List partition = new ArrayList();
    createSampleData(reqGraph, partition);
    layout.setSubgraphPartition((Set[])partition.toArray(new Set[2]));
    painter.setImageFile(new File(imageDir, "cluster.png"));
    painter.paint(new ReversedDigraph(reqGraph));
  }

  static void createSampleData(Digraph g, List partition) {
    g.putArc("G7.Init Notice", "G7.Admin Review", "");
    g.putArc("G7.Init Notice", "G7.Init Retro", "");
    g.putArc("G7.Init Notice", "G7.Init Retro-1", "");
    g.putArc("G7.Admin Review", "G7.Start App", "");
    g.putArc("G7.Death Cert.", "G7.Init Notice", "");
    g.putArc("G7.Underwriting", "G7.Init Notice", "");
    g.putArc("G7.Start App", "G7.Sign1", "");
    g.putArc("G7.Start App", "G7.Finish App", "");
    g.putArc("G7.Sign1", "G7.Sign2", "");
    g.putArc("G7.Sign2", "G7.Sign3", "");
    g.putArc("G7.Sign3", "G7.Sign4", "");
    g.putArc("G7.Sign4", "G7.Finish App", "");
    g.putArc("G7.Finish App", "G7.Release Pay", "");
    g.putArc("G7.Release Pay", "G7.Pay", "");
    g.putArc("G7.Init Retro", "G7.Req. Pay Retro1", "");
    g.putArc("G7.Init Retro", "G7.Req. Pay Retro2", "");
    g.putArc("G7.Init Retro", "G7.Req. Pay Retro3", "");
    g.putArc("G7.Init Retro-1", "G7.Req. Pay Retro1-1", "");
    g.putArc("G7.Init Retro-1", "G7.Req. Pay Retro2-1", "");
    g.putArc("G7.Init Retro-1", "G7.Req. Pay Retro3-1", "");
    g.putArc("G7.Pay", "G7.Req. Pay Retro1", "");
    g.putArc("G7.Pay", "G7.Req. Pay Retro2", "");
    g.putArc("G7.Pay", "G7.Req. Pay Retro3", "");
    g.putArc("G7.Pay", "G7.Req. Pay Retro1-1", "");
    g.putArc("G7.Pay", "G7.Req. Pay Retro2-1", "");
    g.putArc("G7.Pay", "G7.Req. Pay Retro3-1", "");
    g.putArc("G7.Pay", "G7.Close Detail", "");
    g.putArc("G7.Req. Pay Retro1", "G7.Rec. Pay Retro1", "");
    g.putArc("G7.Req. Pay Retro2", "G7.Rec. Pay Retro2", "");
    g.putArc("G7.Req. Pay Retro3", "G7.Rec. Pay Retro3", "");
    g.putArc("G7.Rec. Pay Retro1", "G7.Close Retro", "");
    g.putArc("G7.Rec. Pay Retro2", "G7.Close Retro", "");
    g.putArc("G7.Rec. Pay Retro3", "G7.Close Retro", "");
    g.putArc("G7.Close Retro", "G7.Close Claim", "");
    g.putArc("G7.Req. Pay Retro1-1", "G7.Rec. Pay Retro1-1", "");
    g.putArc("G7.Req. Pay Retro2-1", "G7.Rec. Pay Retro2-1", "");
    g.putArc("G7.Req. Pay Retro3-1", "G7.Rec. Pay Retro3-1", "");
    g.putArc("G7.Rec. Pay Retro1-1", "G7.Close Retro-1", "");
    g.putArc("G7.Rec. Pay Retro2-1", "G7.Close Retro-1", "");
    g.putArc("G7.Rec. Pay Retro3-1", "G7.Close Retro-1", "");
    g.putArc("G7.Close Retro-1", "G7.Close Claim", "");
    g.putArc("G7.Close Detail", "G7.Close Claim", "");

    Set detail = new HashSet();
    detail.add("G7.Init Notice");
    detail.add("G7.Admin Review");
    detail.add("G7.Start App");
    detail.add("G7.Finish App");
    detail.add("G7.Sign1");
    detail.add("G7.Sign2");
    detail.add("G7.Sign3");
    detail.add("G7.Sign4");
    detail.add("G7.Release Pay");
    detail.add("G7.Pay");
    detail.add("G7.Close Detail");

    Set retro = new HashSet();
    retro.add("G7.Init Retro");
    retro.add("G7.Req. Pay Retro1");
    retro.add("G7.Req. Pay Retro2");
    retro.add("G7.Req. Pay Retro3");
    retro.add("G7.Rec. Pay Retro1");
    retro.add("G7.Rec. Pay Retro2");
    retro.add("G7.Rec. Pay Retro3");
    retro.add("G7.Close Retro");

    Set retro1 = new HashSet();
    retro1.add("G7.Init Retro-1");
    retro1.add("G7.Req. Pay Retro1-1");
    retro1.add("G7.Req. Pay Retro2-1");
    retro1.add("G7.Req. Pay Retro3-1");
    retro1.add("G7.Rec. Pay Retro1-1");
    retro1.add("G7.Rec. Pay Retro2-1");
    retro1.add("G7.Rec. Pay Retro3-1");
    retro1.add("G7.Close Retro-1");

    partition.add(detail);
    partition.add(retro);
    partition.add(retro1);
  }
}