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
package org.objectstyle.ashwood.graph.layout;

import java.util.*;
import org.objectstyle.ashwood.util.*;
import org.objectstyle.ashwood.graph.*;

public class SubgraphPartition {
  private NestedSubgraph root;

  public SubgraphPartition() {
  }

  public void setPartition(NestedSubgraph root) {
    this.root = root;
  }

  public void groupVertices(Layer layer) {
    root.computePosition(layer.getRank());
    root.reindex(layer.getRank(), 0);
    layer.sort();
  }

  public void untwineSubgraphs(Layer[] layers) {
    root.computePosition();
    //form subgraph ordering graph
    Digraph subgraphOrderingGraph = new MapDigraph();
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].size() - 1; j++) {
        LayerVertex v1 = layers[i].getVertex(j);
        LayerVertex v2 = layers[i].getVertex(j + 1);
        subgraphOrderingGraph.putArc(v1, v2, Boolean.TRUE);
        NestingTreeNode origin = v1.getParentSubgraph();
        NestingTreeNode dst = v2.getParentSubgraph();
        subgraphOrderingGraph.addVertex(origin);
        subgraphOrderingGraph.addVertex(dst);
        if (origin == dst) continue;
        if (origin == root) origin = v1;
        if (dst == root) dst = v2;
        subgraphOrderingGraph.putArc(origin, dst, Boolean.TRUE);
      }
    }

    //break cycles in subgraph ordering graph:
    //make it dag for the topological sorting op
    List cycles = GraphUtils.findCycles(subgraphOrderingGraph);
    for (Iterator i = cycles.iterator(); i.hasNext(); ) {
      ArrayList cycle = (ArrayList)i.next();
      int minWeightIndex = -1;
      double minWeight = Double.POSITIVE_INFINITY;
      for (int j = 0; j < cycle.size(); j++) {
        double weight = ((NestingTreeNode)cycle.get(j)).getPosition();
        if (weight < minWeight) {
          minWeightIndex = j;
          minWeight = weight;
        }
      }
      if (minWeightIndex > 0) {
        subgraphOrderingGraph.removeArc(
            cycle.get(minWeightIndex - 1), cycle.get(minWeightIndex));
      } else {
        subgraphOrderingGraph.removeArc(
            cycle.get(cycle.size() - 1), cycle.get(0));
      }
    }

    //sort ubgraph ordering topologically
    IndegreeTopologicalSort sorter =
        new IndegreeTopologicalSort(subgraphOrderingGraph);
    Map indexMap = new HashMap();
    int sortIndex = 0;
    while (sorter.hasNext()) {
      indexMap.put(sorter.next(), new Integer(sortIndex++));
    }
    //reorder sequences of vertices in layers
    Comparator comparator = new IndexComparator(indexMap);
    for (int i = 0; i < layers.length; i++) {
      root.reindex(i, 0, comparator, new NestedSubgraph.PositionPredicate(i));
      layers[i].sort();
    }
  }

  public void insertBorderSegments(Layer[] layers) {
    List children = root.getChildren();
    for (int i = 0; i < children.size(); i++) {
      Object child = children.get(i);
      if (child instanceof NestedSubgraph)
        ((NestedSubgraph)child).createBorders();
    }

    for (int i = 0; i < layers.length; i++) {
      Layer layer = layers[i];
      int size = layer.size();
      for (int j = 0; j < size; j++) {
        LayerVertex v = layer.getVertex(j);
        NestedSubgraph subgraph = v.getParentSubgraph();
        if (subgraph == root) continue;

        LayerVertex leftNeighbor = layer.getLeft(v);
        LayerVertex rightNeighbor = layer.getRight(v);
        NestedSubgraph leftNeighborSubgraph = (leftNeighbor != null ?
            leftNeighbor.getParentSubgraph() :
            null);
        NestedSubgraph rightNeighborSubgraph = (rightNeighbor != null ?
            rightNeighbor.getParentSubgraph() :
            null);

        boolean borderAdded = false;
        if (subgraph != leftNeighborSubgraph) {
          LayerVertex border = subgraph.getLeftBorderVertex(i);
          layer.add(j, border);
          j++;
          size++;
          borderAdded = true;
        }
        if (subgraph != rightNeighborSubgraph) {
          LayerVertex border = subgraph.getRightBorderVertex(i);
          j++;
          size++;
          layer.add(j, border);
          borderAdded = true;
        }
        if (borderAdded)
          layer.refreshIndices();
      }
      layer.refreshIndices();
    }
  }
}