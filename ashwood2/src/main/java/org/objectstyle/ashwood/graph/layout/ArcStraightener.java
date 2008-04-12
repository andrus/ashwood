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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ArcStraightener {
	private Layer[] layers;
	private SortedMap classes;
	private double horizontalSpacing;

	public ArcStraightener() {
	}

	public void positionDummies(Layer[] layers, double horizontalSpacing) {
		this.layers = layers;
		this.horizontalSpacing = horizontalSpacing;

		computeLeftPositions();
		// printLayers();

		for (int i = 0; i < layers.length; i++)
			for (int j = 0; j < layers[i].size(); j++)
				layers[i].getVertex(j).setClassIndex(-1);

		computeRightPositions();
		// printLayers();

		position();
	}

	private void computeLeftPositions() {
		// left to right scan
		computeClasses(true);
		for (Iterator i = classes.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			int classIndex = entry.getKey().hashCode();
			List clazz = (List) entry.getValue();
			for (int j = 0; j < clazz.size(); j++) {
				LayerVertex v = (LayerVertex) clazz.get(j);
				if (!v.isDefinedLeftX())
					placeLeft(v);
			}
			adjustLeftClass(classIndex);
		}
	}

	private void computeRightPositions() {
		// right to left scan
		computeClasses(false);
		for (Iterator i = classes.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			int classIndex = entry.getKey().hashCode();
			List clazz = (List) entry.getValue();
			for (int j = 0; j < clazz.size(); j++) {
				LayerVertex v = (LayerVertex) clazz.get(j);
				if (!v.isDefinedRightX())
					placeRight(v);
			}
			adjustRightClass(classIndex);
		}
	}

	private void position() {
		for (int i = 0; i < layers.length; i++) {
			for (int j = 0; j < layers[i].size(); j++) {
				LayerVertex v = layers[i].getVertex(j);
				v.setCenterX((v.getLeftX() + v.getRightX()) / 2);
			}
		}
	}

	private void computeClasses(boolean left) {
		classes = new TreeMap();
		for (int i = 0; i < layers.length; i++) {
			int classIndex = i;
			int start = (left ? 0 : layers[i].size() - 1);
			int end = (left ? layers[i].size() : -1);
			int step = (left ? 1 : -1);
			for (int j = start; j != end; j += step) {
				LayerVertex v = layers[i].getVertex(j);
				if (v.getClassIndex() >= 0) {
					// vertex already classified
					classIndex = v.getClassIndex();
					continue;
				}
				// classify vertex
				List clazz = getClazz(classIndex);
				// original vertex
				if (!v.isDummy()) {
					v.setClassIndex(classIndex);
					clazz.add(v);
					continue;
				}
				// otherwise, all dummies in the long arc
				while (v.isDummy()) {
					v.setClassIndex(classIndex);
					clazz.add(v);
					if (v.outDegree() != 1)
						break;
					v = v.getSuccessor(0);
				}
			}
		}
	}

	private List getClazz(int index) {
		Integer key = new Integer(index);
		List clazz = (List) classes.get(key);
		if (clazz == null) {
			clazz = new ArrayList();
			classes.put(key, clazz);
		}
		return clazz;
	}

	private void placeLeft(LayerVertex vertex) {
		Layer layer = layers[vertex.getRank()];
		double vertexPosition = Double.NEGATIVE_INFINITY;
		if (!vertex.isDummy()) {
			if (layer.isFirst(vertex))
				vertexPosition = 0; // first in layer
			else {
				// get left neighbor
				LayerVertex leftNeighbor = layer.getLeft(vertex);
				if (leftNeighbor.getClassIndex() == vertex.getClassIndex()) {
					// place neighbor
					if (!leftNeighbor.isDefinedLeftX())
						placeLeft(leftNeighbor);
					// place vertex right to neighbor
					double distance = vertex.minCenterDistance(leftNeighbor,
							horizontalSpacing);
					vertexPosition = leftNeighbor.getLeftX() + distance;
				} else
					vertexPosition = 0; // left neighbor in different class
			}
			vertex.setLeftX(vertexPosition);
		} else {
			LayerVertex v = vertex;
			while (vertex.isDummy()) {
				layer = layers[vertex.getRank()];
				if (!layer.isFirst(vertex)) {
					LayerVertex leftNeighbor = layer.getLeft(vertex);
					if (leftNeighbor.getClassIndex() == vertex.getClassIndex()) {
						if (!leftNeighbor.isDefinedLeftX())
							placeLeft(leftNeighbor);
						double distance = vertex.minCenterDistance(
								leftNeighbor, horizontalSpacing);
						vertexPosition = Math.max(vertexPosition, leftNeighbor
								.getLeftX()
								+ distance);
					}
				}
				if (vertex.outDegree() != 1)
					break;
				vertex = vertex.getSuccessor(0);
			}
			vertexPosition = (Double.isInfinite(vertexPosition) ? 0
					: vertexPosition);
			vertex = v;
			while (vertex.isDummy()) {
				vertex.setLeftX(vertexPosition);
				if (vertex.outDegree() != 1)
					break;
				vertex = vertex.getSuccessor(0);
			}
		}
	}

	private void placeRight(LayerVertex vertex) {
		Layer layer = layers[vertex.getRank()];
		double vertexPosition = Double.POSITIVE_INFINITY;
		if (!vertex.isDummy()) {
			if (layer.isLast(vertex))
				vertexPosition = 0; // last in layer
			else {
				// get right neighbor
				LayerVertex rightNeighbor = layer.getRight(vertex);
				if (rightNeighbor.getClassIndex() == vertex.getClassIndex()) {
					// place neighbor
					try {
						if (!rightNeighbor.isDefinedRightX())
							placeRight(rightNeighbor);
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
					// place vertex left to neighbor
					double distance = vertex.minCenterDistance(rightNeighbor,
							horizontalSpacing);
					vertexPosition = rightNeighbor.getRightX() - distance;
				} else
					vertexPosition = 0; // right neighbor in different class
			}
			vertex.setRightX(vertexPosition);
		} else {
			LayerVertex v = vertex;
			while (vertex.isDummy()) {
				layer = layers[vertex.getRank()];
				if (!layer.isLast(vertex)) {
					LayerVertex rightNeighbor = layer.getRight(vertex);
					if (rightNeighbor.getClassIndex() == vertex.getClassIndex()) {
						if (!rightNeighbor.isDefinedRightX())
							placeRight(rightNeighbor);
						double distance = vertex.minCenterDistance(
								rightNeighbor, horizontalSpacing);
						vertexPosition = Math.min(vertexPosition, rightNeighbor
								.getRightX()
								- distance);
					}
				}
				if (vertex.outDegree() != 1)
					break;
				vertex = vertex.getSuccessor(0);
			}
			vertexPosition = (Double.isInfinite(vertexPosition) ? 0
					: vertexPosition);
			vertex = v;
			while (vertex.isDummy()) {
				vertex.setRightX(vertexPosition);
				if (vertex.outDegree() != 1)
					break;
				vertex = vertex.getSuccessor(0);
			}
		}
	}

	private void adjustLeftClass(int classIndex) {
		List clazz = (List) classes.get(new Integer(classIndex));
		double d = Double.POSITIVE_INFINITY;
		for (int i = 0; i < clazz.size(); i++) {
			LayerVertex v = (LayerVertex) clazz.get(i);
			Layer layer = layers[v.getRank()];
			if (layer.isLast(v))
				continue;
			LayerVertex rightNeighbor = layer.getRight(v);
			if (rightNeighbor.getClassIndex() == classIndex)
				continue;
			double distance = v.minCenterDistance(rightNeighbor,
					horizontalSpacing);
			d = Math.min(d, rightNeighbor.getLeftX() - v.getLeftX() - distance);
		}
		if (Double.isInfinite(d)) {
			List heap = new ArrayList(clazz.size() + 1);
			for (int i = 0; i < clazz.size(); i++) {
				LayerVertex v = (LayerVertex) clazz.get(i);
				for (int j = 0; j < v.inDegree(); j++) {
					LayerVertex w = v.getPredecessor(j);
					if (w.getClassIndex() < classIndex) {
						heap.add(new Double(w.getLeftX() - v.getLeftX()));
					}
				}
				for (int j = 0; j < v.outDegree(); j++) {
					LayerVertex w = v.getSuccessor(j);
					if (w.getClassIndex() < classIndex) {
						heap.add(new Double(w.getLeftX() - v.getLeftX()));
					}
				}
			}
			if (heap.size() == 0)
				d = 0;
			else {
				Collections.sort(heap);
				d = ((Double) heap.get(heap.size() / 2)).doubleValue();
			}
		}
		for (int i = 0; i < clazz.size(); i++) {
			LayerVertex v = (LayerVertex) clazz.get(i);
			v.setLeftX(v.getLeftX() + d);
		}
	}

	private void adjustRightClass(int classIndex) {
		List clazz = (List) classes.get(new Integer(classIndex));
		double d = Double.POSITIVE_INFINITY;
		for (int i = 0; i < clazz.size(); i++) {
			LayerVertex v = (LayerVertex) clazz.get(i);
			Layer layer = layers[v.getRank()];
			if (layer.isFirst(v))
				continue;
			LayerVertex leftNeighbor = layer.getLeft(v);
			if (leftNeighbor.getClassIndex() == classIndex)
				continue;
			double distance = v.minCenterDistance(leftNeighbor,
					horizontalSpacing);
			d = Math
					.min(d, v.getRightX() - leftNeighbor.getRightX() - distance);
		}
		if (Double.isInfinite(d)) {
			List heap = new ArrayList(clazz.size() + 1);
			for (int i = 0; i < clazz.size(); i++) {
				LayerVertex v = (LayerVertex) clazz.get(i);
				for (int j = 0; j < v.inDegree(); j++) {
					LayerVertex w = v.getPredecessor(j);
					if (w.getClassIndex() < classIndex) {
						heap.add(new Double(w.getRightX() - v.getRightX()));
					}
				}
				for (int j = 0; j < v.outDegree(); j++) {
					LayerVertex w = v.getSuccessor(j);
					if (w.getClassIndex() < classIndex) {
						heap.add(new Double(w.getRightX() - v.getRightX()));
					}
				}
			}
			if (heap.size() == 0)
				d = 0;
			else {
				Collections.sort(heap);
				d = -((Double) heap.get(heap.size() / 2)).doubleValue();
			}
		}
		for (int i = 0; i < clazz.size(); i++) {
			LayerVertex v = (LayerVertex) clazz.get(i);
			v.setRightX(v.getRightX() - d);
		}
	}

	private void printLayers() {
		for (int i = 0; i < layers.length; i++) {
			for (int j = 0; j < layers[i].size(); j++) {
				LayerVertex v = layers[i].getVertex(j);
				System.out.println("v=" + v.getUserVertex() + ", c="
						+ v.getClassIndex() + ", lx=" + v.getLeftX() + ", rx="
						+ v.getRightX() + ", w=" + v.getWidth());
			}
		}
	}
}