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
import org.apache.commons.collections.*;

public class NestedSubgraph implements NestingTreeNode {
  private List children;
  private VertexSegment leftBorder;
  private VertexSegment rightBorder;
  private int minRank = Integer.MAX_VALUE;
  private int maxRank = Integer.MIN_VALUE;
  private NestedSubgraph parentSubgraph;
  private int vertexCount;
  private double position;
  private Map layerPositions = new HashMap(3);
  private Map layerVertexCounts = new HashMap(3);
  private String label;

//  public NestedSubgraph(Collection vertices) {
//    init(vertices);
//  }

  public NestedSubgraph() {
    children = new ArrayList();
  }

  public NestedSubgraph(int capacity) {
    children = new ArrayList(capacity);
  }

  public boolean contains(LayerVertex vertex) {
    return children.contains(vertex);
  }

  public boolean contains(NestedSubgraph subgraph) {
    return children.contains(subgraph);
  }

  public boolean add(LayerVertex vertex) {
    int rank = vertex.getRank();
    minRank = Math.min(minRank, rank);
    maxRank = Math.max(maxRank, rank);
    vertex.setParentSubgraph(this);
    return children.add(vertex);
  }

  public boolean add(NestedSubgraph subgraph) {
    minRank = Math.min(minRank, subgraph.getMinRank());
    maxRank = Math.max(maxRank, subgraph.getMaxRank());
    subgraph.setParentSubgraph(this);
    return children.add(subgraph);
  }

  public List getChildren() {
    return Collections.unmodifiableList(children);
  }

  public void createBorders() {
    leftBorder = new VertexSegment();
    rightBorder = new VertexSegment();
    LayerVertex leftPredecessor = null;
    LayerVertex rightPredecessor = null;
    for (int i = minRank; i <= maxRank; i++) {
      LayerVertex lv = new BorderVertex();
      leftBorder.add(lv);
      children.add(lv);
      lv.setParentSubgraph(this);
      lv.setRank(i);
      if (leftPredecessor != null) {
        leftPredecessor.getSuccessors().add(lv);
        lv.getPredecessors().add(leftPredecessor);
      }
      leftPredecessor = lv;
      LayerVertex rv = new BorderVertex();
      rightBorder.add(rv);
      children.add(rv);
      rv.setParentSubgraph(this);
      rv.setRank(i);
      if (rightPredecessor != null) {
        rightPredecessor.getSuccessors().add(rv);
        rv.getPredecessors().add(rightPredecessor);
      }
      rightPredecessor = rv;
    }
  }

  public double layerPosition(ArrayList layer) {
    double position = 0;
    int count = 0;
    for (int i = 0; i < layer.size(); i++) {
      if (children.contains(layer.get(i))) {
        position += i;
        ++count;
      }
    }
    position = (count > 0 ? position / count : -1);
    return position;
  }

  public double position(ArrayList[] layers) {
    double position = 0;
    int count = 0;
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].size(); j++) {
        if (children.contains(layers[i].get(j))) {
          position += j;
          ++count;
        }
      }
    }
    position = (count > 0 ? position / count : -1);
    return position;
  }

  public boolean isLeftOf(NestedSubgraph subgraph, ArrayList[] layers) {
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].size() - 1; j++) {
        if (children.contains(layers[i].get(j)) &&
            subgraph.children.contains(layers[i].get(j+1))) return true;
      }
    }
    return false;
  }

  public int getMaxRank() {
    return maxRank;
  }
  public int getMinRank() {
    return minRank;
  }
  public VertexSegment getRightBorder() {
    return rightBorder;
  }
  public VertexSegment getLeftBorder() {
    return leftBorder;
  }

  public LayerVertex getLeftBorderVertex(int rank) {
    return leftBorder.getVertex(rank - minRank);
  }

  public LayerVertex getRightBorderVertex(int rank) {
    return rightBorder.getVertex(rank - minRank);
  }

