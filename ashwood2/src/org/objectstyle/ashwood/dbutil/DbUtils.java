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
import org.apache.commons.lang.*;
import org.apache.commons.collections.*;
import org.objectstyle.ashwood.graph.*;

public class DbUtils {

  private DbUtils() {
  }

  public static void getAllTables(String catalog, String schema, Collection tables, DatabaseMetaData metaData) throws SQLException {
    String[] tableTypes = new String[] {"TABLE"};
    ResultSet rs = null;
    try {
      rs = metaData.getTables(catalog, schema, null, tableTypes);
      while (rs.next()) {
        Table table = new Table(catalog, schema, rs.getString("TABLE_NAME"));
        tables.add(table);
      }
    } finally {
      try {rs.close();}
      catch (Exception e) {}
    }
  }

  public static void refreshTables(Collection tables, DatabaseMetaData metaData) throws SQLException {
    refreshTables(tables, metaData, true, true, true);
  }

  public static void refreshTables(Collection tables, DatabaseMetaData metaData, boolean columns, boolean primaryKeys, boolean foreignKeys) throws SQLException {
    for (Iterator i = tables.iterator(); i.hasNext();) {
      Table table = (Table)i.next();
      if (columns) table.refreshColumns(metaData);
      if (primaryKeys) table.refreshPrimaryKeys(metaData);
      if (foreignKeys) table.refreshForeignKeys(metaData);
    }
  }

  public static Digraph buildReferentialDigraph(Digraph digraph, Collection tables) {
    HashMap tableMap = new HashMap();
    for (Iterator i = tables.iterator(); i.hasNext();) {
      Table table = (Table)i.next();
      tableMap.put(table.getFullName(), table);
      digraph.addVertex(table);
    }
    for (Iterator i = tables.iterator(); i.hasNext();) {
      Table dst = (Table)i.next();
      for (Iterator j = dst.getForeignKeys().iterator(); j.hasNext();) {
        ForeignKey fk = (ForeignKey)j.next();
        String pkTableFullName = Table.composeFullName(fk.getPkTableCatalog(),
            fk.getPkTableSchema(),
            fk.getPkTableName());
        Table origin = (Table)tableMap.get(pkTableFullName);
        if (origin != null) {
          ArrayList fks = (ArrayList)digraph.getArc(origin, dst);
          if (fks == null) {
            fks = new ArrayList();
            digraph.putArc(origin, dst, fks);
          }
          fks.add(fk);
        }
      }
    }
    return digraph;
  }
}
