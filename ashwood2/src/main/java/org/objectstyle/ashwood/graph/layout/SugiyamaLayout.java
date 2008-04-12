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

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectstyle.ashwood.graph.ArcIterator;
import org.objectstyle.ashwood.graph.Digraph;
import org.objectstyle.ashwood.graph.GraphUtils;
import org.objectstyle.ashwood.graph.MapDigraph;
import org.objectstyle.ashwood.util.Attribute;
import org.objectstyle.ashwood.util.MapAttribute;
import org.objectstyle.ashwood.util.MutableInteger;
import org.objectstyle.ashwood.util.Pair;

public class SugiyamaLayout extends DigraphLayout {

	private double movePrecision = 1;
	private double stepSize = 1;
	private int adjustingPassCount = 2;
	private boolean rootsUpwards = true;

	// internal data
	private Map wrapperLevelMap;
	private Map vertexWrapperMap;
	private Map replacementMap = new HashMap();
	private VertexWrapper[][] levels = null;
	private Digraph wrapperDigraph;

	public SugiyamaLayout() {
	}

	public void doLayout() {
		// will operate on digraph of vertex wrappers instead of the original
		// digraph
		createWrapperDigraph();

		// classify vertices by level (longest type)
		wrapperLevelMap = GraphUtils.computeLevels(new HashMap(wrapperDigraph
				.order()), wrapperDigraph, true);

		// shift vertices to leaves if possible
		// GraphUtils.shiftLevelsDown(vertexLevelMap, digraph);

		// split long arcs - make (k,2)partite digraph
		splitLongArcs();

		// fill array of vertex levels
		createLevels();

		// minimize number of arc intersections between each two levels
		// via barycentric method
		minimizeArcIntersections();

		// adjust vertices horizontally to minimize arc lengths
		minimizeDistancesToBarycentres();

		// finally, compute coordinates of vertices and arcs
		computeGeometry();
	}

	private void createWrapperDigraph() {
		wrapperDigraph = new MapDigraph(MapDigraph.HASHMAP_FACTORY);
		vertexWrapperMap = new HashMap(digraph.order());
		for (Iterator i = digraph.vertexIterator(); i.hasNext();) {
			Object vertex = i.next();
			VertexWrapper wrapper = new VertexWrapper(vertex);
			vertexWrapperMap.put(vertex, wrapper);
			wrapperDigraph.addVertex(wrapper);
		}
		for (ArcIterator i = digraph.arcIterator(); i.hasNext();) {
			i.next();
			Object wrapper1 = vertexWrapperMap.get(i.getOrigin());
			Object wrapper2 = vertexWrapperMap.get(i.getDestination());
			if (rootsUpwards)
				wrapperDigraph.putArc(wrapper1, wrapper2, Boolean.TRUE);
			else
				wrapperDigraph.putArc(wrapper2, wrapper1, Boolean.TRUE);
		}
	}

	private void splitLongArcs() {
		for (ArcIterator i = wrapperDigraph.arcIterator(); i.hasNext();) {
			i.next();
			Object origin = i.getOrigin();
			Object dst = i.getDestination();
			int originLevel = wrapperLevelMap.get(origin).hashCode();
			int dstLevel = wrapperLevelMap.get(dst).hashCode();
			int increment = dstLevel - originLevel;
			if (increment == 1)
				continue;
			Pair splitArc = new Pair(origin, dst);
			List dummyWrappers = new ArrayList(increment - 1);
			for (int j = 1; j < increment; j++) {
				DummyWrapper dummyWrapper = new DummyWrapper(splitArc, j);
				dummyWrappers.add(dummyWrapper);
				wrapperLevelMap.put(dummyWrapper, new MutableInteger(
						originLevel + j));
			}
			replacementMap.put(splitArc, dummyWrappers);
		}
		for (Iterator i = replacementMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			Pair splitArc = (Pair) entry.getKey();
			List dummyWrappers = (List) entry.getValue();
			wrapperDigraph.removeArc(splitArc.first, splitArc.second);
			Object origin = splitArc.first;
			for (Iterator j = dummyWrappers.iterator(); j.hasNext();) {
				Object dummyVertex = j.next();
				wrapperDigraph.putArc(origin, dummyVertex, Boolean.TRUE);
				origin = dummyVertex;
			}
			wrapperDigraph.putArc(origin, splitArc.second, Boolean.TRUE);
		}
	}

