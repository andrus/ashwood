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
import java.awt.Point;
import java.awt.geom.*;
import org.objectstyle.ashwood.graph.*;
import org.objectstyle.ashwood.function.*;
import org.objectstyle.ashwood.util.*;
import org.apache.commons.collections.*;

public class DefaultLayerPartition {
  private Layer[] layers;
  private RankFunction rankFunction;
  private IntPartition[] layerPartitions;
  private double horizontalSpacing = 1.0;
  private double verticalSpacing = 1.0;
  private int verticalAlignment = LayoutConstants.CENTER;
  private boolean alternatePendulumTraversals = false;
  private double rubberForceThreshold = 1.0;
  private Digraph longArcDigraph;
  private SubgraphPartition subgraphPartition;
  private boolean dummyFixed;

  public DefaultLayerPartition(Digraph digraph,
                               RankFunction rankFunction,
                               Attribute vertexShape,
                               Set[] subgraphPartition) {
    createLayers(digraph, rankFunction, vertexShape, subgraphPartition);
  }

  public Layer getLayer(int rank) {
    return layers[rank];
  }

  public Object getUserVertex(int rank, int index) {
    return layers[rank].getUserVertex(index);
  }

  public void sort(int rank, Comparator comparator) {
    layers[rank].sort(comparator);
  }

  public void sort(int rank) {
    sort(rank, ComparatorUtils.NATURAL_COMPARATOR);
  }

  public void sortDownByMedian() {
    if (subgraphPartition != null)
        subgraphPartition.groupVertices(layers[0]);
    for (int rank = 1; rank < layers.length; rank++) {
      Layer layer = layers[rank];
      for (int j = 0; j < layer.size(); j++) {
        LayerVertex v = layer.getVertex(j);
        v.predecessorMedianValue();
      }
      sort(rank);
      if (subgraphPartition != null)
        subgraphPartition.groupVertices(layer);
    }
    if (subgraphPartition != null)
        subgraphPartition.untwineSubgraphs(layers);
  }

  public void sortUpByMedian() {
    if (subgraphPartition != null)
        subgraphPartition.groupVertices(layers[layers.length - 1]);
    for (int rank = layers.length - 2; rank >= 0; rank--) {
      Layer layer = layers[rank];
      for (int j = 0; j < layer.size(); j++) {
        LayerVertex v = (LayerVertex)layer.get(j);
        v.successorMedianValue();
      }
      sort(rank);
      if (subgraphPartition != null)
        subgraphPartition.groupVertices(layer);
    }
    if (subgraphPartition != null)
        subgraphPartition.untwineSubgraphs(layers);
  }

  public void breadthFirstSort() {
    Buffer queue = new UnboundedFifoBuffer();
    Set seen = new HashSet();
    queue.addAll(layers[0]);
    seen.addAll(layers[0]);
    int[] indices = new int[layers.length];
    while (!queue.isEmpty()) {
      LayerVertex origin = (LayerVertex)queue.remove();
      origin.setIndexInLayer(indices[origin.getRank()]++);
      for (int i = 0; i < origin.outDegree(); i++) {
        Object dst = origin.getSuccessors().get(i);
        if (seen.add(dst)) queue.add(dst);
      }
    }
    for (int i = 0; i < layers.length; i++) {
      layers[i].sort();
    }
  }

  public int countCrossings(int rank) {
    if (rank == layers.length - 1) return 0;
    Layer layer = layers[rank];
    int count = 0;
    int layerSize = layer.size();
    for (int i = 0; i < layerSize - 1; i++) {
      LayerVertex wrapper = layer.getVertex(i);
      int[] succIndices = wrapper.getSuccessorIndices();
      for (int j = i + 1; j < layerSize; j++) {
        LayerVertex rightNeighbor = layer.getVertex(j);
        count += rightNeighbor.countLeftCrossings(succIndices);
      }
    }
    return count;
  }

  public int countCrossings() {
    int count = 0;
    for (int i = 0; i < layers.length - 1; i++) {
      count += countCrossings(i);
    }
    return count;
  }

  public int getMaxRank() {
    return layers.length - 1;
  }

