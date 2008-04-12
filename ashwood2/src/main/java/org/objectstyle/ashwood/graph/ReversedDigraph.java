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

import java.util.Collection;
import java.util.Iterator;
import java.io.Serializable;

public class ReversedDigraph implements Digraph, Serializable {
  private Digraph digraph;
  private ReversedIteration reversedIteration;

  public ReversedDigraph(Digraph digraph) {
    this.digraph = digraph;
    reversedIteration = new ReversedIteration(digraph);
  }

  public boolean addVertex(Object vertex) {
    return digraph.addVertex(vertex);
  }

  public boolean addAllVertices(Collection vertices) {
    return digraph.addAllVertices(vertices);
  }

  public Object putArc(Object origin, Object destination, Object arc) {
    return digraph.putArc(destination, origin, arc);
  }

  public Object getArc(Object origin, Object destination) {
    return digraph.getArc(destination, origin);
  }

  public boolean removeVertex(Object vertex) {
    return digraph.removeVertex(vertex);
  }

  public boolean removeAllVertices(Collection vertices) {
    return digraph.removeAllVertices(vertices);
  }

  public Object removeArc(Object origin, Object destination) {
    return digraph.removeArc(destination, origin);
  }

  public boolean removeIncoming(Object vertex) {
    return digraph.removeOutgoing(vertex);
  }

  public boolean removeOutgoing(Object vertex) {
    return digraph.removeIncoming(vertex);
  }

  public int order() {
    return digraph.order();
  }

  public int size() {
    return digraph.size();
  }

  public int outgoingSize(Object vertex) {
    return digraph.incomingSize(vertex);
  }

  public int incomingSize(Object vertex) {
    return digraph.outgoingSize(vertex);
  }

  public boolean containsVertex(Object vertex) {
    return digraph.containsVertex(vertex);
  }

  public boolean containsAllVertices(Collection vertices) {
    return digraph.containsAllVertices(vertices);
  }

  public boolean hasArc(Object origin, Object destination) {
    return digraph.hasArc(destination, origin);
  }

  public boolean isEmpty() {
    return digraph.isEmpty();
  }

  public boolean isOutgoingEmpty(Object vertex) {
    return digraph.isIncomingEmpty(vertex);
  }

  public boolean isIncomingEmpty(Object vertex) {
    return digraph.isOutgoingEmpty(vertex);
  }

  public Iterator vertexIterator() {
    return reversedIteration.vertexIterator();
  }

  public ArcIterator arcIterator() {
    return reversedIteration.arcIterator();
  }

  public ArcIterator outgoingIterator(Object vertex) {
    return reversedIteration.outgoingIterator(vertex);
  }

  public ArcIterator incomingIterator(Object vertex) {
    return reversedIteration.incomingIterator(vertex);
  }
}