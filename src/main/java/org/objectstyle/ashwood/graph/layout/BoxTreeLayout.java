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
import org.objectstyle.ashwood.graph.*;
import org.objectstyle.ashwood.util.*;

public class BoxTreeLayout extends DigraphLayout {
  private Object root;

  public BoxTreeLayout() {
  }

  public void setRoot(Object root) {
    this.root = root;
  }
  public Object getRoot() {
    return root;
  }

  public void doLayout() {
    Point2D leftUpperCorner =
        new Point2D.Double(areaBounds.getMinX(), areaBounds.getMinY());
    Map boxMap = new HashMap();

    //compute subtree box sizes
    DepthFirstStampSearch sizeTraversal = new DepthFirstStampSearch(digraph, root);
    while (sizeTraversal.hasNext()) {
      Object vertex = sizeTraversal.next();
      int stamp = sizeTraversal.getStamp();
      if (stamp == DepthFirstStampSearch.LEAF_STAMP) {
        RectangularShape leafShape = (RectangularShape)vertexShape.get(vertex);
        Rectangle2D box = new Rectangle2D.Double(
            0, 0, leafShape.getWidth(), leafShape.getHeight());
        boxMap.put(vertex, box);
      } else if (stamp == DepthFirstStampSearch.SHRINK_STAMP) {
        RectangularShape shape = (RectangularShape)vertexShape.get(vertex);
        double boxWidth = -horizontalSpacing;
        double boxHeight = 0;
        for (ArcIterator i = digraph.outgoingIterator(vertex); i.hasNext(); ) {
          i.next();
          Object child = i.getDestination();
          Rectangle2D childBox = (Rectangle2D)boxMap.get(child);
          boxWidth += childBox.getWidth() + horizontalSpacing;
          boxHeight = Math.max(boxHeight, childBox.getHeight());
        }
        boxWidth = Math.max(boxWidth, shape.getWidth());
        boxHeight += shape.getHeight() + verticalSpacing;
        Rectangle2D box = new Rectangle2D.Double(0, 0, boxWidth, boxHeight);
        boxMap.put(vertex, box);
      }
    }

    //compute subtree box locations
    Rectangle2D rootBox = (Rectangle2D)boxMap.get(root);
    rootBox.setFrame(
        leftUpperCorner.getX(),
        leftUpperCorner.getY(),
        rootBox.getWidth(),
        rootBox.getHeight());
    areaBounds.setFrame(rootBox.getBounds2D());
    BreadthFirstSearch placeTraversal = new BreadthFirstSearch(digraph, root);
    while (placeTraversal.hasNext()) {
      Object vertex = placeTraversal.next();
      Rectangle2D box = (Rectangle2D)boxMap.get(vertex);
      RectangularShape shape = (RectangularShape)vertexShape.get(vertex);
      double childBoxX = box.getMinX();
      double childBoxY = box.getMinY() + shape.getHeight() + verticalSpacing;
      for (ArcIterator i = digraph.outgoingIterator(vertex); i.hasNext(); ) {
        i.next();
        Object child = i.getDestination();
        Rectangle2D childBox = (Rectangle2D)boxMap.get(child);
        childBox.setFrame(
            childBoxX, childBoxY, childBox.getWidth(), childBox.getHeight());
        childBoxX += childBox.getWidth() + horizontalSpacing;
      }
    }

    //compute vertex locations
    DepthFirstStampSearch vertexLocationTraversal = new DepthFirstStampSearch(digraph, root);
    while (vertexLocationTraversal.hasNext()) {
      Object vertex = vertexLocationTraversal.next();
      int stamp = vertexLocationTraversal.getStamp();
      if (stamp == DepthFirstStampSearch.LEAF_STAMP) {
        RectangularShape leafShape = (RectangularShape)vertexShape.get(vertex);
        Rectangle2D leafBox = (Rectangle2D)boxMap.get(vertex);
        leafShape.setFrame(
            leafBox.getMinX(),
            leafBox.getMinY(),
            leafShape.getWidth(),
            leafShape.getHeight());
      } else if (stamp == DepthFirstStampSearch.SHRINK_STAMP) {
        RectangularShape shape = (RectangularShape)vertexShape.get(vertex);
        Rectangle2D box = (Rectangle2D)boxMap.get(vertex);
        double centerX = 0;
        int childCount = 0;
        for (ArcIterator i = digraph.outgoingIterator(vertex); i.hasNext(); ) {
          i.next();
          childCount++;
          Object child = i.getDestination();
          RectangularShape childShape = (RectangularShape)vertexShape.get(child);
          centerX += childShape.getCenterX();
        }
        centerX /= childCount;
        shape.setFrame(
            centerX - shape.getWidth() / 2,
            box.getMinY(),
            shape.getWidth(),
            shape.getHeight());
      }
    }
  }

  public static void main(String[] args) {
    //init
    Digraph digraph = new MapDigraph(MapDigraph.HASHMAP_FACTORY);
    Random randomizer = new Random(100);
    GraphUtils.randomizeTree(digraph, 3, 4, randomizer);
    Map vertexShapeMap = new HashMap();
    Point2D leftUpperCorner = new Point2D.Double(30, 30);
    RectangularShape shapePattern = new Rectangle2D.Double(0, 0, 15, 10);
    double horizontalSpacing = 5;
    double verticalSpacing = 5;
    Object root = null;
    for (Iterator i = digraph.vertexIterator(); i.hasNext(); ) {
      Object vertex = i.next();
      vertexShapeMap.put(vertex, shapePattern.clone());
      if (root == null && digraph.incomingSize(vertex) == 0) root = vertex;
    }
    Attribute vertexShape = new MapAttribute(vertexShapeMap);

    //go-go
    BoxTreeLayout boxTreeLayout = new BoxTreeLayout();
    boxTreeLayout.setDigraph(digraph);
    boxTreeLayout.setRoot(root);
    boxTreeLayout.setVertexShape(vertexShape);
    boxTreeLayout.setHorizontalSpacing(horizontalSpacing);
    boxTreeLayout.setVerticalSpacing(verticalSpacing);
    boxTreeLayout.setAreaBounds(new Rectangle2D.Double(30, 30, 1000, 1000));
    boxTreeLayout.doLayout();

    //print everything
    System.out.println("Tree");
    System.out.println("Tree bounds: " + boxTreeLayout.getAreaBounds());
    RectangularShape shape = (RectangularShape)vertexShape.get(root);
    System.out.println(root + "[" + shape.getCenterX() + "," + shape.getMinY() + "]");
    BreadthFirstSearch printTraversal = new BreadthFirstSearch(digraph, root);
    while (printTraversal.hasNext()) {
      Object vertex = printTraversal.next();
      for (ArcIterator i = digraph.outgoingIterator(vertex); i.hasNext(); ) {
        i.next();
        Object child = i.getDestination();
        shape = (RectangularShape)vertexShape.get(child);
        System.out.print("(" + vertex + "->" + child + ")[" + shape.getCenterX() + "," + shape.getMinY() + "]");
      }
      if (digraph.outgoingSize(vertex) != 0)
        System.out.println();
    }
    System.out.println("Bye-bye!");
  }
}