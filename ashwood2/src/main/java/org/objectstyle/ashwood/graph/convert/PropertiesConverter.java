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

package org.objectstyle.ashwood.graph.convert;

import java.util.*;
import org.objectstyle.ashwood.graph.*;
import org.objectstyle.ashwood.graph.access.*;
import org.apache.commons.collections.*;

public class PropertiesConverter {
  private Properties graphData = new Properties();
  private DataAccessor vertexAccessor = new DataAccessor();

  public PropertiesConverter() {
  }

  public Properties getGraphData() {
    return (Properties)graphData.clone();
  }
  public void reset() {
    graphData.clear();
  }
  public void build(Digraph digraph) {
    for (Iterator i = graphData.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry)i.next();
      Object origin = vertexAccessor.create(entry.getKey());
      digraph.addVertex(origin);
      StringTokenizer st = new StringTokenizer((String)entry.getValue(), ";");
      while (st.hasMoreTokens()) {
        digraph.putArc(origin, vertexAccessor.create(st.nextToken()), Boolean.TRUE);
      }
    }
  }

  public void convert(Digraph digraph) {
    for (Iterator i = digraph.vertexIterator(); i.hasNext();) {
      Object origin = i.next();
      String key = vertexAccessor.getId(origin).toString();
      StringBuffer value = new StringBuffer();
      boolean firstDst = true;
      for (ArcIterator j = digraph.outgoingIterator(origin); j.hasNext();) {
        j.next();
        if (firstDst) {
          value.append(vertexAccessor.getId(j.getDestination()));
          firstDst = false;
        } else value.append(';').append(j.getDestination());
      }
      graphData.setProperty(key, value.toString());
    }
  }
  public void setVertexAccessor(DataAccessor vertexAccessor) {
    this.vertexAccessor = vertexAccessor;
  }
  public DataAccessor getVertexAccessor() {
    return vertexAccessor;
  }
}
