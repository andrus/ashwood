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
import java.io.Serializable;

public class MapDigraph implements Digraph, Serializable {
  public static final Factory HASHMAP_FACTORY = new HashMapFactory();
  public static final Factory TREEMAP_FACTORY = new TreeMapFactory();

  private Factory mapFactory;
  private Map graph;
  private int size;

  public MapDigraph() {
    this(null);
  }

  public MapDigraph(Factory mapFactory) {
    this.mapFactory = mapFactory != null ? mapFactory : HASHMAP_FACTORY;
    graph = createMap();
  }

  public boolean addVertex(Object vertex) {
    if (graph.containsKey(vertex)) return false;
    graph.put(vertex, createMap());
    return true;
  }
  public boolean addAllVertices(Collection vertices) {
    if (graph.keySet().containsAll(vertices)) return false;
    for (Iterator i = vertices.iterator(); i.hasNext();) addVertex(i.next());
    return true;
  }
  public Object putArc(Object origin, Object destination, Object arc) {
    Map destinations = (Map)graph.get(origin);
    if (destinations == null) {
      destinations = createMap();
      graph.put(origin, destinations);
    }
    addVertex(destination);
    Object oldArc = destinations.put(destination, arc);
    if (oldArc == null) size++;
    return oldArc;
  }
  public Object getArc(Object origin, Object destination) {
    Map destinations = (Map)graph.get(origin);
    if (destinations == null) return null;
    return destinations.get(destination);
  }
  public boolean removeVertex(Object vertex) {
    Map dst = (Map)graph.remove(vertex);
    if (dst != null) size -= dst.size();
    else return false;
    removeIncoming(vertex);
    return true;
  }
  public boolean removeAllVertices(Collection vertices) {
    boolean modified = false;
    for (Iterator i = vertices.iterator(); i.hasNext();)
      modified |= removeVertex(i.next());
    return modified;
  }
  public Object removeArc(Object origin, Object destination) {
    Map destinations = (Map)graph.get(origin);
    if (destinations == null) return null;
    Object arc = destinations.remove(destination);
    if (arc != null) size--;
    return arc;
  }
  public boolean removeIncoming(Object vertex) {
    boolean modified = false;
    for (Iterator i = graph.values().iterator(); i.hasNext();) {
      Map destinations = (Map)i.next();
      Object arc = destinations.remove(vertex);
      if (arc != null) size--;
      modified |= (arc != null);
    }
    return modified;
  }
  public boolean removeOutgoing(Object vertex) {
    Map destinations = (Map)graph.remove(vertex);
    if (destinations != null) size -= destinations.size();
    else return false;
    boolean modified = !destinations.isEmpty();
    destinations.clear();
    return modified;
  }
  public Iterator vertexIterator() {
    return graph.keySet().iterator();
  }
  public ArcIterator arcIterator() {
    return new AllArcIterator();
  }
  private class AllArcIterator implements ArcIterator {
    private Iterator orgIterator, dstIterator;
    private Object origin, nextOrigin;
    private Object dst, nextDst;
    private Object arc, nextArc;

