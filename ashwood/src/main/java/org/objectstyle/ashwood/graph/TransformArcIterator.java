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
import org.apache.commons.collections.iterators.ProxyIterator;

public class TransformArcIterator extends ProxyIterator implements ArcIterator {
  private Transformer transformOrigin, transformDestination;
  private Transformer transformArc;

  public TransformArcIterator(ArcIterator iterator,
                              Transformer transformOrigin,
                              Transformer transformDestination,
                              Transformer transformArc) {
    super(iterator);
    this.transformOrigin = transformOrigin;
    this.transformDestination = transformDestination;
    this.transformArc = transformArc;
  }

  public TransformArcIterator(ArcIterator iterator,
                              Transformer transformVertex,
                              Transformer transformArc) {
    this(iterator, transformVertex, transformVertex, transformArc);
  }

  public TransformArcIterator(ArcIterator iterator,
                              Transformer transformArc) {
    this(iterator, null, null, transformArc);
  }

  public Object next() {
    return transformArc(super.next());
  }

  public Object getOrigin() {
    return transformOrigin(((ArcIterator)getIterator()).getOrigin());
  }

  public Object getDestination() {
    return transformDst(((ArcIterator)getIterator()).getDestination());
  }

  protected Object transformArc(Object source) {
    if (transformArc != null)
      return transformArc.transform(source);
    return source;
  }

  protected Object transformOrigin(Object source) {
    if (transformOrigin != null)
      return transformOrigin.transform(source);
    return source;
  }

  protected Object transformDst(Object source) {
    if (transformDestination != null)
      return transformDestination.transform(source);
    return source;
  }
}