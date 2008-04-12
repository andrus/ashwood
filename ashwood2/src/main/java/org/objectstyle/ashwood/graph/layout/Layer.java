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

public class Layer extends ArrayList {
  private int rank;

  public Layer() {
  }
  public int getRank() {
    return rank;
  }
  public void setRank(int rank) {
    this.rank = rank;
  }

  public LayerVertex getVertex(int index) {

    return (LayerVertex)get(index);
  }

  public Object getUserVertex(int index) {
    return getVertex(index).getUserVertex();
  }

  public void sort() {
    Collections.sort(this, LayerVertex.LAYER_INDEX_COMPARATOR);
    boolean indicesCorrect = checkIndices();
    if (!indicesCorrect) {
      System.out.println("indices failed: rank=" + rank);
      for (int i = 0; i < size(); i++) {
        LayerVertex v = getVertex(i);
        String sgLabel = (v.getParentSubgraph() != null ?
                          v.getParentSubgraph().getLabel() :
                          "?");
        System.out.print(sgLabel + "." + v + "{" + v.getIndexInLayer() + "}, ");
      }
      System.out.println();
    }
//    refreshIndices();
  }

  public void sort(Comparator comparator) {
    Collections.sort(this, comparator);
    refreshIndices();
  }

  public void refreshIndices() {
    for (int i = 0; i < size(); i++) {
      getVertex(i).setIndexInLayer(i);
    }
  }

  public boolean checkIndices() {
    for (int i = 0; i < size(); i++) {
      if (getVertex(i).getIndexInLayer() != i)
        return false;
    }
    return true;
  }

  public boolean add(LayerVertex vertex) {
    vertex.setIndexInLayer(size());
    vertex.setRank(rank);
    return super.add(vertex);
  }

  public boolean add(Object vertex) {
    return add((LayerVertex)vertex);
  }

  public LayerVertex getLeft(LayerVertex vertex) {
    int index = vertex.getIndexInLayer();
    return (index > 0 ? getVertex(index - 1) : null);
  }

  public LayerVertex getRight(LayerVertex vertex) {
    int index = vertex.getIndexInLayer();
    return (index < size() - 1 ? getVertex(index + 1) : null);
  }

  public boolean isFirst(LayerVertex vertex) {
    return vertex.getIndexInLayer() == 0;
  }

  public boolean isLast(LayerVertex vertex) {
    return vertex.getIndexInLayer() == size() - 1;
  }
}