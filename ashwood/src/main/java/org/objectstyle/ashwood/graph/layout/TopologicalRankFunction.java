package org.objectstyle.ashwood.graph.layout;

import java.util.*;
import org.objectstyle.ashwood.graph.*;
import org.objectstyle.ashwood.util.*;

public class TopologicalRankFunction implements RankFunction {
  private int maxRank = -1;
  private Map vertexRankMap;

  public TopologicalRankFunction(Digraph digraph) {
    init(digraph);
  }

  private void init(Digraph digraph) {
    vertexRankMap = new HashMap(digraph.order());
    IndegreeTopologicalSort traversal = new IndegreeTopologicalSort(digraph);
    while (traversal.hasNext()) {
      Object vertex = traversal.next();
      int rank = -1;
      for (ArcIterator i = digraph.incomingIterator(vertex); i.hasNext(); ) {
        i.next();
        Object predecessor = i.getOrigin();
        int predRank = intValue(predecessor);
        if (predRank < 0)
          throw new ArithmeticException("Ranking failed.");
        rank = Math.max(rank, predRank);
      }
      rank++;
      assignRank(vertex, rank);
    }
  }

  public void assignRank(Object vertex, int rank) {
    vertexRankMap.put(vertex, new Integer(rank));
    maxRank = Math.max(maxRank, rank);
  }

  public int maxRank() {
    return maxRank;
  }

  public int intValue(Object vertex) {
    Number rank = (Number)vertexRankMap.get(vertex);
    return (rank != null ? rank.intValue() : Integer.MIN_VALUE);
  }
}