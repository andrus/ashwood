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

public abstract class DigraphLayout {
  protected Digraph digraph;
  protected Attribute vertexShape;
  protected Digraph arcGeometry;
  protected Rectangle2D areaBounds =
      new Rectangle2D.Double(0, 0, 100000, 100000);
  protected double horizontalSpacing = 50;
  protected double verticalSpacing = 50;
  protected int verticalAligment = LayoutConstants.CENTER;

  public abstract void doLayout();

  public void setDigraph(Digraph digraph) {
    this.digraph = digraph;
  }
  public Digraph getDigraph() {
    return digraph;
  }

  public void setVertexShape(Attribute vertexShape) {
    this.vertexShape = vertexShape;
  }
  public Attribute getVertexShape() {
    return vertexShape;
  }

  public void setAreaBounds(Rectangle2D areaBounds) {
    this.areaBounds = areaBounds;
  }
  public Rectangle2D getAreaBounds() {
    return areaBounds;
  }

  public void setHorizontalSpacing(double horizontalSpacing) {
    this.horizontalSpacing = horizontalSpacing;
  }
  public double getHorizontalSpacing() {
    return horizontalSpacing;
  }
  public void setVerticalSpacing(double verticalSpacing) {
    this.verticalSpacing = verticalSpacing;
  }
  public double getVerticalSpacing() {
    return verticalSpacing;
  }
  public int getVerticalAligment() {
    return verticalAligment;
  }
  public void setVerticalAligment(int verticalAligment) {
    this.verticalAligment = verticalAligment;
  }
  public Digraph getArcGeometry() {
    return arcGeometry;
  }
}