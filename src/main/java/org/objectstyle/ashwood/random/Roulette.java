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

package org.objectstyle.ashwood.random;

import java.util.*;

public class Roulette implements Iterator {
  private Random random;
  private int[] sectors;

  public Roulette(int sectorCount) {
    this(sectorCount, 1, new Random());
  }
  public Roulette(int sectorCount, int sectorSelectionCount, Random randomizer) {
    random = randomizer;
    this.sectors = new int[sectorCount];
    Arrays.fill(sectors, sectorSelectionCount);
  }
  public Roulette(int[] sectors, Random randomizer) {
    random = randomizer;
    this.sectors = new int[sectors.length];
    System.arraycopy(sectors, 0, this.sectors, 0, sectors.length);
  }
  public boolean hasNext() {
    for (int i = 0; i < sectors.length; i++) {
      if (sectors[i] > 0) return true;
    }
    return false;
  }
  public Object next() {
    int pointedSector = random.nextInt(sectors.length);
    for (int i = pointedSector; i < sectors.length; i++) {
      if (sectors[i]-- > 0) return new Integer(i);
    }
    for (int i = 0; i < pointedSector; i++) {
      if (sectors[i]-- > 0) return new Integer(i);
    }
    return null;
  }
  public void remove() {
    throw new java.lang.UnsupportedOperationException("Method remove() not supported.");
  }
}