  public int getRankCount() {
    return layers.length;
  }

  private void createLayers(Digraph digraph,
                            RankFunction rankFunction,
                            Attribute vertexShape,
                            Set[] subgraphPartition) {
    layers = new Layer[rankFunction.maxRank() + 1];
    for (int i = 0; i < layers.length; i++) {
      layers[i] = new Layer();
      layers[i].setRank(i);
    }

    Map vertexMap = new HashMap(digraph.order());
    longArcDigraph = new MapDigraph();
    for (Iterator i = digraph.vertexIterator(); i.hasNext(); ) {
      Object vertex = i.next();
      int rank = rankFunction.intValue(vertex);
      LayerVertex wrapper = (LayerVertex)vertexMap.get(vertex);
      if (wrapper == null) {
        wrapper = new LayerVertex(
            vertex, digraph.incomingSize(vertex), digraph.outgoingSize(vertex));
        if (vertexShape != null)
          wrapper.setGeometry(vertexShape);
        vertexMap.put(vertex, wrapper);
        layers[rank].add(wrapper);
      }
      for (ArcIterator j = digraph.outgoingIterator(vertex); j.hasNext(); ) {
        j.next();
        Object dst = j.getDestination();
        int dstRank = rankFunction.intValue(dst);
        int increment = dstRank - rank;
        LayerVertex dstWrapper = (LayerVertex)vertexMap.get(dst);
        if (dstWrapper == null) {
          dstWrapper = new LayerVertex(
              dst, digraph.incomingSize(dst), digraph.outgoingSize(dst));
          if (vertexShape != null)
            dstWrapper.setGeometry(vertexShape);
          vertexMap.put(dst, dstWrapper);
          layers[dstRank].add(dstWrapper);
        }
        LayerVertex origin = wrapper;
        for (int k = 1; k < increment; k++) {
          LayerVertex dummy = new LayerVertex(null, 1, 1);
          if (k == 1)
            longArcDigraph.putArc(wrapper, dstWrapper, dummy);
          origin.getSuccessors().add(dummy);
          dummy.getPredecessors().add(origin);
          layers[rank + k].add(dummy);
          origin = dummy;
        }
        origin.getSuccessors().add(dstWrapper);
        dstWrapper.getPredecessors().add(origin);
      }
    }

    if (subgraphPartition != null && subgraphPartition.length > 0) {
      NestedSubgraph root = new NestedSubgraph();
      root.setLabel("r");
      this.subgraphPartition = new SubgraphPartition();
      for (int i = 0; i < subgraphPartition.length; i++) {
        Set subgraph = subgraphPartition[i];
        NestedSubgraph nestedSubgraph = new NestedSubgraph(subgraph.size());
        nestedSubgraph.setLabel(String.valueOf(i));
        for (Iterator j = subgraph.iterator(); j.hasNext(); ) {
          Object vertex = j.next();
          LayerVertex v = (LayerVertex)vertexMap.get(vertex);
          nestedSubgraph.add(v);
        }
        root.add(nestedSubgraph);
      }
      for (ArcIterator i = longArcDigraph.arcIterator(); i.hasNext(); ) {
        LayerVertex dummy = (LayerVertex)i.next();
        LayerVertex origin = (LayerVertex)i.getOrigin();
        LayerVertex dst = (LayerVertex)i.getDestination();
        NestedSubgraph subgraph = origin.getParentSubgraph();
        if (subgraph == null || subgraph != dst.getParentSubgraph()) continue;
        do {
          subgraph.add(dummy);
          dummy = (LayerVertex)dummy.getSuccessors().get(0);
        } while (dummy != dst);
      }
      for (int i = 0; i < layers.length; i++) {
        Layer layer = layers[i];
        for (int j = 0; j < layer.size(); j++) {
          LayerVertex v = layer.getVertex(j);
          if (v.getParentSubgraph() == null) root.add(v);
        }
      }
      this.subgraphPartition.setPartition(root);
    }
  }

