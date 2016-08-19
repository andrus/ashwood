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
import org.objectstyle.ashwood.*;

public class InOutBreadthFirstSearch extends Algorithm {
  private DigraphIteration factory;
  private Object firstVertex;

  private Buffer queue = new UnboundedFifoBuffer();
  private Set seen = new HashSet();

  public InOutBreadthFirstSearch(
      DigraphIteration factory,
      Object firstVertex) {
    this.factory = factory;
    reset(firstVertex);
  }

  public void reset(Object newFirstVertex) {
    if (!queue.isEmpty()) queue.clear();
    if (!seen.isEmpty()) seen.clear();
    this.firstVertex = newFirstVertex;
    queue.add(firstVertex);
    seen.add(firstVertex);
  }

  public boolean hasNext() {
    return !queue.isEmpty();
  }

  public Object next() {
    Object currentVertex = queue.remove();
    ArcIterator i = factory.outgoingIterator(currentVertex);
    Object origin = i.getOrigin();
    while (i.hasNext()) {
      i.next();
      Object dst = i.getDestination();
      if (seen.add(dst)) queue.add(dst);
    }
    i = factory.incomingIterator(currentVertex);
    while (i.hasNext()) {
      i.next();
      Object org = i.getOrigin();
      if (seen.add(org)) queue.add(org);
    }
    return origin;
  }

  public Set getSeenVertices() {
    return Collections.unmodifiableSet(seen);
  }
}
