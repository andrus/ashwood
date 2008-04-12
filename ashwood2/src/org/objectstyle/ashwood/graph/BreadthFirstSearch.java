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
import org.objectstyle.ashwood.util.*;

public class BreadthFirstSearch extends Algorithm {
  private DigraphIteration factory;
  private Collection firstVertices;

  private Buffer queue = new UnboundedFifoBuffer();
  private Set seen = new HashSet();

  private Attribute level;
  private boolean validTree;

  public BreadthFirstSearch(
      DigraphIteration factory,
      Object firstVertex) {
    this(factory, firstVertex, new ConstAttribute(new Integer(-1)));
  }

  public BreadthFirstSearch(
      DigraphIteration factory,
      Object firstVertex,
      Attribute level) {
    this.factory = factory;
    reset(firstVertex, level);
  }

  public BreadthFirstSearch(
      DigraphIteration factory,
      Collection firstVertices) {
    this(factory, firstVertices, new ConstAttribute(new Integer(-1)));
  }

  public BreadthFirstSearch(
      DigraphIteration factory,
      Collection firstVertices,
      Attribute level) {
    this.factory = factory;
    reset(firstVertices, level);
  }

  public void reset(Object newFirstVertex) {
    reset(newFirstVertex, new ConstAttribute(new Integer(-1)));
  }

  public void reset(Object newFirstVertex, Attribute level) {
    reset(Collections.singletonList(newFirstVertex), level);
  }

  public void reset(Collection newFirstVertices, Attribute level) {
    if (!queue.isEmpty()) queue.clear();
    if (!seen.isEmpty()) seen.clear();
    this.firstVertices = newFirstVertices;
    this.level = level;
    queue.addAll(firstVertices);
    seen.addAll(firstVertices);
    for (Iterator i = firstVertices.iterator(); i.hasNext(); ) {
      level.set(i.next(), new Integer(0));
    }
    validTree = true;
  }

  public Attribute getLevel() {
    return level;
  }

  public boolean hasNext() {
    return !queue.isEmpty();
  }

  public Object next() {
    ArcIterator i = factory.outgoingIterator(queue.remove());
    Object origin = i.getOrigin();
    Integer childLevelValue = new Integer(level.get(origin).hashCode() + 1);
    while (i.hasNext()) {
      i.next();
      Object dst = i.getDestination();
      if (seen.add(dst)) {
        queue.add(dst);
        level.set(dst, childLevelValue);
      } else validTree = false;
    }
    return origin;
  }

  public Set getSeenVertices() {
    return Collections.unmodifiableSet(seen);
  }

  public boolean isValidTree() {
    return validTree;
  }
}
