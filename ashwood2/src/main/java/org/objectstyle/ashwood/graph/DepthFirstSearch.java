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

public class DepthFirstSearch extends Algorithm {
  protected DigraphIteration factory;
  protected Object firstVertex;

  protected ArrayStack stack = new ArrayStack();
  protected Set seen = new HashSet();

  public DepthFirstSearch(DigraphIteration factory, Object firstVertex) {
    this.factory = factory;
    this.firstVertex = firstVertex;
    stack.push(factory.outgoingIterator(firstVertex));
    seen.add(firstVertex);
  }

  public void reset(Object newFirstVertex) {
    stack.clear();
    seen.clear();
    firstVertex = newFirstVertex;
    stack.push(factory.outgoingIterator(firstVertex));
    seen.add(firstVertex);
  }

  public boolean hasNext() {
    return !stack.isEmpty();
  }
  public Object next() {
    ArcIterator i = (ArcIterator)stack.pop();
    Object origin = i.getOrigin();
    while (i.hasNext()) {
      i.next();
      //origin = i.getOrigin();
      Object dst = i.getDestination();
      if (seen.add(dst)) stack.push(factory.outgoingIterator(dst));
    }
    return origin;
  }
}
