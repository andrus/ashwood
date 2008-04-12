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

public final class IntPartition {
  private int[] partition;
  private int setCount;

  public IntPartition(int elementCount) {
    partition = new int[elementCount];
    reset();
  }

  public final int joinSets(int setId1, int setId2) {
    if (setId1 < 0 || setId2 < 0)
      throw new IndexOutOfBoundsException("setId1=" + setId1 + ", setId2=" + setId2);
    if (setId1 == setId2) return setId1;
    int rank1 = partition[setId1];
    int rank2 = partition[setId2];
    setCount--;
    if (rank2 < rank1) {
      partition[setId1] = setId2;
      return setId2;
    } else {
      if (rank2 == rank1) partition[setId1]--;
      partition[setId2] = setId1;
      return setId1;
    }
  }

  public final int findSetId(int element) {
    if (partition[element] < 0)
      return element;
    else {
      partition[element] = findSetId(partition[element]);
      return partition[element];
    }
  }

  public final int size() {
    return partition.length;
  }

  public final int getSetCount() {
    return setCount;
  }

  public final void reset() {
    for (int i = 0; i < partition.length; i++) {
      partition[i] = -1;
    }
    setCount = size();
  }

  public final boolean isSetId(int element) {
    return partition[element] < 0;
  }
}