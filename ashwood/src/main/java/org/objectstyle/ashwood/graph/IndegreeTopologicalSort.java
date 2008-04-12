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

package org.objectstyle.ashwood.graph;

import java.util.*;
import org.apache.commons.collections.*;

public class IndegreeTopologicalSort extends Algorithm {
  private Digraph digraph;
  private LinkedList vertices = new LinkedList();
  private Map inDegrees = new HashMap();
  private ListIterator current;

  public IndegreeTopologicalSort(Digraph digraph) {
    this.digraph = digraph;
    for (Iterator i = digraph.vertexIterator(); i.hasNext();) {
      Object vertex = i.next();
      vertices.add(vertex);
      inDegrees.put(vertex, new InDegree(digraph.incomingSize(vertex)));
    }
    current = vertices.listIterator();
  }

  public boolean hasNext() {
    return !vertices.isEmpty();
  }

  public Object next() {
    boolean progress = true;
    while (hasNext()) {
      if (!current.hasNext()) {
        if (!progress) break;
        progress = false;
        current = vertices.listIterator();
      }
      Object vertex = current.next();
      InDegree indegree = (InDegree)inDegrees.get(vertex);
      if (indegree.value == 0) {
        removeVertex(vertex);
        current.remove();
        return vertex;
      }
    }
    return null;
  }

  private void removeVertex(Object vertex) {
    for (ArcIterator i = digraph.outgoingIterator(vertex); i.hasNext();) {
      i.next();
      Object dst = i.getDestination();
      InDegree indegree = (InDegree)inDegrees.get(dst);
      indegree.value--;
    }
  }

  private static class InDegree {
    int value;
    InDegree(int value) {
      InDegree.this.value = value;
    }
  }
}
