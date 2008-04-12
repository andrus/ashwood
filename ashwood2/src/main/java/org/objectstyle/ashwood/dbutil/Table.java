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

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class Table implements Serializable {

	private String catalog;
	private String schema;
	private String name;

	private Collection<Column> columns = new ArrayList<Column>(1);
	private Collection<ForeignKey> foreignKeys = new ArrayList<ForeignKey>(1);
	private Collection<PrimaryKey> primaryKeys = new ArrayList<PrimaryKey>(1);

	public Table() {
	}

	public Table(String catalog, String schema, String name) {
		setCatalog(catalog);
		setSchema(schema);
		setName(name);
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		return schema;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name != null ? name : "";
	}

	public static String composeFullName(String catalog, String schema,
			String tableName) {

		StringBuilder buffer = new StringBuilder();

		if (catalog != null) {
			buffer.append(catalog).append('.');
		}

		if (schema != null) {
			buffer.append(schema).append('.');
		}

		if (tableName != null) {
			buffer.append(tableName);
		}

		return buffer.toString();
	}

	public String getFullName() {
		return composeFullName(catalog, schema, name);
	}

	public void refreshColumns(DatabaseMetaData metaData) throws SQLException {
		columns.clear();
		ResultSet rs = null;
		try {
			rs = metaData.getColumns(catalog, schema, name, null);
			while (rs.next()) {
				Column column = new Column();
				columns.add(column);
				column.setOwner(this);
				column.setName(rs.getString("COLUMN_NAME"));
				column.setDataType(rs.getInt("DATA_TYPE"));
				column.setTypeName(rs.getString("TYPE_NAME"));
				column.setSize(rs.getInt("COLUMN_SIZE"));
				column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
				column.setRadix(rs.getInt("NUM_PREC_RADIX"));
				column.setNullable(rs.getInt("NULLABLE"));
				column.setRemarks(rs.getString("REMARKS"));
				column.setDefaultValue(rs.getString("COLUMN_DEF"));
				column.setCharOctetLength(rs.getInt("CHAR_OCTET_LENGTH"));
				column.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
			}
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			try {
				rs.close();
			} catch (Exception ex) {
			}
		}
	}

	public void refreshPrimaryKeys(DatabaseMetaData metaData)
			throws SQLException {
		primaryKeys.clear();
		ResultSet rs = null;
		try {
			rs = metaData.getPrimaryKeys(catalog, schema, name);
			while (rs.next()) {
				PrimaryKey pk = new PrimaryKey();
				primaryKeys.add(pk);
				pk.setOwner(this);
				pk.setColumnName(rs.getString("COLUMN_NAME"));
				pk.setKeySequence(rs.getShort("KEY_SEQ"));
				pk.setName(rs.getString("PK_NAME"));
			}
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			try {
				rs.close();
			} catch (Exception ex) {
			}
		}
	}

	public void refreshForeignKeys(DatabaseMetaData metaData)
			throws SQLException {
		foreignKeys.clear();
		ResultSet rs = null;
		try {
			rs = metaData.getImportedKeys(catalog, schema, name);
			while (rs.next()) {
				ForeignKey fk = new ForeignKey();
				foreignKeys.add(fk);
				fk.setOwner(this);
				fk.setPkTableCatalog(rs.getString("PKTABLE_CAT"));
				fk.setPkTableSchema(rs.getString("PKTABLE_SCHEM"));
				fk.setPkTableName(rs.getString("PKTABLE_NAME"));
				fk.setPkColumnName(rs.getString("PKCOLUMN_NAME"));
				fk.setColumnName(rs.getString("FKCOLUMN_NAME"));
				fk.setKeySequence(rs.getShort("KEY_SEQ"));
				fk.setUpdateRule(rs.getShort("UPDATE_RULE"));
				fk.setDeleteRule(rs.getShort("DELETE_RULE"));
				fk.setName(rs.getString("FK_NAME"));
				fk.setPkName(rs.getString("PK_NAME"));
				fk.setDeferrability(rs.getShort("DEFERRABILITY"));
			}
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			try {
				rs.close();
			} catch (Exception ex) {
			}
		}
	}

	public void refresh(DatabaseMetaData metaData) throws SQLException {
		refreshColumns(metaData);
		refreshPrimaryKeys(metaData);
		refreshForeignKeys(metaData);
	}

	public Collection<PrimaryKey> getPrimaryKeys() {
		return Collections.unmodifiableCollection(primaryKeys);
	}

	public Collection<ForeignKey> getForeignKeys() {
		return Collections.unmodifiableCollection(foreignKeys);
	}

	public Collection<Column> getColumns() {
		return Collections.unmodifiableCollection(columns);
	}

	public void addColumn(Column column) {
		columns.add(column);
		column.setOwner(this);
	}

	public boolean removeColumn(Column column) {
		column.setOwner(null);
		return columns.remove(column);
	}

	public void addPrimaryKey(PrimaryKey primaryKey) {
		primaryKeys.add(primaryKey);
		primaryKey.setOwner(this);
	}

	public boolean removePrimaryKey(PrimaryKey primaryKey) {
		primaryKey.setOwner(null);
		return primaryKeys.remove(primaryKey);
	}

	public void addForeignKey(ForeignKey foreignKey) {
		foreignKeys.add(foreignKey);
		foreignKey.setOwner(this);
	}

	public boolean removeForeignKey(ForeignKey foreignKey) {
		foreignKey.setOwner(null);
		return foreignKeys.remove(foreignKey);
	}

	public void toCreateSQL(PrintWriter out) {
		out.println("CREATE TABLE " + name + " (");
		for (Iterator<Column> i = columns.iterator(); i.hasNext();) {
			Column c = i.next();
			out.print("  " + c.getName() + " " + c.getTypeName());
			if (c.getNullable() == DatabaseMetaData.columnNoNulls)
				out.print(" NOT NULL");
			if (i.hasNext())
				out.println(',');
		}

		if (!primaryKeys.isEmpty()) {
			out.println(',');
			Iterator<PrimaryKey> i = primaryKeys.iterator();
			PrimaryKey pk = i.next();
			out.print("  ");
			if (pk.getName() != null) {
				out.print("CONSTRAINT " + pk.getName() + " ");
			}
			out.print("PRIMARY KEY (" + pk.getColumnName());
			while (i.hasNext()) {
				pk = i.next();
				out.print(", " + pk.getColumnName());
			}
			out.print(")");
		}
		if (!foreignKeys.isEmpty()) {
			out.println(',');
			for (Iterator<ForeignKey> i = foreignKeys.iterator(); i.hasNext();) {
				ForeignKey fk = i.next();
				out.print("  ");
				if (fk.getName() != null)
					out.print("CONSTRAINT " + fk.getName() + " ");
				out.print("FOREIGN KEY (" + fk.getColumnName()
						+ ") REFERENCES " + fk.getPkTableName());
				if (fk.getPkColumnName() != null)
					out.print(" (" + fk.getPkColumnName() + ")");
				if (i.hasNext())
					out.println(',');
			}
		}
		out.println();
		out.print(")");
	}

	public void toDropSQL(PrintWriter out) {
		out.print("DROP TABLE " + name + " CASCADE CONSTRAINTS");
	}
}
