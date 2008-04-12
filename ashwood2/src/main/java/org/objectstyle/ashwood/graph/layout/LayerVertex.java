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
import java.awt.geom.*;
import org.objectstyle.ashwood.util.*;
import org.objectstyle.ashwood.function.*;
import org.apache.commons.collections.*;

public class LayerVertex implements Comparable, NestingTreeNode {
  public static final Comparator LAYER_INDEX_COMPARATOR = new LayerIndexComparator();
  public static final DoubleFunction POSITION_IN_LAYER = new PositionInLayer();

  private int rank;
  private int indexInLayer;
  private double weight;
  private double centerX;
  private double centerY;
  private double width;
  private double height;
  private ArrayList predecessors;
  private ArrayList successors;
  private Object userVertex;
  private NestedSubgraph parentSubgraph;
  private double leftX = Double.NaN;
  private double rightX = Double.NaN;
  private int classIndex = -1;

  public LayerVertex(
      Object userVertex,
      int predCapacity,
      int succCapacity) {
    this.userVertex = userVertex;
    predecessors = new ArrayList(Math.max(predCapacity, 1));
    successors = new ArrayList(Math.max(succCapacity, 1));
  }

  public int[] getSuccessorIndices() {
    int succSize = successors.size();
    int[] succIndices = new int[succSize];
    for (int j = 0; j < succSize; j++) {
      succIndices[j] = ((LayerVertex)successors.get(j)).indexInLayer;
    }
    Arrays.sort(succIndices);
    return succIndices;
  }

  public int compareTo(Object o) {
    return Double.compare(weight, ((LayerVertex)o).weight);
  }

  public double rubberForce() {
    double forceValue = 0;
    int predSize = predecessors.size();
    int succSize = successors.size();
    if (predSize + succSize == 0) return 0;
    forceValue = deflection() / (predSize + succSize);
    return forceValue;
  }

  public double predecessorPendulumForce() {
    return pendulumForce(predecessors);
  }

  public double successorPendulumForce() {
    return pendulumForce(successors);
  }

  public double predecessorDeflection() {
    return deflection(predecessors);
  }

  public double successorDeflection() {
    return deflection(successors);
  }

  public double deflection() {
    return deflection(predecessors) + deflection(successors);
  }

  public double successorMedianValue() {
    return medianValue(successors);
  }

  public double predecessorMedianValue() {
    return medianValue(predecessors);
  }

  public double distanceToLeft(LayerVertex leftNeighbor) {
    if (leftNeighbor == null) return Double.POSITIVE_INFINITY;
    return (getMinX() - leftNeighbor.getMaxX());
  }

  public boolean isTouchingToLeft(LayerVertex leftNeighbor, double minDistance) {
    return distanceToLeft(leftNeighbor) <= minDistance;
  }

  public double minCenterDistance(LayerVertex vertex, double spacing) {
    return (width + vertex.width) / 2 + spacing;
  }

  public double getMaxX() {
    return (centerX + width / 2);
  }

  public double getMinX() {
    return (centerX - width / 2);
  }

  public double getMaxY() {
    return (centerY + height / 2);
  }

  public double getMinY() {
    return (centerY - height / 2);
  }

  public double moveX(double distance) {
    return centerX += distance;
  }

  public double getCenterX() {
    return centerX;
  }
  public void setCenterX(double centerX) {
    this.centerX = centerX;
  }
  public double getCenterY() {
    return centerY;
  }
  public void setCenterY(double centerY) {
    this.centerY = centerY;
  }
  public int getIndexInLayer() {
    return indexInLayer;
  }
  public void setIndexInLayer(int indexInLayer) {
    this.indexInLayer = indexInLayer;
  }
  public ArrayList getPredecessors() {
    return predecessors;
  }
  public int getRank() {
    return rank;
  }
  public ArrayList getSuccessors() {
    return successors;
  }
  public Object getUserVertex() {
    return userVertex;
  }
  public double getWeight() {
    return weight;
  }
  public double getWidth() {
    return width;
  }
  public void setUserVertex(Object userVertex) {
    this.userVertex = userVertex;
  }
  public void setWeight(double weight) {
    this.weight = weight;
  }
  public void setWidth(double width) {
    this.width = width;
  }
  public void setRank(int rank) {
    this.rank = rank;
  }
  public void setSuccessors(ArrayList successors) {
    this.successors = successors;
  }
  public void setPredecessors(ArrayList predecessors) {
    this.predecessors = predecessors;
  }