  public Map makeOrderSnapshot() {
    Map snapshot = new HashMap();
    for (int i = 0; i < layers.length; i++) {
      Layer layer = layers[i];
      for (int j = 0; j < layer.size(); j++) {
        LayerVertex v = layer.getVertex(j);
        snapshot.put(v, new Integer(v.getIndexInLayer()));
      }
    }
    return snapshot;
  }

  public void restoreOrder(Map orderSnapshot) {
    for (int i = 0; i < layers.length; i++) {
      Layer layer = layers[i];
      for (int j = 0; j < layer.size(); j++) {
        LayerVertex v = layer.getVertex(j);
        v.setIndexInLayer(orderSnapshot.get(v).hashCode());
      }
      layer.sort();
    }
  }

  public void refreshIndices() {
    for (int i = 0; i < layers.length; i++) {
      layers[i].refreshIndices();
    }
  }

  public double balanceMeasure() {
    double value = 0;
    int denominator = 0;
    for (int i = 0; i < layers.length; i++) {
      Layer layer = layers[i];
      for (int j = 0; j < layer.size(); j++) {
        LayerVertex v = layer.getVertex(j);
        value += v.deflection();
        denominator += v.degree();
      }
    }
    if (denominator == 0) return 0.0;
    value = Math.abs(value) / denominator;
    return value;
  }

  private void createLayerPartition(int rank, boolean topDown) {
    IntPartition layerPartition = layerPartitions[rank];
    layerPartition.reset();
    LayerVertex leftNeighbor = null;
    double leftForce = Double.NEGATIVE_INFINITY;
    int leftRegionId = -1;
    ArrayList layer = layers[rank];
    for (int i = 0; i < layer.size(); i++) {
      LayerVertex vertex = (LayerVertex)layer.get(i);
      double force = (topDown ?
                      vertex.predecessorPendulumForce() :
                      vertex.successorPendulumForce());
      int regionId = layerPartition.findSetId(i);
      if (vertex.isTouchingToLeft(leftNeighbor, horizontalSpacing)) {
        if (!(dummyFixed && (vertex.isDummy() || leftNeighbor.isDummy()))) {
          if ((leftForce * force > 0) || (leftForce == force))
            layerPartition.joinSets(leftRegionId, regionId);
        }
      }
      leftRegionId = layerPartition.findSetId(regionId);
      leftForce = force;
      leftNeighbor = vertex;
    }
  }

  private void joinRegions(int rank, boolean topDown) {
    IntPartition layerPartition = layerPartitions[rank];
    ArrayList layer = layers[rank];
    int layerSize = layer.size();
    int regionCount;
    do {
      regionCount = layerPartition.getSetCount();
      double leftNeighborRegionForce = Double.NEGATIVE_INFINITY;
      int leftNeighborRegionId = -1;
      LayerVertex leftNeighborRightmostVertex = null;
      for (int i = 0; i < layerSize;) {
        int regionId = layerPartition.findSetId(i);
        int regionSize = 0;
        double regionForce = 0;
        LayerVertex leftmostVertex = (LayerVertex)layer.get(i);
        LayerVertex rightmostVertex;
        do {
          rightmostVertex = (LayerVertex)layer.get(i);
          regionForce += (topDown ?
                          rightmostVertex.predecessorPendulumForce() :
                          rightmostVertex.successorPendulumForce());
          regionSize++;
          i++;
        } while (i < layerSize && regionId == layerPartition.findSetId(i));
        regionForce /= regionSize;
        if (leftmostVertex.isTouchingToLeft(leftNeighborRightmostVertex, horizontalSpacing)) {
          if (!(dummyFixed && (leftmostVertex.isDummy() || leftNeighborRightmostVertex.isDummy()))) {
            if ((leftNeighborRegionForce >= 0 && regionForce <= 0) ||
                (regionForce >= 0 && leftNeighborRegionForce > regionForce) ||
                (leftNeighborRegionForce < 0 && regionForce < leftNeighborRegionForce)) {
              layerPartition.joinSets(leftNeighborRegionId, regionId);
            }
          }
        }
        leftNeighborRegionForce = regionForce;
        leftNeighborRegionId = layerPartition.findSetId(regionId);
        leftNeighborRightmostVertex = rightmostVertex;
      }
    } while (regionCount > layerPartition.getSetCount());
  }

