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
import org.objectstyle.ashwood.function.*;

public class MedianUtils {
  private MedianUtils() {}

  public static double weightedMedianValue(
      Object o1, Object o2, DoubleFunction valueFunction) {
    return (valueFunction.doubleValue(o1) + valueFunction.doubleValue(o2)) / 2;
  }

  public static double weightedMedianValue(
      List elements, DoubleFunction valueFunction) {
    int size = elements.size();
    switch (size) {
      case 0:
        return Double.NaN;
      case 1:
        return valueFunction.doubleValue(elements.get(0));
      case 2:
        return weightedMedianValue(elements.get(0), elements.get(1), valueFunction);
      default:
        int rightMedian = size / 2;
        double rmValue = valueFunction.doubleValue(elements.get(rightMedian));
        if ((size % 2) == 1) return rmValue;
        else {
          int leftMedian = rightMedian - 1;
          double lmValue = valueFunction.doubleValue(elements.get(leftMedian));
          double rightSpan =
            valueFunction.doubleValue(elements.get(size - 1)) - rmValue;
          double leftSpan =
            lmValue - valueFunction.doubleValue(elements.get(0));
          if (Math.abs(rightSpan - leftSpan) < 0.000001)
            return (lmValue + rmValue) / 2;
          else {
            return (lmValue * rightSpan + rmValue * leftSpan) / (leftSpan + rightSpan);
          }
        }
    }
  }

  public static double weightedMedianValue(
      Object[] elements, DoubleFunction valueFunction) {
    return weightedMedianValue(Arrays.asList(elements), valueFunction);
  }

  public static double weightedMedianValue(double[] elements) {
    int size = elements.length;
    switch (size) {
      case 0:
        return Double.NaN;
      case 1:
        return elements[0];
      case 2:
        return (elements[0] + elements[1]) / 2;
      default:
        int rightMedian = size / 2;
        double rmValue = elements[rightMedian];
        if ((size % 2) == 1) return rmValue;
        else {
          int leftMedian = rightMedian - 1;
          double lmValue = elements[leftMedian];
          double rightSpan = elements[size - 1] - rmValue;
          double leftSpan = lmValue - elements[0];
          if (Math.abs(rightSpan - leftSpan) < 0.000001)
            return (lmValue + rmValue) / 2;
          else {
            return (lmValue * rightSpan + rmValue * leftSpan) / (leftSpan + rightSpan);
          }
        }
    }
  }
}