  public static class LayerIndexComparator implements Comparator {
    public int compare(Object vertex1, Object vertex2) {
      return (((LayerVertex)vertex1).indexInLayer - ((LayerVertex)vertex2).indexInLayer);
    }
  }

  public static class PositionInLayer implements DoubleFunction {
    public double doubleValue(Object vertex) {
      return ((LayerVertex)vertex).indexInLayer;
    }
  }

  private double pendulumForce(ArrayList adjacent) {
    double forceValue = 0;
    int size = adjacent.size();
    if (size == 0) return 0;
    forceValue = deflection(adjacent) / size;
    return forceValue;
  }

  private double deflection(ArrayList adjacent) {
    double value = 0;
    int size = adjacent.size();
    for (int i = 0; i < size; i++) {
      LayerVertex v = (LayerVertex)adjacent.get(i);
      value += v.centerX - centerX;
    }
    return value;
  }

  private double medianValue(ArrayList adjacent) {
    if (adjacent.size() > 1)
      Collections.sort(adjacent, LayerVertex.LAYER_INDEX_COMPARATOR);
    weight = MedianUtils.weightedMedianValue(adjacent, LayerVertex.POSITION_IN_LAYER);
    return weight;
  }

  public int inDegree() {
    return predecessors.size();
  }

  public int outDegree() {
    return successors.size();
  }

  public int degree() {
    return predecessors.size() + successors.size();
  }

  public LayerVertex getPredecessor(int index) {
    return (index < inDegree() ? (LayerVertex)predecessors.get(index) : null);
  }

  public LayerVertex getSuccessor(int index) {
    return (index < outDegree() ? (LayerVertex)successors.get(index) : null);
  }

  public int countLeftCrossings(int[] leftNeighborSuccIndices) {
    int succSize = successors.size();
    int count = 0;
    for (int k = 0; k < succSize; k++) {
      LayerVertex successor = (LayerVertex)successors.get(k);
      int succIndex = successor.getIndexInLayer();
      for (int l = leftNeighborSuccIndices.length - 1; l >= 0; l--) {
        if (leftNeighborSuccIndices[l] > succIndex) count++;
        else break;
      }
    }
    return count;
  }
  public double getHeight() {
    return height;
  }
  public void setHeight(double height) {
    this.height = height;
  }

  public void setGeometry(Attribute shapeAttribute) {
    RectangularShape shape = (RectangularShape)shapeAttribute.get(userVertex);
    if (shape != null) {
      width = shape.getWidth();
      height = shape.getHeight();
      centerX = shape.getCenterX();
      centerY = shape.getCenterY();
    } else
      width = height = centerX = centerY = 0;


  }

  public void updateGeometry(Attribute shapeAttribute) {
    RectangularShape shape = (RectangularShape)shapeAttribute.get(userVertex);
    if (shape != null)
      shape.setFrame(centerX - width / 2, centerY - height / 2, width, height);
  }

  public boolean isDummy() {
    return userVertex == null;
  }

  public void setParentSubgraph(NestedSubgraph parentSubgraph) {
    this.parentSubgraph = parentSubgraph;
  }
  public NestedSubgraph getParentSubgraph() {
    return parentSubgraph;
  }
  public void setLeftX(double leftX) {
    this.leftX = leftX;
  }
  public double getLeftX() {
    return leftX;
  }
  public void setRightX(double rightX) {
    this.rightX = rightX;
  }
  public double getRightX() {
    return rightX;
  }
  public void setClassIndex(int classIndex) {
    this.classIndex = classIndex;
  }
  public int getClassIndex() {
    return classIndex;
  }
  public double getPosition() {
    return indexInLayer;
  }
  public int countVertices(int rank) {
    return (rank != this.rank ? 0 : 1);
  }
  public int getVertexCount(int rank) {
    return (rank != this.rank ? 0 : 1);
  }
  public double computePosition(int rank) {
    return (rank != this.rank ? Double.NaN : indexInLayer);
  }
  public int countVertices() {
    return 1;
  }
  public double getPosition(int rank) {
    return computePosition(rank);
  }
  public int getVertexCount() {
    return 1;
  }
  public double computePosition() {
    return indexInLayer;
  }

  public int reindex(
      int rank, int firstIndex, Comparator comparator, Predicate predicate) {
    indexInLayer = firstIndex++;
    return firstIndex;
  }

  public boolean isDefinedLeftX() {
    return !Double.isNaN(leftX);
  }

  public boolean isDefinedRightX() {
    return !Double.isNaN(rightX);
  }

  public String toString() {
    return (userVertex != null ? userVertex.toString() : "*");
  }

  public boolean includesRank(int rank) {
    return (this.rank == rank);
  }
}

