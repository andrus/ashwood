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

package org.objectstyle.ashwood.util;

import java.util.*;
import org.apache.commons.collections.*;

public class Pair implements java.io.Serializable {
  public static final Comparator FIRST_COMPARATOR = new FirstComparator();
  public static final Comparator SECOND_COMPARATOR = new SecondComparator();

  public Object first;
  public Object second;

  public Pair(Object x, Object y) {
    first = x;
    second = y;
  }

  public Pair() {
    first = null;
    second = null;
  }

  public Pair(Pair pair) {
    first = pair.first;
    second = pair.second;
  }

  public int hashCode() {
    int h = first == null ? 0 : first.hashCode();
    if ( second != null )
      h ^= second.hashCode();
    return h;
  }

  public String toString() {
    return ("Pair( " + first + ", " + second + " )");
  }

  public boolean equals(Object object) {
    return (object instanceof Pair && equals((Pair)object));
  }

  public boolean equals(Pair pair) {
    if ( pair == null ) return false;
    return (( first == null ? pair.first == null : first.equals( pair.first ) )
            && ( second == null ? pair.second == null : second.equals( pair.second ) ));
  }

  public Object clone() {
    return new Pair( this );
  }

  public static class FirstComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      return ComparatorUtils.NATURAL_COMPARATOR.compare(((Pair)o1).first, ((Pair)o2).first);
    }
  }

  public static class SecondComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      return ComparatorUtils.NATURAL_COMPARATOR.compare(((Pair)o1).second, ((Pair)o2).second);
    }
  }
}