	private void createLevels() {
		int maxLevel = 0;
		Map levelSizes = new HashMap();
		for (Iterator i = wrapperLevelMap.values().iterator(); i.hasNext();) {
			Number level = (Number) i.next();
			maxLevel = (maxLevel >= level.intValue() ? maxLevel : level
					.intValue());
			int[] levelSize = (int[]) levelSizes.get(level);
			if (levelSize == null)
				levelSizes.put(level, new int[] { 1 });
			else
				++levelSize[0];
		}
		levels = new VertexWrapper[maxLevel + 1][];
		for (int i = 0; i < levels.length; i++) {
			int levelSize = ((int[]) levelSizes.get(new MutableInteger(i)))[0];
			levels[i] = new VertexWrapper[levelSize];
		}
		int[][] currentIndexes = new int[levels.length][1];
		for (Iterator i = wrapperLevelMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			int level = entry.getValue().hashCode();
			int currentIndex = currentIndexes[level][0]++;
			VertexWrapper wrapper = (VertexWrapper) entry.getKey();
			levels[level][currentIndex] = wrapper;
		}
	}

	private void minimizeArcIntersections() {
		// move down
		for (int i = 0; i < levels.length - 1; i++) {
			for (int j = 0; j < levels[i + 1].length; j++) {
				VertexWrapper dst = levels[i + 1][j];
				double numenator = 0;
				double denominator = 0;
				for (int k = 0; k < levels[i].length; k++) {
					VertexWrapper origin = levels[i][k];
					double distance = (wrapperDigraph.hasArc(origin, dst) ? 1
							: 0);
					numenator += k * distance;
					denominator += distance;
				}
				dst.setBarycenter(numenator / denominator);
			}
			Arrays.sort(levels[i + 1]);
		}
		// move up
		for (int i = levels.length - 1; i > 0; i--) {
			for (int j = 0; j < levels[i - 1].length; j++) {
				VertexWrapper origin = levels[i - 1][j];
				double numenator = 0;
				double denominator = 0;
				for (int k = 0; k < levels[i].length; k++) {
					VertexWrapper dst = levels[i][k];
					double distance = (wrapperDigraph.hasArc(origin, dst) ? 1
							: 0);
					numenator += k * distance;
					denominator += distance;
				}
				origin.setBarycenter(numenator / denominator);
			}
			Arrays.sort(levels[i - 1]);
		}
	}

	private void minimizeDistancesToBarycentres() {
		// set center x, priorities
		for (int i = 0; i < levels.length; i++) {
			double previousCenterX = areaBounds.getMinX() - horizontalSpacing;
			double previousWidth = 0;
			for (int j = 0; j < levels[i].length; j++) {
				VertexWrapper box = levels[i][j];
				// init x coordinate of box center
				box.setCenterX(previousCenterX + horizontalSpacing
						+ (previousWidth + box.getWidth()) / 2);
				previousCenterX = box.getCenterX();
				previousWidth = box.getWidth();
				// set upper/lower priority
				box.setUpperConnectivity(wrapperDigraph.incomingSize(box));
				box.setLowerConnectivity(wrapperDigraph.outgoingSize(box));
			}
		}
		int passCount = Math.min(adjustingPassCount, levels.length - 1);
		for (int pass = 0; pass < adjustingPassCount; pass++) {
			// move down
			adjustMovingDown(pass + 1);
			// move up
			// if (pass != adjustingPassCount - 1)
			adjustMovingUp(levels.length - 2);
		}
	}