  private void move(int rank, boolean topDown) {
    IntPartition layerPartition = layerPartitions[rank];
    ArrayList layer = layers[rank];
    int layerSize = layer.size();
    for (int i = 0; i < layerSize;) {
      int regionId = layerPartition.findSetId(i);
      int regionSize = 0;
      double regionForce = 0;
      LayerVertex leftmostVertex = (LayerVertex)layer.get(i);
      int leftmostIndex = i;
      LayerVertex rightmostVertex;
      do {
        rightmostVertex = (LayerVertex)layer.get(i);
        regionForce += (topDown ?
                        rightmostVertex.predecessorPendulumForce() :
                        rightmostVertex.successorPendulumForce());
        regionSize++;
        i++;
      } while (i < layerSize && regionId == layerPartition.findSetId(i));
      int rightmostIndex = i - 1;
      regionForce /= regionSize;
      double moveDistance = 0.0;
      if (regionForce < 0) {
        if (leftmostIndex <= 0)
          moveDistance = regionForce;
        else {
          LayerVertex leftNeighborRightmostVertex = (LayerVertex)layer.get(leftmostIndex - 1);
          double minDistance = horizontalSpacing;
          moveDistance = - Math.min(
              -regionForce,
              leftmostVertex.distanceToLeft(leftNeighborRightmostVertex) - minDistance);
        }
      } else if (regionForce > 0) {
        if (rightmostIndex >= layerSize - 1)
          moveDistance = regionForce;
        else {
          LayerVertex rightNeighborLeftmostVertex = (LayerVertex)layer.get(rightmostIndex + 1);
          double minDistance = horizontalSpacing;
          moveDistance = Math.min(
              regionForce,
              rightNeighborLeftmostVertex.distanceToLeft(rightmostVertex) - minDistance);
        }
      }
      if (moveDistance != 0.0) {
        for (int j = leftmostIndex; j <= rightmostIndex; j++) {
          LayerVertex vertex = (LayerVertex)layer.get(j);
          if (!(dummyFixed && vertex.isDummy())) {
            vertex.moveX(moveDistance);
          }
        }
      }
    }
  }

  private void balanceLayer(int rank, boolean topDown) {
    createLayerPartition(rank, topDown);
    joinRegions(rank, topDown);
    move(rank, topDown);
  }

  public void createSubgraphPartition() {
    if (subgraphPartition != null)
      subgraphPartition.insertBorderSegments(layers);
  }

  public int balancePendulum(int iterationCount) {
    layerPartitions = new IntPartition[layers.length];
    for (int i = 0; i < layers.length; i++) {
      layerPartitions[i] = new IntPartition(layers[i].size());
    }
    double balanceMeasure;
    double newBalanceMeasure = Double.POSITIVE_INFINITY;
    int iterations = 0;
    boolean topDown = true;
    if (!dummyFixed)
      initXPositions();
    int iteration = 0;
    do {
      iteration++;
      balanceMeasure = newBalanceMeasure;
      int start = (topDown ? 1 : layers.length - 2);
      int finish = (topDown ? layers.length : -1);
      int step = (topDown ? 1 : -1);
      for (int i = start; i != finish; i += step) {
        balanceLayer(i, topDown);
      }
      newBalanceMeasure = balanceMeasure();
      if (alternatePendulumTraversals) topDown = !topDown;
    } while (newBalanceMeasure < balanceMeasure && iteration <= iterationCount);
    return iteration;
  }

  private void initXPositions() {
    for (int i = 0; i < layers.length; i++) {
      initXPositions(i);
    }
  }

  private void initXPositions(int rank) {
    ArrayList layer = layers[rank];
    int layerSize = layer.size();
    double minX = 0.0;
    for (int i = 0; i < layerSize; i++) {
      LayerVertex vertex = (LayerVertex)layer.get(i);
      vertex.setCenterX(minX + vertex.getWidth() / 2);
      minX += vertex.getWidth() + horizontalSpacing;
    }
  }

