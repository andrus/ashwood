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
import java.io.*;
import org.apache.commons.collections.*;

public class FilterIteration implements DigraphIteration, Serializable {
  private DigraphIteration digraph;
  private Predicate acceptVertex;
  private Predicate acceptArc;

  public FilterIteration(DigraphIteration digraph, Predicate acceptVertex, Predicate acceptArc) {
    this.digraph = digraph;
    this.acceptVertex = acceptVertex;
    this.acceptArc = acceptArc;
  }
  public Iterator vertexIterator() {
    return IteratorUtils.filteredIterator(digraph.vertexIterator(), acceptVertex);
  }
  public ArcIterator arcIterator() {
    return new FilterArcIterator(digraph.arcIterator(), acceptVertex, acceptVertex, acceptArc);
  }
  public ArcIterator outgoingIterator(Object vertex) {
    if (!acceptVertex.evaluate(vertex)) return ArcIterator.EMPTY_ITERATOR;
    return new FilterArcIterator(digraph.outgoingIterator(vertex), GraphUtils.TRUE_PREDICATE, acceptVertex, acceptArc);
  }
  public ArcIterator incomingIterator(Object vertex) {
    if (!acceptVertex.evaluate(vertex)) return ArcIterator.EMPTY_ITERATOR;
    return new FilterArcIterator(digraph.incomingIterator(vertex), acceptVertex, GraphUtils.TRUE_PREDICATE, acceptArc);
  }
}
