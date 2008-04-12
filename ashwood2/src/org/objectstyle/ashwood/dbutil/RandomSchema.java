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

package org.objectstyle.ashwood.dbutil;

import java.util.*;
import java.sql.*;
import org.objectstyle.ashwood.graph.*;
import org.objectstyle.ashwood.random.*;

public class RandomSchema {
  private boolean acyclic = true;
  private Digraph schemaGraph;
  private int tableCount = 10;
  private int maxReferencesPerTable = 3;
  private int maxForeignKeysPerTable = 3;
  private Random randomizer = new Random();
  private List tables = Collections.EMPTY_LIST;
  private String schemaName;
  private String catalog;
  private Map sequencesByTable = Collections.EMPTY_MAP;
  private int maxLoopsPerTable=0;
  private int loopCount=0;

  public RandomSchema() {
  }

  public boolean isAcyclic() {
    return acyclic;
  }
  public void setAcyclic(boolean acyclic) {
    this.acyclic = acyclic;
  }

  public void generate() {
    schemaGraph = new MapDigraph(MapDigraph.HASHMAP_FACTORY);
    generateAcyclicSchema();
  }

  private void generateAcyclicSchema() {
    Digraph graph = new MapDigraph(MapDigraph.HASHMAP_FACTORY);
    Map vertexToTable = new HashMap();
    GraphUtils.randomizeAcyclic(graph, tableCount, maxForeignKeysPerTable, maxReferencesPerTable, randomizer);
    tables = new ArrayList(tableCount);
    sequencesByTable = new HashMap(tableCount);
    Algorithm sorter = new IndegreeTopologicalSort(graph);
    List sortedVertices = new ArrayList(graph.order());
    while (sorter.hasNext()) sortedVertices.add(sorter.next());
    if (loopCount > 0 && maxLoopsPerTable > 0) {
      Roulette loopSelector = new Roulette(sortedVertices.size(), 1, randomizer);
      for (int i = Math.min(loopCount, sortedVertices.size()); i > 0; i--) {
        Number vertexIndex = (Number)loopSelector.next();
        Object vertex = sortedVertices.get(vertexIndex.intValue());
        graph.putArc(vertex, vertex, Boolean.TRUE);
      }
    }
    for (Iterator i = sortedVertices.iterator(); i.hasNext();) {
      Integer vertex = (Integer)i.next();
      Table table = generateTable(vertex, graph);
      vertexToTable.put(vertex, table);
      schemaGraph.addVertex(table);
    }
    for (ArcIterator i = graph.arcIterator(); i.hasNext();) {
      i.next();
      Object origin = vertexToTable.get(i.getOrigin());
      Object dst = vertexToTable.get(i.getDestination());
      schemaGraph.putArc(origin, dst, Boolean.TRUE);
    }
  }

  private Table generateTable(Integer vertex, Digraph graph) {
    Table table = new Table(catalog, schemaName, "TABLE" + vertex);
    int outSize = graph.outgoingSize(vertex);
    int inSize = graph.incomingSize(vertex);
    if (outSize != 0 || (outSize == 0 && inSize == 0)) {
      Column pkColumn = new Column();
      pkColumn.setName(table.getName() + "_ID");
      pkColumn.setTypeName("INTEGER");
      pkColumn.setNullable(DatabaseMetaData.columnNoNulls);
      table.addColumn(pkColumn);
      PrimaryKey pk = new PrimaryKey(pkColumn);
      table.addPrimaryKey(pk);
      sequencesByTable.put(table, new Sequence(table.getName() + "_SEQ"));
    }
    for (ArcIterator i = graph.incomingIterator(vertex); i.hasNext();) {
      i.next();
      Integer origin = (Integer)i.getOrigin();
      String referencedTableName = "TABLE" + origin;
      String pkColumnName = referencedTableName + "_ID";
      String pkName = referencedTableName + "_PK";
      int fkCount = (!vertex.equals(origin) ? 1 : generateFkCountForLoop());
      for (int j = 1; j <= fkCount; j++) {
        String fkColumnSuffix = (/*fkCount == 1 ? "" :*/ String.valueOf(j));
        Column fkColumn = new Column();
        fkColumn.setName(pkColumnName + fkColumnSuffix);
        fkColumn.setTypeName("INTEGER");
        fkColumn.setNullable(DatabaseMetaData.columnNoNulls);
        table.addColumn(fkColumn);
        if (outSize == 0) {
          PrimaryKey pk = new PrimaryKey(fkColumn);
          table.addPrimaryKey(pk);
        }
        ForeignKey fk = new ForeignKey(fkColumn);
        fk.setPkColumnName(pkColumnName);
        fk.setPkName(pkName);
        fk.setPkTableCatalog(catalog);
        fk.setPkTableName(referencedTableName);
        fk.setPkTableSchema(schemaName);
        table.addForeignKey(fk);
      }
    }
    tables.add(table);
    return table;
  }

  private int generateFkCountForLoop() {
    int count = randomizer.nextInt(maxLoopsPerTable) + 1;
    return count;
  }

  public Digraph getSchemaGraph() {
    return schemaGraph;
  }
  public void setTableCount(int tableCount) {
    this.tableCount = tableCount;
  }
  public int getTableCount() {
    return tableCount;
  }
  public void setMaxReferencesPerTable(int maxReferencesPerTable) {
    this.maxReferencesPerTable = maxReferencesPerTable;
  }
  public int getMaxReferencesPerTable() {
    return maxReferencesPerTable;
  }
  public void setMaxForeignKeysPerTable(int maxForeignKeysPerTable) {
    this.maxForeignKeysPerTable = maxForeignKeysPerTable;
  }
  public int getMaxForeignKeysPerTable() {
    return maxForeignKeysPerTable;
  }
  public void setRandomizer(Random randomizer) {
    this.randomizer = randomizer;
  }
  public Random getRandomizer() {
    return randomizer;
  }
  public List getTables() {
    return Collections.unmodifiableList(tables);
  }
  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }
  public String getSchemaName() {
    return schemaName;
  }
  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }
  public String getCatalog() {
    return catalog;
  }
  public Map getSequencesByTable() {
    return Collections.unmodifiableMap(sequencesByTable);
  }
  public void setMaxLoopsPerTable(int maxLoopsPerTable) {
    this.maxLoopsPerTable = maxLoopsPerTable;
  }
  public int getMaxLoopsPerTable() {
    return maxLoopsPerTable;
  }
  public void setLoopCount(int loopCount) {
    this.loopCount = loopCount;
  }
  public int getLoopCount() {
    return loopCount;
  }
}
