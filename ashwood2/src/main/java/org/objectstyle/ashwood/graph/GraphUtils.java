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
import org.apache.commons.collections.iterators.TransformIterator;
import org.objectstyle.ashwood.predicate.*;
import org.objectstyle.ashwood.util.*;

public class GraphUtils {
  public static final Predicate TRUE_PREDICATE = ConstPredicate.TRUE;
  public static final Predicate FALSE_PREDICATE = ConstPredicate.FALSE;

  private GraphUtils() {
  }

  public static boolean isConnected(DigraphIteration digraph, Object firstVertex, int order) {
    Algorithm dfs = new DepthFirstSearch(digraph, firstVertex);
    return order == traverse(dfs);
  }

  public static boolean isConnected(Digraph digraph) {
    return isConnected(digraph, digraph.vertexIterator().next(), digraph.order());
  }

  public static boolean isStronglyConnected(DigraphIteration digraph, Object firstVertex, int order) {
    return (isConnected(digraph, firstVertex, order) &&
            isConnected(new ReversedIteration(digraph), firstVertex, order));
  }

  public static boolean isStronglyConnected(Digraph digraph) {
    return isStronglyConnected(digraph, digraph.vertexIterator().next(), digraph.order());
  }

  public static int traverse(Iterator iterator) {
    int count = 0;
    while (iterator.hasNext()) {
      iterator.next();
      count++;
    }
    return count;
  }

  public static boolean hasLoops(Digraph digraph) {
    return (traverse(new LoopSearch(digraph)) != 0);
  }
  public static boolean isAcyclic(Digraph digraph) {
    int order = digraph.order();
    if (order == 0) return true;
    Set spanned = new HashSet(order);
    DepthFirstStampSearch dfs = new DepthFirstStampSearch(digraph, digraph.vertexIterator().next());
    for (Iterator i = digraph.vertexIterator(); i.hasNext();) {
      Object dfsRoot = i.next();
      if (spanned.contains(dfsRoot)) continue;
      dfs.reset(dfsRoot);
      Map dfsOrders = dfs.traverse(new HashMap(digraph.order()));
      for (Iterator j = dfsOrders.entrySet().iterator(); j.hasNext();) {
        Map.Entry entry = (Map.Entry)j.next();
        Object origin = entry.getKey();
        DepthFirstStampSearch.OrderPair orgOrders = (DepthFirstStampSearch.OrderPair)entry.getValue();
        spanned.add(origin);
        for (ArcIterator k = digraph.outgoingIterator(origin); k.hasNext();) {
          k.next();
          Object dst = k.getDestination();
          DepthFirstStampSearch.OrderPair dstOrders = (DepthFirstStampSearch.OrderPair)dfsOrders.get(dst);
          if (dstOrders.getPostOrder() > orgOrders.getPostOrder()) return false;
        }
      }
      if (dfsOrders.size() == order) break;
    }
    return true;
  }

  public static Digraph randomize(Digraph digraph, int order, int size, Random randomizer) {
    for (int i = 1; i <= order; i++) digraph.addVertex(new Integer(i));
    Random random = randomizer;
    int n_2 = order*order;
    size = Math.min(size, n_2);
    for (int arc = 1; arc <= size; arc++) {
      int arcCode = random.nextInt(n_2);
      int origin = arcCode / order + 1;
      int dst = arcCode % order + 1;
      digraph.putArc(new Integer(origin), new Integer(dst), new Integer(arc));
    }
    return digraph;
  }

  public static Digraph randomizeAcyclic(Digraph digraph, int order, int incomingSize, int outgoingSize, Random randomizer) {
    Random random = randomizer;
    int arc = 1;
    for (int i = 1; i <= order; i++) {
      Integer destination = new Integer(i);
      digraph.addVertex(destination);
      for (int j = 0; j < incomingSize; j++) {
        int org = random.nextInt(i);
        if (org == 0) continue;
        Integer origin = new Integer(org);
        if (digraph.outgoingSize(origin) >= outgoingSize) continue;
        digraph.putArc(origin, destination, new Integer(arc++));
      }
    }
    return digraph;
  }

  public static Digraph randomizeTree(Digraph digraph, int maxChildren, int maxLevels, Random randomizer) {
    int vertexIndex = 1;
    Object root = new Integer(vertexIndex);
    List level = Collections.singletonList(root);
    digraph.addVertex(root);
    for (int i = 1; i < maxLevels; i++) {
      List childLevel = new ArrayList(level.size() * maxChildren);
      for (Iterator j = level.iterator(); j.hasNext(); ) {
        Object parent = j.next();
        int childCount = randomizer.nextInt(maxChildren + 1);
        for (int k = 0; k < childCount; k++) {
          Object child = new Integer(++vertexIndex);
          digraph.addVertex(child);
          digraph.putArc(parent, child, Boolean.TRUE);
          childLevel.add(child);
        }
      }
      if (childLevel.isEmpty()) break;
      level = childLevel;
    }
    return digraph;
  }

  public static Digraph transform(Digraph result,
                                  DigraphIteration source,
                                  Transformer vertexTransform,
                                  Transformer arcTransform) {
    for (Iterator i = new TransformIterator(source.vertexIterator(), vertexTransform);
         i.hasNext();) {
      result.addVertex(i.next());
    }
    for (ArcIterator i = new TransformArcIterator(source.arcIterator(), vertexTransform, arcTransform);
         i.hasNext();) {
      Object arc = i.next();
      Object origin = i.getOrigin();
      Object dst = i.getDestination();
      result.putArc(origin, dst, arc);
    }
    return result;
  }

