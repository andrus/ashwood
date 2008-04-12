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

import java.util.Iterator;
import java.io.*;

public class ReversedIteration implements DigraphIteration, Serializable {
  private DigraphIteration wrappedIteration;

  public ReversedIteration(DigraphIteration wrappedIteration) {
    this.wrappedIteration = wrappedIteration;
  }
  public Iterator vertexIterator() {
    return wrappedIteration.vertexIterator();
  }
  public ArcIterator arcIterator() {
    return new ReversedArcIterator(wrappedIteration.arcIterator());
  }
  public ArcIterator outgoingIterator(Object vertex) {
    return new ReversedArcIterator(wrappedIteration.incomingIterator(vertex));
  }
  public ArcIterator incomingIterator(Object vertex) {
    return new ReversedArcIterator(wrappedIteration.outgoingIterator(vertex));
  }

  public static class ReversedArcIterator implements ArcIterator {
    private ArcIterator wrappedIterator;

    public ReversedArcIterator(ArcIterator wrappedIterator) {
      ReversedArcIterator.this.wrappedIterator = wrappedIterator;
    }

    public Object getOrigin() {
      return wrappedIterator.getDestination();
    }
    public Object getDestination() {
      return wrappedIterator.getOrigin();
    }
    public boolean hasNext() {
      return wrappedIterator.hasNext();
    }
    public Object next() {
      return wrappedIterator.next();
    }
    public void remove() {
      wrappedIterator.remove();
    }
  }
}