	private void adjustMovingDown(int upperLevel) {
		for (int i = upperLevel; i < levels.length; i++) {
			for (int j = 0; j < levels[i].length; j++) {
				VertexWrapper box = levels[i][j];
				double numenator = 0;
				for (int k = 0; k < levels[i - 1].length; k++) {
					VertexWrapper upperBox = levels[i - 1][k];
					if (wrapperDigraph.hasArc(upperBox, box))
						numenator += upperBox.getCenterX();
				}
				box.setUpperBarycenter(numenator / box.getUpperConnectivity());
				int priority = box.getUpperConnectivity();
				if (box.isDummy())
					priority = 0;// Integer.MAX_VALUE;
				double actualMovementDistance = 0;
				do {
					double distanceToBarycenter = box.getUpperBarycenter()
							- box.getCenterX();
					if (distanceToBarycenter > movePrecision) {
						actualMovementDistance = adjustVertexWithinLevel(true,
								false, levels[i], j, priority, stepSize);
					} else if (distanceToBarycenter < (-movePrecision)) {
						actualMovementDistance = adjustVertexWithinLevel(true,
								true, levels[i], j, priority, stepSize);
					} else
						actualMovementDistance = 0;
				} while (actualMovementDistance > movePrecision);
			}
		}
	}

	private void adjustMovingUp(int lowerLevel) {
		for (int i = lowerLevel; i >= 0; i--) {
			for (int j = 0; j < levels[i].length; j++) {
				VertexWrapper box = levels[i][j];
				double numenator = 0;
				for (int k = 0; k < levels[i + 1].length; k++) {
					VertexWrapper lowerBox = levels[i + 1][k];
					if (wrapperDigraph.hasArc(box, lowerBox))
						numenator += lowerBox.getCenterX();
				}
				box.setLowerBarycenter(numenator / box.getLowerConnectivity());
				int priority = box.getLowerConnectivity();
				if (box.isDummy())
					priority = 0;// Integer.MAX_VALUE;
				double actualMovementDistance = 0;
				do {
					double distanceToBarycenter = box.getLowerBarycenter()
							- box.getCenterX();
					if (distanceToBarycenter > movePrecision) {
						actualMovementDistance = adjustVertexWithinLevel(false,
								false, levels[i], j, priority, stepSize);
					} else if (distanceToBarycenter < (-movePrecision)) {
						actualMovementDistance = adjustVertexWithinLevel(false,
								true, levels[i], j, priority, stepSize);
					} else
						actualMovementDistance = 0;
				} while (actualMovementDistance > movePrecision);
			}
		}
	}

	private double adjustVertexWithinLevel(boolean wayDown, boolean toLeft,
			VertexWrapper[] level, int wrapperIndex, int priority,
			double wantedDistance) {
		int direction = (toLeft ? -1 : 1);
		VertexWrapper box = level[wrapperIndex];
		double centerX = box.getCenterX();
		double width = box.getWidth();
		double possibleDistance = 0;
		double actualDistance = 0;
		double areaWidth = areaBounds.getWidth();
		double areaMinX = areaBounds.getMinX();
		if (toLeft && wrapperIndex == 0) { // most left box moving left
			possibleDistance = centerX - (areaMinX + width / 2);
			actualDistance = Math.min(wantedDistance, possibleDistance);
		} else if (!toLeft && wrapperIndex == level.length - 1) { // most
			// right box
			// moving
			// right
			possibleDistance = areaMinX + areaWidth - (centerX + width / 2);
			actualDistance = Math.min(wantedDistance, possibleDistance);
		} else { // box in the middle, or most right one moving left, or most
			// left one moving right
			int neighborIndex = wrapperIndex + direction;
			VertexWrapper neighbor = level[neighborIndex];
			int neighborPriority = (wayDown ? neighbor.getUpperConnectivity()
					: neighbor.getLowerConnectivity());
			if (neighbor.isDummy())
				priority = 0;// Integer.MAX_VALUE;
			// how far we can move box without moving neighbor
			if (toLeft) {
				possibleDistance = centerX
						- (neighbor.getCenterX() + horizontalSpacing + (neighbor
								.getWidth() + width) / 2);
			} else {
				possibleDistance = neighbor.getCenterX()
						- (centerX + horizontalSpacing + (neighbor.getWidth() + width) / 2);
			}
			if (wantedDistance <= possibleDistance) { // enough for us
				actualDistance = wantedDistance;
			} else if (priority <= neighborPriority) { // not enough but
				// neighbor has higher
				// priority and does not
				// move
				actualDistance = possibleDistance;
			} else { // try to move neighbor as far as we need
				double askedDistance = wantedDistance - possibleDistance;
				// neighbor moved some, maybe less than was asked
				double neighborMovedDistance = adjustVertexWithinLevel(wayDown,
						toLeft, level, neighborIndex, priority, askedDistance);
				actualDistance = possibleDistance + neighborMovedDistance;
			}
		}
		box.setCenterX(centerX + direction * actualDistance);
		return actualDistance;
	}