  public int balanceRubberBends(int iterationCount) {
    boolean rubberForceThresholdArchived;
    double balanceMeasure = Double.POSITIVE_INFINITY;
    double newBalanceMeasure;
    int iteration = 0;
    do {
      iteration++;
      rubberForceThresholdArchived = true;
      for (int i = 0; i < layers.length; i++) {
        ArrayList layer = layers[i];
        int layerSize = layer.size();
        double minX = 0.0;
        for (int j = 0; j < layerSize; j++) {
          LayerVertex vertex = (LayerVertex)layer.get(j);
          if (dummyFixed && vertex.isDummy()) continue;
          double force = vertex.rubberForce();
          if (Math.abs(force) > rubberForceThreshold) {
            rubberForceThresholdArchived = false;
            if (force < 0) {
              if (j == 0)
                vertex.moveX(force);
              else {
                LayerVertex leftNeighbor = (LayerVertex)layer.get(j - 1);
                vertex.moveX( - Math.min(
                    -force,
                    vertex.distanceToLeft(leftNeighbor) - horizontalSpacing));
              }
            } else {
              if (j == layerSize - 1)
                vertex.moveX(force);
              else {
                LayerVertex rightNeighbor = (LayerVertex)layer.get(j + 1);
                vertex.moveX(Math.min(
                    force,
                    rightNeighbor.distanceToLeft(vertex) - horizontalSpacing));
              }
            }
            newBalanceMeasure = balanceMeasure();
            if (balanceMeasure <= newBalanceMeasure)
              return iteration;
            else
              balanceMeasure = newBalanceMeasure;
          }
        }
      }
    } while (!rubberForceThresholdArchived  && iteration <= iterationCount);
    return iteration;
  }
  public boolean isAlternatePendulumTraversals() {
    return alternatePendulumTraversals;
  }
  public void setAlternatePendulumTraversals(boolean alternatePendulumTraversals) {
    this.alternatePendulumTraversals = alternatePendulumTraversals;
  }
  public double getRubberForceThreshold() {
    return rubberForceThreshold;
  }
  public void setRubberForceThreshold(double rubberForceThreshold) {
    this.rubberForceThreshold = rubberForceThreshold;
  }
  public double getHorizontalSpacing() {
    return horizontalSpacing;
  }
  public void setHorizontalSpacing(double horizontalSpacing) {
    this.horizontalSpacing = horizontalSpacing;
  }
  public double getVerticalSpacing() {
    return verticalSpacing;
  }
  public void setVerticalSpacing(double verticalSpacing) {
    this.verticalSpacing = verticalSpacing;
  }

  public void positionLayers() {
    double layerY = 0.0;
    switch (verticalAlignment) {
      case LayoutConstants.TOP:
        for (int i = 0; i < layers.length; i++) {
          ArrayList layer = layers[i];
          int layerSize = layer.size();
          double increment = 0.0;
          for (int j = 0; j < layerSize; j++) {
            LayerVertex vertex = (LayerVertex)layer.get(j);
            vertex.setCenterY(layerY + vertex.getHeight() / 2);
            increment = Math.max(increment, vertex.getHeight());
          }
          layerY += increment + verticalSpacing;
        }
        break;
      case LayoutConstants.BOTTOM:
        for (int i = 0; i < layers.length; i++) {
          ArrayList layer = layers[i];
          int layerSize = layer.size();
          double increment = 0.0;
          for (int j = 0; j < layerSize; j++) {
            LayerVertex vertex = (LayerVertex)layer.get(j);
            increment = Math.max(increment, vertex.getHeight());
          }
          layerY += increment;
          for (int j = 0; j < layerSize; j++) {
            LayerVertex vertex = (LayerVertex)layer.get(j);
            vertex.setCenterY(layerY - vertex.getHeight() / 2);
          }
          layerY += verticalSpacing;
        }
        break;
      default:
        for (int i = 0; i < layers.length; i++) {
          ArrayList layer = layers[i];
          int layerSize = layer.size();
          double increment = 0.0;
          for (int j = 0; j < layerSize; j++) {
            LayerVertex vertex = (LayerVertex)layer.get(j);
            increment = Math.max(increment, vertex.getHeight() / 2);
          }
          layerY += increment;
          for (int j = 0; j < layerSize; j++) {
            LayerVertex vertex = (LayerVertex)layer.get(j);
            vertex.setCenterY(layerY);
          }
          layerY += increment + verticalSpacing;
        }
    }
  }

