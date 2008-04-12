package org.objectstyle.ashwood.test;

import java.util.*;
import org.objectstyle.ashwood.graph.*;
import org.apache.commons.collections.*;

public class CycleTest {
  public static void main(String[] args) {
    Digraph graph = new MapDigraph();
    graph.putArc("a", "b", Boolean.TRUE);
    graph.putArc("b", "c", Boolean.TRUE);
    graph.putArc("c", "d", Boolean.TRUE);
    graph.putArc("d", "b", Boolean.TRUE);
    graph.putArc("d", "e", Boolean.TRUE);
    graph.putArc("e", "d", Boolean.TRUE);
    graph.putArc("e", "a", Boolean.TRUE);

    ArrayStack stack = new ArrayStack();
    ArrayStack path = new ArrayStack();
    Set seen = new HashSet();
    List cycles = new ArrayList();
    Iterator vertexIterator = graph.vertexIterator();

    while (vertexIterator.hasNext()) {
      while (vertexIterator.hasNext()) {
        Object vertex = vertexIterator.next();
        if (seen.add(vertex)) {
          stack.push(graph.outgoingIterator(vertex));
          path.push(vertex);
          break;
        }
      }

      while (!stack.isEmpty()) {
        ArcIterator i = (ArcIterator)stack.peek();
        Object origin = i.getOrigin();
        boolean subtreeIsTraversed = true;
        while (i.hasNext()) {
          i.next();
          Object dst = i.getDestination();
          int index = path.indexOf(dst);
          if (index < 0) {
            seen.add(dst);
            stack.push(graph.outgoingIterator(dst));
            path.push(dst);
            subtreeIsTraversed = false;
            break;
          } else {
            cycles.add(new ArrayList(path.subList(index, path.size())));
          }
        }
        if (subtreeIsTraversed) {
          stack.pop();
          path.pop();
        }
      }
    }

    for (Iterator i = cycles.iterator(); i.hasNext(); ) {
      List cycle = (List)i.next();
      System.out.print("cycle: {");
      for (Iterator j = cycle.iterator(); j.hasNext(); ) {
        Object vertex = j.next();
        System.out.print(vertex + ", ");
      }
      System.out.println("}");
    }
  }
}