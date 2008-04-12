/* ====================================================================
 *
 * Copyright(c) 2003, Andriy Shapochka
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the ASHWOOD nor the
 *    names of its contributors may be used to endorse or
 *    promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by
 * individuals on behalf of the ASHWOOD Project and was originally
 * created by Andriy Shapochka.
 *
 */

package org.objectstyle.ashwood.test;

import java.util.*;
import java.io.*;
import org.objectstyle.ashwood.graph.*;

public class TestApp {
  static String[] vertices = {"A", "B", "C", "D", "E", "F", "G", "H", "K"};
  public static void main(String[] args) throws Exception {
    Digraph graph = new MapDigraph(MapDigraph.TREEMAP_FACTORY);
    //loadDigraph(graph, new File("d:/temp/cycle.txt"));
    //loadDigraph(graph, new File("d:/temp/sample_graph.txt"));
    loadDigraph(graph, new File("d:/temp/test_graph.txt"));
    System.out.println("===== Vertices =====");
    for (Iterator i = graph.vertexIterator(); i.hasNext();) {
      System.out.print(i.next() + ", ");
    }
    System.out.println();
    System.out.println("===== Arcs =====");
    for (ArcIterator i = graph.arcIterator(); i.hasNext();) {
      Object arc = i.next();
      Object origin = i.getOrigin();
      Object dst = i.getDestination();
      System.out.print(origin + " -> " + dst + ", ");
    }
    System.out.println();
    System.out.println("===== Incoming Arcs =====");
    for (Iterator j = graph.vertexIterator(); j.hasNext();) {
      Object vertex = j.next();
      System.out.print(vertex + "<-");
      for (ArcIterator i = graph.incomingIterator(vertex); i.hasNext();) {
        Object arc = i.next();
        Object origin = i.getOrigin();
        Object dst = i.getDestination();
        System.out.print(origin + ", ");
      }
      System.out.println();
    }
    System.out.println("===== BFS =====");
    Algorithm bfs = new BreadthFirstSearch(graph, "A");
    while (bfs.hasNext()) {
      System.out.print("->" + bfs.next());
    }
    System.out.println();
    System.out.println("===== DFS 1 =====");
    Algorithm dfs = new DepthFirstSearch(graph, "A");
    while (dfs.hasNext()) {
      System.out.print("->" + dfs.next());
    }
    System.out.println();
    System.out.println("===== Reversed DFS 2 =====");
    DepthFirstStampSearch dfss = new DepthFirstStampSearch(graph, "A");
    while (dfss.hasNext()) {
      System.out.print("->" + dfss.next() + "(" + dfss.getStamp() + ")");
    }
    System.out.println();
    System.out.println("===== DFS 2 Traverse =====");
    dfss.reset("A");
    Map orders = dfss.traverse(new HashMap());
    for (Iterator i = graph.vertexIterator(); i.hasNext();) {
      Object vertex = i.next();
      System.out.print("; " + vertex + orders.get(vertex));
    }
    System.out.println();
    System.out.println("===== Strongly Connected =====");
    System.out.println(GraphUtils.isStronglyConnected(graph));
    System.out.println();
    System.out.println("===== Acyclic =====");
    System.out.println(GraphUtils.isAcyclic(graph));
    System.out.println();
    System.out.println("===== DFS 2 Reversed Topological Sort =====");
    DFSReverseTopologicalSort ts = new DFSReverseTopologicalSort(graph, "A");
    System.out.print("Reversed: ");
    int order = 0;
    while (ts.hasNext())
      System.out.print(ts.next() + "(" + (++order) + "), ");
    System.out.println();

    ts.reset("A");
    System.out.print("Direct: ");
    SortedMap sortedVertices = ts.sort();
    for (Iterator i = sortedVertices.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry)i.next();
      System.out.print(entry.getValue() + "(" + entry.getKey() + "), ");
    }
    System.out.println();
    System.out.println("===== In-degree Topological Sort =====");
    Algorithm ints = new IndegreeTopologicalSort(graph);
    while (ints.hasNext()) {
      Object vertex = ints.next();
      if (vertex == null) {
        System.out.println();
        System.out.println("Cycle found!");
        break;
      }
      System.out.print(vertex + ", ");
    }
    System.out.println();
    System.out.println("===== Strongly Connected Components =====");
    StrongConnection sc = new StrongConnection(graph, CollectionFactory.ARRAYLIST_FACTORY);
    int index = 0;
    while (sc.hasNext()) {
      index++;
      Collection component = (Collection)sc.next();
      System.out.print("comp " + index + ": ");
      for (Iterator i = component.iterator(); i.hasNext();) {
        Object vertex = i.next();
        System.out.print(vertex + ", ");
      }
      System.out.println();
    }
    System.out.println("===== Strongly Connected Component Contraction =====");
    sc = new StrongConnection(graph, CollectionFactory.ARRAYLIST_FACTORY);
    Digraph contractedGraph = new MapDigraph(MapDigraph.HASHMAP_FACTORY);
    sc.contract(contractedGraph);
    System.out.println("===== Contracted Vertices =====");
    for (Iterator i = contractedGraph.vertexIterator(); i.hasNext();) {
      Collection vertex = (Collection)i.next();
      System.out.print('{');
      for (Iterator j = vertex.iterator(); j.hasNext();)
        System.out.print(j.next() + ", ");
      System.out.println("}");
    }
    System.out.println("===== Contracted Arcs =====");
    for (ArcIterator i = contractedGraph.arcIterator(); i.hasNext();) {
      Object arc = i.next();
      Collection origin = (Collection)i.getOrigin();
      Collection dst = (Collection)i.getDestination();
      System.out.print('{');
      if (origin != null) {
        for (Iterator j = origin.iterator(); j.hasNext();)
          System.out.print(j.next() + ", ");
      }
      System.out.print("} -> {");
      if (dst != null) {
        for (Iterator j = dst.iterator(); j.hasNext();)
          System.out.print(j.next() + ", ");
      }
      System.out.println('}');
    }
  }

  static void loadDigraph(Digraph graph, File source) throws Exception {
    Properties ps = new Properties();
    FileInputStream in = new FileInputStream(source);
    ps.load(in);
    in.close();
    for (Iterator i = ps.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry)i.next();
      Object origin = entry.getKey();
      StringTokenizer st = new StringTokenizer((String)entry.getValue(), ";");
      while (st.hasMoreTokens()) {
        graph.putArc(origin, st.nextToken(), Boolean.TRUE);
      }
    }
  }
}
