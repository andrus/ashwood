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
import org.apache.commons.collections.comparators.*;
import org.apache.commons.collections.*;

public class DepthFirstStampSearch extends DepthFirstSearch {
  public static final int UNDEFINED_STAMP = -1;
  public static final int GROW_DEPTH_STAMP = 0;
  public static final int GROW_BREADTH_STAMP = 1;
  public static final int SHRINK_STAMP = 2;
  public static final int LEAF_STAMP = 3;

  private int stamp = UNDEFINED_STAMP;

  public DepthFirstStampSearch(DigraphIteration factory, Object firstVertex) {
    super(factory, firstVertex);
  }

  public int getStamp() {
    return stamp;
  }

  public Object next() {
    ArcIterator i = (ArcIterator)stack.peek();
    Object origin = i.getOrigin();
    Object dst = i.getDestination();
    if (dst == null) {
      if (i.hasNext()) {
        i.next();
        dst = i.getDestination();
      } else {
        stack.pop();
        //shrink
        stamp = LEAF_STAMP;
        return origin;
      }
    }
    if (seen.add(dst)) {
      stack.push(factory.outgoingIterator(dst));
      //grow depth
      stamp = GROW_DEPTH_STAMP;
      if (i.hasNext()) i.next();
    }
    else {
      if (i.hasNext()) {
        i.next();
        //grow breadth
        stamp = GROW_BREADTH_STAMP;
      } else {
        stack.pop();
        //shrink
        stamp = SHRINK_STAMP;
      }
    }
    return origin;
  }

  public Map traverse(Map orders) {
    int preOrder = 0;
    int postOrder = 0;
    while (hasNext()) {
      Object vertex = next();
      int stamp = getStamp();
      if (stamp == SHRINK_STAMP) {
        postOrder++;
        OrderPair pair = (OrderPair)orders.get(vertex);
        if (pair == null) {
          preOrder++;
          orders.put(vertex, new OrderPair(preOrder, postOrder));
        } else pair.postOrder = postOrder;
      } else if (stamp == LEAF_STAMP) {
        preOrder++;
        postOrder++;
        orders.put(vertex, new OrderPair(preOrder, postOrder));
      } else if (!orders.containsKey(vertex)) {
        preOrder++;
        orders.put(vertex, new OrderPair(preOrder, -1));
      }
    }
    return orders;
  }

  public static class OrderPair {
    private int preOrder;
    private int postOrder;

    public OrderPair(int preOrder, int postOrder) {
      this.preOrder = preOrder;
      this.postOrder = postOrder;
    }

    public int getPreOrder() {
      return preOrder;
    }
    public int getPostOrder() {
      return postOrder;
    }

    public String toString() {
      return "(" + preOrder + ", " + postOrder + ")";
    }
  }
}