  public static Digraph merge(Digraph destination,DigraphIteration graphToMerge) {
    for (Iterator i = graphToMerge.vertexIterator(); i.hasNext();) {
      destination.addVertex(i.next());
    }
    for (ArcIterator i = graphToMerge.arcIterator(); i.hasNext();) {
      Object arc = i.next();
      Object origin = i.getOrigin();
      Object dst = i.getDestination();
      destination.putArc(origin, dst, arc);
    }
    return destination;
  }

  public static Map computeLevels(Map vertexLevelMap,
                                  Digraph digraph,
                                  boolean longest) {
    if (vertexLevelMap == null)
      vertexLevelMap = new HashMap(digraph.order());

    for (Iterator i = digraph.vertexIterator(); i.hasNext(); ) {
      Object rootCandidate = i.next();
      if (digraph.incomingSize(rootCandidate) == 0)
        computeLevels(vertexLevelMap,
                      digraph,
                      rootCandidate,
                      longest);
    }

    return vertexLevelMap;
  }

  public static Map computeLevels(Map vertexLevelMap,
                                  DigraphIteration digraph,
                                  Object root,
                                  boolean longest) {
    if (vertexLevelMap == null)
      vertexLevelMap = new HashMap();

    MutableInteger rootLevel = (MutableInteger)vertexLevelMap.get(root);
    if (rootLevel == null) {
      rootLevel = new MutableInteger(0);
      vertexLevelMap.put(root, rootLevel);
    }

    for (ArcIterator i = digraph.outgoingIterator(root); i.hasNext(); ) {
      i.next();
      Object child = i.getDestination();
      int childLevelCandidate = rootLevel.intValue()+1;
      MutableInteger childLevel = (MutableInteger)vertexLevelMap.get(child);
      if (childLevel == null) {
        childLevel = new MutableInteger(childLevelCandidate);
        vertexLevelMap.put(child, childLevel);
        computeLevels(vertexLevelMap, digraph, child, longest);
      } else if ((longest && childLevel.intValue() < childLevelCandidate) ||
                 (!longest && childLevel.intValue() > childLevelCandidate)) {
        childLevel.setValue(childLevelCandidate);
        computeLevels(vertexLevelMap, digraph, child, longest);
      }
    }

    return vertexLevelMap;
  }

  public static Map shiftLevelsDown(Map vertexLevelMap, Digraph digraph) {
    for (Iterator i = digraph.vertexIterator(); i.hasNext(); ) {
      Object rootCandidate = i.next();
      if (digraph.incomingSize(rootCandidate) == 0)
        shiftLevelsDown(vertexLevelMap, digraph, rootCandidate);
    }

    return vertexLevelMap;
  }

  public static Map shiftLevelsDown(Map vertexLevelMap,
                                    DigraphIteration digraph,
                                    Object root) {
    int minChildLevel = Integer.MAX_VALUE;
    for (ArcIterator i = digraph.outgoingIterator(root); i.hasNext(); ) {
      i.next();
      Object child = i.getDestination();
      shiftLevelsDown(vertexLevelMap, digraph, child);
      MutableInteger childLevel = (MutableInteger)vertexLevelMap.get(child);
      minChildLevel = (minChildLevel <= childLevel.intValue() ?
                       minChildLevel :
                       childLevel.intValue());
    }

    if (minChildLevel != Integer.MAX_VALUE) {
      MutableInteger rootLevel = (MutableInteger)vertexLevelMap.get(root);
      rootLevel.setValue(minChildLevel - 1);
    }

    return vertexLevelMap;
  }

  public static boolean isTree(Digraph digraph) {
    Object root = null;
    for (Iterator i = digraph.vertexIterator(); i.hasNext(); ) {
      Object vertex = i.next();
      int inSize = digraph.incomingSize(vertex);
      if (inSize == 0) {
        root = vertex;
        break;
      }
    }

    //not a tree - no vertex with 0 in-degree
    if (root == null) return false;

    //try to reach all vertices from the root candidate
    BreadthFirstSearch traversal = new BreadthFirstSearch(digraph, root);
    while (traversal.isValidTree() && traversal.hasNext())
      traversal.next();

    //not a tree - one of vertices has been seen more than once by the BFS
    if (!traversal.isValidTree()) return false;

    //has every vertex been reached?
    Set seenVertices = traversal.getSeenVertices();
    for (Iterator i = digraph.vertexIterator(); i.hasNext(); )
      if (!seenVertices.contains(i.next())) return false;

    //all tests are passed - good!
    return true;
  }

  public static List findCycles(DigraphIteration graph) {
    ArrayStack stack = new ArrayStack();
    ArrayStack path = new ArrayStack();
    Set seen = new HashSet();
    List cycles = new ArrayList();
    Iterator vertexIterator = graph.vertexIterator();

    while (vertexIterator.hasNext()) {
      while (vertexIterator.hasNext()) {
        Object vertex = vertexIterator.next();
        if (seen.add(vertex)) {
          stack.push(graph.outgoingIterator(vertex));
          path.push(vertex);
          break;
        }
      }

      while (!stack.isEmpty()) {
        ArcIterator i = (ArcIterator)stack.peek();
        Object origin = i.getOrigin();
        boolean subtreeIsTraversed = true;
        while (i.hasNext()) {
          i.next();
          Object dst = i.getDestination();
          int index = path.indexOf(dst);
          if (index < 0) {
            seen.add(dst);
            stack.push(graph.outgoingIterator(dst));
            path.push(dst);
            subtreeIsTraversed = false;
            break;
          } else {
            cycles.add(new ArrayList(path.subList(index, path.size())));
          }
        }
        if (subtreeIsTraversed) {
          stack.pop();
          path.pop();
        }
      }
    }
    return cycles;
  }
}
