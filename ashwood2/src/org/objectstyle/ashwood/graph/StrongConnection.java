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

public class StrongConnection extends Algorithm {
  private DigraphIteration digraph;
  private DigraphIteration reverseDigraph;
  private DigraphIteration filteredDigraph;
  private DepthFirstStampSearch directDfs;
  private DepthFirstSearch reverseDfs;
  private Set seen = new HashSet();
  private Iterator vertexIterator;
  private ArrayStack dfsStack = new ArrayStack();
  private DFSSeenVerticesPredicate reverseDFSFilter = new DFSSeenVerticesPredicate();
  private CollectionFactory componentFactory;


  public StrongConnection(DigraphIteration digraph, CollectionFactory componentFactory) {
    this.digraph = digraph;
    this.componentFactory = componentFactory;
    filteredDigraph = new FilterIteration(digraph, new NotSeenPredicate(), GraphUtils.TRUE_PREDICATE);
    reverseDigraph = new FilterIteration(new ReversedIteration(digraph), reverseDFSFilter, GraphUtils.TRUE_PREDICATE);
    vertexIterator = filteredDigraph.vertexIterator();
    runDirectDFS();
  }
  public boolean hasNext() {
    return !dfsStack.isEmpty();
  }
  public Object next() {
    Collection component = buildStronglyConnectedComponent();
    if (dfsStack.isEmpty()) runDirectDFS();
    return component;
  }

  public Digraph contract(Digraph contractedDigraph) {
    return contract(contractedDigraph, null);
  }

  public Digraph contract(Digraph contractedDigraph, CollectionFactory arcFactory) {
    List components = new ArrayList();
    CollectionUtils.addAll(components, this);
    Map memberToComponent = new HashMap();
    for (Iterator i = components.iterator(); i.hasNext();) {
      Collection c = (Collection)i.next();
      for (Iterator j = c.iterator(); j.hasNext();)
        memberToComponent.put(j.next(), c);
    }
    for (Iterator i = components.iterator(); i.hasNext();) {
      Collection origin = (Collection)i.next();
      contractedDigraph.addVertex(origin);
      for (Iterator j = origin.iterator(); j.hasNext();) {
        Object member = j.next();
        for (ArcIterator k = digraph.outgoingIterator(member); k.hasNext();) {
          Object arc = k.next();
          Object dst = k.getDestination();
          if (origin.contains(dst)) continue;
          Collection destination = (Collection)memberToComponent.get(dst);
          if (arcFactory == null)
            contractedDigraph.putArc(origin, destination, arc);
          else {
            Collection contractedArc = (Collection)contractedDigraph.getArc(origin, destination);
            if (contractedArc == null) {
              contractedArc = arcFactory.createSingleton(arc);
              contractedDigraph.putArc(origin, destination, contractedArc);
            } else {
              if (contractedArc.size() == 1) {
                Collection tmp = contractedArc;
                contractedArc = arcFactory.create();
                contractedArc.addAll(tmp);
                contractedDigraph.putArc(origin, destination, contractedArc);
              }
              contractedArc.add(arc);
            }
          }
        }
      }
    }
    return contractedDigraph;
  }

  private Object nextDFSRoot() {
    /*while (vertexIterator.hasNext()) {
      Object vertex = vertexIterator.next();
      if (!seen.contains(vertex)) return vertex;
    }
    return null;*/
    return (vertexIterator.hasNext() ? vertexIterator.next() : null);
  }

  private boolean runDirectDFS() {
    dfsStack.clear();
    reverseDFSFilter.seenVertices.clear();
    Object root = nextDFSRoot();
    if (root == null) return false;
    if (directDfs == null) directDfs = new DepthFirstStampSearch(filteredDigraph, root);
    else directDfs.reset(root);
    int stamp;
    Object vertex;
    while (directDfs.hasNext()) {
      vertex = directDfs.next();
      stamp = directDfs.getStamp();
      if (stamp == DepthFirstStampSearch.SHRINK_STAMP ||
          stamp == DepthFirstStampSearch.LEAF_STAMP) {
        //if (seen.add(vertex)) {
          dfsStack.push(vertex);
          reverseDFSFilter.seenVertices.add(vertex);
        //}
      }
    }
    seen.addAll(dfsStack);
    return true;
  }

  private Collection buildStronglyConnectedComponent() {
    Object root = dfsStack.pop();
    Collection component = componentFactory.createSingleton(root);
    boolean singleton = true;
    if (reverseDfs == null) reverseDfs = new DepthFirstSearch(reverseDigraph, root);
    else reverseDfs.reset(root);
    while (reverseDfs.hasNext()) {
      Object vertex = reverseDfs.next();
      if (vertex != root) {
        if (singleton) {
          Collection tmp = component;
          component = componentFactory.create();
          component.addAll(tmp);
          singleton = false;
        }
        component.add(vertex);
        dfsStack.remove(vertex);
      }
    }
    reverseDFSFilter.seenVertices.removeAll(component);
    return component;
  }

  private class DFSSeenVerticesPredicate implements Predicate {
    private Set seenVertices = new HashSet();

    public boolean evaluate(Object vertex) {
      return seenVertices.contains(vertex);
    }
  }

  private class NotSeenPredicate implements Predicate {
    public boolean evaluate(Object vertex) {
      return !seen.contains(vertex);
    }
  }
}
