/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord.dialect;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.builder.TimestampProcessedModelBuilder;
import com.jfinal.plugin.activerecord.builder.TimestampProcessedRecordBuilder;

/**
 * PostgreSqlDialect.
 */
public class PostgreSqlDialect extends Dialect {
	
	public PostgreSqlDialect() {
		this.modelBuilder = TimestampProcessedModelBuilder.me;
		this.recordBuilder = TimestampProcessedRecordBuilder.me;
	}
	
	public String forTableBuilderDoBuild(String tableName) {
		return "select * from \"" + tableName + "\" where 1 = 2";
	}
	
	public void forModelSave(Table table, Map<String, Object> attrs, StringBuilder sql, List<Object> paras) {
		sql.append("insert into \"").append(table.getName()).append("\"(");
		StringBuilder temp = new StringBuilder(") values(");
		for (Entry<String, Object> e: attrs.entrySet()) {
			String colName = e.getKey();
			if (table.hasColumnLabel(colName)) {
				if (paras.size() > 0) {
					sql.append(", ");
					temp.append(", ");
				}
				sql.append('\"').append(colName).append('\"');
				temp.append('?');
				paras.add(e.getValue());
			}
		}
		sql.append(temp.toString()).append(')');
	}
	
	public String forModelDeleteById(Table table) {
		String[] pKeys = table.getPrimaryKey();
		StringBuilder sql = new StringBuilder(45);
		sql.append("delete from \"");
		sql.append(table.getName());
		sql.append("\" where ");
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append('\"').append(pKeys[i]).append("\" = ?");
		}
		return sql.toString();
	}
	
	public void forModelUpdate(Table table, Map<String, Object> attrs, Set<String> modifyFlag, StringBuilder sql, List<Object> paras) {
		sql.append("update \"").append(table.getName()).append("\" set ");
		String[] pKeys = table.getPrimaryKey();
		for (Entry<String, Object> e : attrs.entrySet()) {
			String colName = e.getKey();
			if (modifyFlag.contains(colName) && !isPrimaryKey(colName, pKeys) && table.hasColumnLabel(colName)) {
				if (paras.size() > 0) {
					sql.append(", ");
				}
				sql.append('\"').append(colName).append("\" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(" where ");
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append('\"').append(pKeys[i]).append("\" = ?");
			paras.add(attrs.get(pKeys[i]));
		}
	}
	
	public String forModelFindById(Table table, String columns) {
		StringBuilder sql = new StringBuilder("select ");
		columns = columns.trim();
		if ("*".equals(columns)) {
			sql.append('*');
		}
		else {
			String[] arr = columns.split(",");
			for (int i=0; i<arr.length; i++) {
				if (i > 0) {
					sql.append(',');
				}
				sql.append('\"').append(arr[i].trim()).append('\"');
			}
		}
		
		sql.append(" from \"");
		sql.append(table.getName());
		sql.append("\" where ");
		String[] pKeys = table.getPrimaryKey();
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append('\"').append(pKeys[i]).append("\" = ?");
		}
		return sql.toString();
	}
	
	public String forDbFindById(String tableName, String[] pKeys) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);
		
		StringBuilder sql = new StringBuilder("select * from \"").append(tableName).append("\" where ");
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append('\"').append(pKeys[i]).append("\" = ?");
		}
		return sql.toString();
	}
	
	public String forDbDeleteById(String tableName, String[] pKeys) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);
		
		StringBuilder sql = new StringBuilder("delete from \"").append(tableName).append("\" where ");
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append('\"').append(pKeys[i]).append("\" = ?");
		}
		return sql.toString();
	}
	
	public void forDbSave(String tableName, String[] pKeys, Record record, StringBuilder sql, List<Object> paras) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);
		
		sql.append("insert into \"");
		sql.append(tableName).append("\"(");
		StringBuilder temp = new StringBuilder();
		temp.append(") values(");
		
		for (Entry<String, Object> e: record.getColumns().entrySet()) {
			if (paras.size() > 0) {
				sql.append(", ");
				temp.append(", ");
			}
			sql.append('\"').append(e.getKey()).append('\"');
			temp.append('?');
			paras.add(e.getValue());
		}
		sql.append(temp.toString()).append(')');
	}
	
	public void forDbUpdate(String tableName, String[] pKeys, Object[] ids, Record record, StringBuilder sql, List<Object> paras) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);
		
		sql.append("update \"").append(tableName).append("\" set ");
		for (Entry<String, Object> e: record.getColumns().entrySet()) {
			String colName = e.getKey();
			if (!isPrimaryKey(colName, pKeys)) {
				if (paras.size() > 0) {
					sql.append(", ");
				}
				sql.append('\"').append(colName).append("\" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(" where ");
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append('\"').append(pKeys[i]).append("\" = ?");
			paras.add(ids[i]);
		}
	}
	
	public String forPaginate(int pageNumber, int pageSize, StringBuilder findSql) {
		int offset = pageSize * (pageNumber - 1);
		findSql.append(" limit ").append(pageSize).append(" offset ").append(offset);
		return findSql.toString();
	}
	
	public void fillStatement(PreparedStatement pst, List<Object> paras) throws SQLException {
		fillStatementHandleDateType(pst, paras);
	}
	
	public void fillStatement(PreparedStatement pst, Object... paras) throws SQLException {
		fillStatementHandleDateType(pst, paras);
	}
	
	/**
	 * 解决 PostgreSql 获取自增主键时 rs.getObject(1) 总是返回第一个字段的值，而非返回了 id 值
	 * issue: https://www.oschina.net/question/2312705_2243354
	 * 
	 * 相对于 Dialect 中的默认实现，仅将 rs.getXxx(1) 改成了 rs.getXxx(pKey)
	 */
	public void getModelGeneratedKey(Model<?> model, PreparedStatement pst, Table table) throws SQLException {
		String[] pKeys = table.getPrimaryKey();
		ResultSet rs = pst.getGeneratedKeys();
		for (String pKey : pKeys) {
			if (model.get(pKey) == null || isOracle()) {
				if (rs.next()) {
					Class<?> colType = table.getColumnType(pKey);
					if (colType != null) {
						if (colType == Integer.class || colType == int.class) {
							model.set(pKey, rs.getInt(pKey));
						} else if (colType == Long.class || colType == long.class) {
							model.set(pKey, rs.getLong(pKey));
						} else if (colType == BigInteger.class) {
							processGeneratedBigIntegerKey(model, pKey, rs.getObject(pKey));
						} else {
							model.set(pKey, rs.getObject(pKey));
						}
					}
				}
			}
		}
		rs.close();
	}
	
	/**
	 * 解决 PostgreSql 获取自增主键时 rs.getObject(1) 总是返回第一个字段的值，而非返回了 id 值
	 * issue: https://www.oschina.net/question/2312705_2243354
	 * 
	 * 相对于 Dialect 中的默认实现，仅将 rs.getXxx(1) 改成了 rs.getXxx(pKey)
	 */
	public void getRecordGeneratedKey(PreparedStatement pst, Record record, String[] pKeys) throws SQLException {
		ResultSet rs = pst.getGeneratedKeys();
		for (String pKey : pKeys) {
			if (record.get(pKey) == null || isOracle()) {
				if (rs.next()) {
					record.set(pKey, rs.getObject(pKey));
				}
			}
		}
		rs.close();
	}
}