	private void computeGeometry() {
		double levelY = areaBounds.getMinY();
		double areaMaxX = areaBounds.getMinX();
		for (int i = 0; i < levels.length; i++) {
			double lowerLevelY = levelY;
			VertexWrapper[] level = levels[i];
			for (int j = 0; j < level.length; j++) {
				lowerLevelY = Math.max(lowerLevelY, level[j]
						.setupVertexShape(levelY));
			}
			double levelMaxX = level[level.length - 1].getCenterX()
					+ level[level.length - 1].getWidth() / 2;
			levelY = lowerLevelY + verticalSpacing;
			areaMaxX = Math.max(areaMaxX, levelMaxX);
		}
		areaBounds.setFrame(areaBounds.getMinX(), areaBounds.getMinY(),
				areaMaxX - areaBounds.getMinX(), levelY - verticalSpacing
						- areaBounds.getMinY());
	}

	private RectangularShape getVertexShape(Object vertex) {
		return (RectangularShape) vertexShape.get(vertex);
	}

	public List computeBentArcs() {
		List arcs = new ArrayList(replacementMap.size());
		for (Iterator i = replacementMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			Pair splitArc = (Pair) entry.getKey();
			Object vertex1 = ((VertexWrapper) splitArc.first).getVertex();
			Object vertex2 = ((VertexWrapper) splitArc.second).getVertex();
			Pair vertexPair = (rootsUpwards ? new Pair(vertex1, vertex2)
					: new Pair(vertex2, vertex1));
			List dummyWrappers = (List) entry.getValue();
			List points = new ArrayList(dummyWrappers.size());
			for (Iterator j = dummyWrappers.iterator(); j.hasNext();) {
				DummyWrapper dummy = (DummyWrapper) j.next();
				points.add(dummy.getBendingPoint());
			}
			arcs.add(new Pair(vertexPair, points));
		}
		return arcs;
	}

	private class VertexWrapper implements Comparable {
		private Object vertex;
		private double barycenter;
		private double upperBarycenter;
		private double lowerBarycenter;
		private int upperConnectivity;
		private int lowerConnectivity;
		private double centerX;
		private double width = 0;

		private VertexWrapper(Object vertex) {
			this.vertex = vertex;
			if (vertex != null) {
				RectangularShape shape = getVertexShape(vertex);
				width = (shape != null ? shape.getWidth() : 0);
			}
		}

		void setBarycenter(double barycenter) {
			this.barycenter = barycenter;
		}

		void setUpperConnectivity(int upperConnectivity) {
			this.upperConnectivity = upperConnectivity;
		}

		int getUpperConnectivity() {
			return upperConnectivity;
		}

		void setLowerConnectivity(int lowerConnectivity) {
			this.lowerConnectivity = lowerConnectivity;
		}

		int getLowerConnectivity() {
			return lowerConnectivity;
		}

		void setUpperBarycenter(double upperBarycenter) {
			this.upperBarycenter = upperBarycenter;
		}