  public void positionDummies() {
    ArcStraightener arcStraightener = new ArcStraightener();
    arcStraightener.positionDummies(layers, horizontalSpacing);
  }

  public void updateGeometry(Attribute vertexShape, Rectangle2D areaBounds) {
    double minX = Double.POSITIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < layers.length; i++) {
      ArrayList layer = layers[i];
      int layerSize = layer.size();
      for (int j = 0; j < layerSize; j++) {
        LayerVertex vertex = (LayerVertex)layer.get(j);
        minX = Math.min(minX, vertex.getMinX());
        minY = Math.min(minY, vertex.getMinY());
        maxX = Math.max(maxX, vertex.getMaxX());
        maxY = Math.max(maxY, vertex.getMaxY());
        if (!vertex.isDummy())
          vertex.updateGeometry(vertexShape);
      }
    }
    areaBounds.setFrame(minX, minY, maxX - minX, maxY - minY);
  }

  public int getVerticalAlignment() {
    return verticalAlignment;
  }
  public void setVerticalAlignment(int verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
  }

  public Digraph positionArcs() {
    Digraph arcGeometryDigraph = new MapDigraph();
    for (ArcIterator i = longArcDigraph.arcIterator(); i.hasNext(); ) {
      LayerVertex dummy = (LayerVertex)i.next();
      LayerVertex origin = (LayerVertex)i.getOrigin();
      LayerVertex dst = (LayerVertex)i.getDestination();
      int pointCount = dst.getRank() - origin.getRank() + 1;
      List points = new ArrayList(pointCount);
      double x1 = origin.getCenterX();
      double x2 = dummy.getCenterX();
      double y1 = origin.getCenterY();
      double y2 = dummy.getCenterY();
      double h = origin.getHeight() / 2;
      double x = (x2 - x1) * h / (y2 - y1) + x1;
      double y = y1 + h;
      points.add(new Point((int)x, (int)y));
      LayerVertex successor = dummy;
      int pointIndex = 0;
      do {
        dummy = successor;
        successor = dummy.getSuccessor(0);
        pointIndex++;
        if (pointCount > 4 && pointIndex > 1 && pointIndex < pointCount - 2)
          continue;
        x2 = dummy.getCenterX();
        y2 = dummy.getCenterY();
        if (pointCount >= 4) {
          if (pointIndex == 1) y2 -= origin.getHeight() / 2;
          else if (pointIndex == pointCount - 2) y2 += dst.getHeight() / 2;
        }
        points.add(new Point((int)x2, (int)y2));
      } while (successor != dst);
      x1 = dst.getCenterX();
      y1 = dst.getCenterY();
      h = dst.getHeight() / 2;
      x = (x2 - x1) * h / (y1 - y2) + x1;
      y = y1 - h;
      points.add(new Point((int)x, (int)y));
      arcGeometryDigraph.putArc(origin.getUserVertex(), dst.getUserVertex(), points);
    }

    return arcGeometryDigraph;
  }
  public void setDummyFixed(boolean dummyFixed) {
    this.dummyFixed = dummyFixed;
  }
  public boolean isDummyFixed() {
    return dummyFixed;
  }

  public void printLayers() {
    for (int i = 0; i < layers.length; i++) {
      System.out.print("l[" + layers[i].getRank() + "]: ");
      for (int j = 0; j < layers[i].size(); j++) {
        LayerVertex v = layers[i].getVertex(j);
        String sgLabel = (v.getParentSubgraph() != null ?
                          v.getParentSubgraph().getLabel() :
                          "?");
        System.out.print(sgLabel + "." + v + "{" + v.getRank() + "," + v.getIndexInLayer() +"}, ");
      }
      System.out.println();
    }
  }
}