    private AllArcIterator() {
      orgIterator = graph.entrySet().iterator();
      next();
    }
    public Object getOrigin() {
      return origin;
    }
    public Object getDestination() {
      return dst;
    }
    public boolean hasNext() {
      return nextArc != null;
    }
    public Object next() {
      origin = nextOrigin;
      dst = nextDst;
      arc = nextArc;
      if (dstIterator == null || !dstIterator.hasNext()) {
        nextOrigin = null;
        nextDst = null;
        nextArc = null;
        while (orgIterator.hasNext()) {
          Map.Entry entry = (Map.Entry)orgIterator.next();
          dstIterator = ((Map)entry.getValue()).entrySet().iterator();
          if (dstIterator.hasNext()) {
            nextOrigin = entry.getKey();
            Map.Entry entry1 = (Map.Entry)dstIterator.next();
            nextDst = entry1.getKey();
            nextArc = entry1.getValue();
            break;
          }
        }
      } else {
        Map.Entry entry1 = (Map.Entry)dstIterator.next();
        nextDst = entry1.getKey();
        nextArc = entry1.getValue();
      }
      return arc;
    }
    public void remove() {
      throw new java.lang.UnsupportedOperationException("Method remove() not yet implemented.");
    }
  }
  public ArcIterator outgoingIterator(Object vertex) {
    if (!containsVertex(vertex)) return ArcIterator.EMPTY_ITERATOR;
    return new OutgoingArcIterator(vertex);
  }
  private class OutgoingArcIterator implements ArcIterator {
    private Object origin;
    private Iterator dstIt;
    private Map.Entry entry;
    private OutgoingArcIterator(Object vertex) {
      origin = vertex;
      dstIt = ((Map)graph.get(vertex)).entrySet().iterator();
    }
    public Object getOrigin() {
      return origin;
    }
    public Object getDestination() {
      if (entry == null) return null;
      return entry.getKey();
    }
    public boolean hasNext() {
      return dstIt.hasNext();
    }
    public Object next() {
      entry = (Map.Entry)dstIt.next();
      return entry.getValue();
    }
    public void remove() {
      throw new java.lang.UnsupportedOperationException("Method remove() not yet implemented.");
    }
  }
  public ArcIterator incomingIterator(Object vertex) {
    if (!containsVertex(vertex)) return ArcIterator.EMPTY_ITERATOR;
    return new IncomingArcIterator(vertex);
  }
  private class IncomingArcIterator implements ArcIterator {
    private Object dst;
    private Object origin, nextOrigin;
    private Object arc, nextArc;
    private Iterator graphIt;

    private IncomingArcIterator(Object vertex) {
      dst = vertex;
      graphIt = graph.entrySet().iterator();
      next();
    }
    public Object getOrigin() {
      return origin;
    }
    public Object getDestination() {
      return dst;
    }
    public boolean hasNext() {
      return (nextArc != null);
    }
    public Object next() {
      origin = nextOrigin;
      arc = nextArc;
      nextArc = null;
      nextOrigin = null;
      while (graphIt.hasNext()) {
        Map.Entry entry = (Map.Entry)graphIt.next();
        Map destinations = (Map)entry.getValue();
        nextArc = destinations.get(dst);
        if (nextArc != null) {
          nextOrigin = entry.getKey();
          break;
        }
      }
      return arc;
    }
    public void remove() {
      throw new java.lang.UnsupportedOperationException("Method remove() not yet implemented.");
    }
  }
  public int order() {
    return graph.size();
  }
  public int size() {
    return size;
  }
  public int outgoingSize(Object vertex) {
    Map destinations = (Map)graph.get(vertex);
    if (destinations == null) return 0;
    else return destinations.size();
  }
  public int incomingSize(Object vertex) {
    int count = 0;
    if (!graph.containsKey(vertex)) return 0;
    for (Iterator i = graph.values().iterator(); i.hasNext();) {
      Map destinations = (Map)i.next();
      count += (destinations.containsKey(vertex) ? 1 : 0);
    }
    return count;
  }
  public boolean containsVertex(Object vertex) {
    return graph.containsKey(vertex);
  }
  public boolean containsAllVertices(Collection vertices) {
    return graph.keySet().containsAll(vertices);
  }
  public boolean hasArc(Object origin, Object destination) {
    Map destinations = (Map)graph.get(origin);
    if (destinations == null) return false;
    return destinations.containsKey(destination);
  }
  public boolean isEmpty() {
    return graph.isEmpty();
  }
  public boolean isOutgoingEmpty(Object vertex) {
    return outgoingSize(vertex) == 0;
  }
  public boolean isIncomingEmpty(Object vertex) {
    return incomingSize(vertex) == 0;
  }

  private Map createMap() {
    return (Map)mapFactory.create();
  }

  public static class HashMapFactory implements Factory, Serializable {
    public Object create() {
      return new HashMap();
    }
  }

  public static class TreeMapFactory implements Factory, Serializable {
    private Comparator comparator;
    public TreeMapFactory() {
    }
    public TreeMapFactory(Comparator mapComparator) {
      comparator = mapComparator;
    }
    public Object create() {
      if (comparator == null) return new TreeMap();
      else return new TreeMap(comparator);
    }
  }
}
