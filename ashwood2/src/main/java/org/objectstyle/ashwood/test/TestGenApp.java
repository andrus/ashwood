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

package org.objectstyle.ashwood.test;

import java.util.*;
import java.io.*;
import org.objectstyle.ashwood.graph.access.*;
import org.objectstyle.ashwood.graph.convert.*;
import org.objectstyle.ashwood.graph.*;

public class TestGenApp {
  public static void main(String[] args) throws Exception {
    File dir = new File("d:/temp");
    int graphCount = 100;
    int order = 15;
    int size = 45;
    long seed = 1000;//System.currentTimeMillis();
    PropertiesConverter converter = new PropertiesConverter();
    converter.setVertexAccessor(new IntegerAccessor());
    Digraph digraph;
    for (int i = 1; i <= graphCount; i++) {
      digraph = new MapDigraph(MapDigraph.TREEMAP_FACTORY);
      //GraphUtils.randomize(digraph, order, size, seed++);
      GraphUtils.randomizeAcyclic(digraph, order, 3, 3, new Random(seed++));
      boolean acyclic = GraphUtils.isAcyclic(digraph);
      boolean stronglyConnected = GraphUtils.isStronglyConnected(digraph);
      StrongConnection contractor = new StrongConnection(digraph, CollectionFactory.TREESET_FACTORY);
      Digraph contactedDigraph = contractor.contract(new MapDigraph(MapDigraph.HASHMAP_FACTORY));
      int componentCount = contactedDigraph.order();
      String graphName = "digraph-" + i;
      String desc = "seed=" + (seed-1);
      desc += ", acyclic=" + acyclic;
      desc += ", strongly_connected=" + stronglyConnected;
      desc += ", components=" + componentCount;
      System.out.println(graphName + ": " + desc);
      converter.convert(digraph);
      Properties data = converter.getGraphData();
      converter.reset();
      File f = new File(dir, graphName + ".txt");
      FileOutputStream out = new FileOutputStream(f);
      data.store(out, desc);
      out.close();
    }
  }
}