		double getUpperBarycenter() {
			return upperBarycenter;
		}

		void setLowerBarycenter(double lowerBarycenter) {
			this.lowerBarycenter = lowerBarycenter;
		}

		double getLowerBarycenter() {
			return lowerBarycenter;
		}

		void setCenterX(double centerX) {
			this.centerX = centerX;
		}

		double getCenterX() {
			return centerX;
		}

		void setWidth(double width) {
			this.width = width;
		}

		double getWidth() {
			return width;
		}

		Object getVertex() {
			return vertex;
		}

		boolean isDummy() {
			return false;
		}

		public int compareTo(Object o) {

			if (barycenter < ((VertexWrapper) o).barycenter) {
				return -1;
			}
			if (barycenter > ((VertexWrapper) o).barycenter) {
				return +1;
			}
			// Need to compare bits to handle 0.0 == -0.0 being true
			// compare should put -0.0 < +0.0
			// Two NaNs are also == for compare purposes
			// where NaN == NaN is false
			long lhsBits = Double.doubleToLongBits(barycenter);
			long rhsBits = Double
					.doubleToLongBits(((VertexWrapper) o).barycenter);
			if (lhsBits == rhsBits) {
				return 0;
			}
			// Something exotic! A comparison to NaN or 0.0 vs -0.0
			// Fortunately NaN's long is > than everything else
			// Also negzeros bits < poszero
			// NAN: 9221120237041090560
			// MAX: 9218868437227405311
			// NEGZERO: -9223372036854775808
			if (lhsBits < rhsBits) {
				return -1;
			} else {
				return +1;
			}
		}

		double setupVertexShape(double levelY) {
			if (vertex == null)
				return levelY;
			RectangularShape shape = getVertexShape(vertex);
			double height = shape.getHeight();
			shape.setFrame(centerX - width / 2, levelY, width, height);
			return (levelY + height);
		}
	}

	private class DummyWrapper extends VertexWrapper {
		private Pair splitArc;
		private int index;
		private Point bendingPoint;

		private DummyWrapper(Pair splitArc, int index) {
			super(null);
			this.splitArc = splitArc;
			this.index = index;
		}

		boolean isDummy() {
			return true;
		}

		double setupVertexShape(double levelY) {
			bendingPoint = new Point((int) getCenterX(), (int) levelY);
			return levelY;
		}

		Point getBendingPoint() {
			return bendingPoint;
		}
	}

	public void setMovePrecision(double movePrecision) {
		this.movePrecision = movePrecision;
	}

