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

public class CobbleLayout extends DigraphLayout {
  private boolean enforceWidth = true;

  public void doLayout() {
    double minX = areaBounds.getMinX();
    double minY = areaBounds.getMinY();
    double maxX = areaBounds.getMaxX();
    double maxY = areaBounds.getMaxY();

    double x = minX;
    double y = minY;
    double levelWidth = 0;
    double levelHeight = 0;
    boolean first = true;
    for (Iterator i = digraph.vertexIterator(); i.hasNext(); ) {
      Object vertex = i.next();
      RectangularShape shape = (RectangularShape)vertexShape.get(vertex);
      double width = shape.getWidth();
      double height = shape.getHeight();
      shape.setFrame(x, y, width, height);
      if (enforceWidth) {
        if (!first && shape.getMaxX() > maxX) {
          x = minX;
          y += verticalSpacing + levelHeight;
          levelHeight = height;
          shape.setFrame(x, y, width, height);
        } else {
          x += horizontalSpacing + width;
          levelHeight = Math.max(levelHeight, height);
        }
      } else {
        if (!first && y > maxY) {
          y = minY;
          x += horizontalSpacing + levelWidth;
          levelWidth = width;
          shape.setFrame(x, y, width, height);
        } else {
          y += verticalSpacing + height;
          levelWidth = Math.max(levelWidth, width);
        }
      }
      first = false;
    }
  }

  public void setEnforceWidth(boolean enforceWidth) {
    this.enforceWidth = enforceWidth;
  }

  public boolean isEnforceWidth() {
    return enforceWidth;
  }
}