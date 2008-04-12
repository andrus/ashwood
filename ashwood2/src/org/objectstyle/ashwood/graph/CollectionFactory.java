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

public interface CollectionFactory {
  public static final CollectionFactory ARRAYLIST_FACTORY = new ArrayListFactory();
  public static final CollectionFactory LINKEDLIST_FACTORY = new LinkedListFactory();
  public static final CollectionFactory HASHSET_FACTORY = new HashSetFactory();
  public static final CollectionFactory TREESET_FACTORY = new TreeSetFactory();

  Collection create();
  Collection createSingleton(Object contents);
  Collection createEmpty();

  public static class ArrayListFactory implements CollectionFactory, Serializable {
    public Collection create() {
      return new ArrayList();
    }
    public Collection createSingleton(Object contents) {
      return Collections.singletonList(contents);
    }
    public Collection createEmpty() {
      return Collections.EMPTY_LIST;
    }
  }
  public static class LinkedListFactory extends ArrayListFactory {
    public Collection create() {
      return new LinkedList();
    }
  }
  public static class HashSetFactory implements CollectionFactory, Serializable {
    public Collection create() {
      return new HashSet();
    }
    public Collection createSingleton(Object contents) {
      return Collections.singleton(contents);
    }
    public Collection createEmpty() {
      return Collections.EMPTY_SET;
    }
  }
  public static class TreeSetFactory extends HashSetFactory {
    private Comparator comparator = ComparatorUtils.NATURAL_COMPARATOR;

    public TreeSetFactory() {}
    public TreeSetFactory(Comparator comparator) {this.comparator = comparator;}

    public Collection create() {
      return new TreeSet(comparator);
    }
  }
}