	public double getMovePrecision() {
		return movePrecision;
	}

	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
	}

	public double getStepSize() {
		return stepSize;
	}

	public void setAdjustingPassCount(int adjustingPassCount) {
		this.adjustingPassCount = adjustingPassCount;
	}

	public int getAdjustingPassCount() {
		return adjustingPassCount;
	}

	public void setRootsUpwards(boolean rootsUpwards) {
		this.rootsUpwards = rootsUpwards;
	}

	public boolean isRootsUpwards() {
		return rootsUpwards;
	}

	public static void main(String[] args) {
		// create sample graph
		Digraph digraph = createDigraph4();
		Rectangle2D vertexRect = new Rectangle2D.Double(0, 0, 20, 15);
		Rectangle2D areaBounds = new Rectangle2D.Double(0, 0, 500, 500);
		Map vertexShapeMap = new HashMap();
		for (Iterator i = digraph.vertexIterator(); i.hasNext();) {
			Object vertex = i.next();
			vertexShapeMap.put(vertex, vertexRect.clone());
		}
		Attribute vertexShape = new MapAttribute(vertexShapeMap);
		double horisontalSpacing = 5;
		double verticalSpacing = 5;
		int adjustingPassCount = 100;
		double movePrecision = 1;
		double stepSize = 1;
		boolean rootsUpwards = true;

		SugiyamaLayout layout = new SugiyamaLayout();
		layout.setDigraph(digraph);
		layout.setVertexShape(vertexShape);
		layout.setAreaBounds(areaBounds);
		layout.setAdjustingPassCount(adjustingPassCount);
		layout.setHorizontalSpacing(horisontalSpacing);
		layout.setVerticalSpacing(verticalSpacing);
		layout.setMovePrecision(movePrecision);
		layout.setStepSize(stepSize);
		layout.setRootsUpwards(rootsUpwards);

		layout.doLayout();

		System.out.println("Results:");
		System.out.println("Area: " + layout.getAreaBounds());
		System.out.println("Vertices:");
		Attribute vshape = layout.getVertexShape();
		for (Iterator i = digraph.vertexIterator(); i.hasNext();) {
			Object vertex = i.next();
			System.out.println(vertex + ": " + vshape.get(vertex));
		}
		System.out.println("Bye-bye.");
	}

	// sample graphs
	private static Digraph createDigraph1() {
		Digraph digraph = new MapDigraph(MapDigraph.HASHMAP_FACTORY);
		String[] vertices = new String[] { "A", "B", "C", "D", "E", "F", "G",
				"H", "J" };
		digraph.addAllVertices(Arrays.asList(vertices));
		digraph.putArc("D", "A", Boolean.TRUE);
		digraph.putArc("D", "B", Boolean.TRUE);
		digraph.putArc("F", "B", Boolean.TRUE);
		digraph.putArc("G", "C", Boolean.TRUE);
		digraph.putArc("E", "D", Boolean.TRUE);
		digraph.putArc("H", "F", Boolean.TRUE);
		digraph.putArc("F", "G", Boolean.TRUE);
		digraph.putArc("E", "H", Boolean.TRUE);
		digraph.putArc("J", "E", Boolean.TRUE);
		digraph.putArc("J", "H", Boolean.TRUE);
		digraph.putArc("J", "G", Boolean.TRUE);
		return digraph;
	}

	private static Digraph createDigraph2() {
		Digraph digraph = new MapDigraph(MapDigraph.HASHMAP_FACTORY);
		String[] vertices = new String[] { "A", "B", "C", "D", "E", "F" };
		digraph.addAllVertices(Arrays.asList(vertices));
		digraph.putArc("A", "B", Boolean.TRUE);
		digraph.putArc("A", "F", Boolean.TRUE);
		digraph.putArc("B", "C", Boolean.TRUE);
		digraph.putArc("F", "D", Boolean.TRUE);
		digraph.putArc("C", "D", Boolean.TRUE);
		digraph.putArc("E", "D", Boolean.TRUE);
		return digraph;
	}

	private static Digraph createDigraph3() {
		Digraph digraph = new MapDigraph(MapDigraph.HASHMAP_FACTORY);
		String[] vertices = new String[] { "A", "B", "C", "D", "E", "F" };
		digraph.addAllVertices(Arrays.asList(vertices));
		digraph.putArc("A", "B", Boolean.TRUE);
		digraph.putArc("A", "E", Boolean.TRUE);
		digraph.putArc("B", "F", Boolean.TRUE);
		digraph.putArc("C", "E", Boolean.TRUE);
		digraph.putArc("C", "D", Boolean.TRUE);
		digraph.putArc("E", "D", Boolean.TRUE);
		digraph.putArc("E", "B", Boolean.TRUE);
		digraph.putArc("D", "F", Boolean.TRUE);
		return digraph;
	}

	private static Digraph createDigraph4() {
		Digraph digraph = new MapDigraph(MapDigraph.HASHMAP_FACTORY);
		String[] vertices = new String[] { "A", "B", "C", "D", "E" };
		digraph.addAllVertices(Arrays.asList(vertices));
		digraph.putArc("A", "B", Boolean.TRUE);
		digraph.putArc("A", "C", Boolean.TRUE);
		digraph.putArc("B", "D", Boolean.TRUE);
		digraph.putArc("C", "E", Boolean.TRUE);
		return digraph;
	}
}