//  private void init(Collection vertices) {
//    children = new ArrayList(vertices);
//    cache();
//  }
//
//  private void cache() {
//    minRank = Integer.MAX_VALUE;
//    maxRank = Integer.MIN_VALUE;
//    for (Iterator i = children.iterator(); i.hasNext(); ) {
//      LayerVertex v = (LayerVertex)i.next();
//      v.setParentSubgraph(this);
//      int rank = v.getRank();
//      minRank = Math.min(minRank, rank);
//      maxRank = Math.max(maxRank, rank);
//    }
//  }
  public void setParentSubgraph(NestedSubgraph parentSubgraph) {
    this.parentSubgraph = parentSubgraph;
  }
  public NestedSubgraph getParentSubgraph() {
    return parentSubgraph;
  }
  public double getPosition() {
    return position;
  }
  public int countVertices(int rank) {
    int count = 0;
    for (int i = 0; i < children.size(); i++) {
      NestingTreeNode child = (NestingTreeNode)children.get(i);
      count += child.countVertices(rank);
    }
    layerVertexCounts.put(new Integer(rank), new Integer(count));
    return count;
  }
  public double computePosition(int rank) {
    int count = countVertices(rank);
    if (count == 0) return Double.NaN;
    double p = 0;
    for (int i = 0; i < children.size(); i++) {
      NestingTreeNode child = (NestingTreeNode)children.get(i);
      double childPosition = child.computePosition(rank);
      if (Double.isNaN(childPosition)) continue;
      p += child.getVertexCount(rank) * childPosition;
    }
    p /= count;
    layerPositions.put(new Integer(rank), new Double(p));
    return p;
  }
  public int countVertices() {
    vertexCount = 0;
    for (int i = 0; i < children.size(); i++) {
      NestingTreeNode child = (NestingTreeNode)children.get(i);
      vertexCount += child.countVertices();
    }
    return vertexCount;
  }
  public double getPosition(int rank) {
    Double p = (Double)layerPositions.get(new Integer(rank));
    return (p != null ? p.doubleValue() : Double.NaN);
  }
  public int getVertexCount(int rank) {
    Integer count = (Integer)layerVertexCounts.get(new Integer(rank));
    return (count != null ? count.intValue() : 0);
  }
  public int getVertexCount() {
    return vertexCount;
  }
  public double computePosition() {
    int count = countVertices();
    if (count == 0) {
      position = Double.NaN;
      return Double.NaN;
    }
    position = 0;
    for (int i = 0; i < children.size(); i++) {
      NestingTreeNode child = (NestingTreeNode)children.get(i);
      double childPosition = child.computePosition();
      if (Double.isNaN(childPosition)) continue;
      position += child.getVertexCount() * childPosition;
    }
    position /= count;
    return position;
  }

  public int reindex(int rank, int firstIndex) {
    return reindex(
        rank,
        firstIndex,
        new PositionComparator(rank),
        new PositionPredicate(rank));
  }

  public int reindex(int rank,
                     int firstIndex,
                     Comparator comparator,
                     Predicate predicate) {
    List traversalOrder = new ArrayList();
    for (int i = 0; i < children.size(); i++) {
      NestingTreeNode child = (NestingTreeNode)children.get(i);
//      if (!predicate.evaluate(child)) continue;
      if (!child.includesRank(rank)) continue;
      traversalOrder.add(child);
    }
    Collections.sort(traversalOrder, comparator);
    for (Iterator i = traversalOrder.iterator(); i.hasNext(); ) {
      NestingTreeNode child = (NestingTreeNode)i.next();
      firstIndex = child.reindex(rank, firstIndex, comparator, predicate);
      if (child instanceof LayerVertex) {
        LayerVertex v = (LayerVertex)child;
//        System.out.print(v + "{" + v.getRank() + "," + v.getIndexInLayer() + "}, ");
      }
    }
//    System.out.println("");
    return firstIndex;
  }

  public static class PositionComparator implements Comparator {
    private int rank = -1;
    public PositionComparator() {}
    public PositionComparator(int rank) {
      this.rank = rank;
    }
    public int compare(Object o1, Object o2) {
      NestingTreeNode node1 = (NestingTreeNode)o1;
      NestingTreeNode node2 = (NestingTreeNode)o2;
      if (rank < 0)
        return Double.compare(node1.getPosition(), node2.getPosition());
      else
        return Double.compare(node1.getPosition(rank), node2.getPosition(rank));
    }
  }

  public static class PositionPredicate implements Predicate {
    private int rank = -1;
    public PositionPredicate() {}
    public PositionPredicate(int rank) {
      this.rank = rank;
    }
    public boolean evaluate(Object o) {
      NestingTreeNode node = (NestingTreeNode)o;
      if (rank < 0)
        return !Double.isNaN(node.getPosition());
      else
        return !Double.isNaN(node.getPosition(rank));
    }
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public boolean includesRank(int rank) {
    return (minRank <= rank && rank <= maxRank);
  }
}