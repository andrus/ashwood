package org.objectstyle.ashwood.graph.layout;

import org.objectstyle.ashwood.function.IntFunction;

public interface RankFunction extends IntFunction {
  int maxRank();
  void assignRank(Object vertex, int rank);
}