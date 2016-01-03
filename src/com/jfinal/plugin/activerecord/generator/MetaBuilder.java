/**
 * Copyright (c) 2011-2016, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.plugin.activerecord.generator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.DataSource;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;

/**
 * MetaBuilder
 */
public class MetaBuilder {
	
	protected DataSource dataSource;
	protected Dialect dialect = new MysqlDialect();
	protected Set<String> excludedTables = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	
	protected Connection conn = null;
	protected DatabaseMetaData dbMeta = null;
	
	protected String[] removedTableNamePrefixes = null;
	
	private TypeMapping typeMapping = new TypeMapping();
	
	public MetaBuilder(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("dataSource can not be null.");
		}
		this.dataSource = dataSource;
	}
	
	public void setDialect(Dialect dialect) {
		if (dialect != null) {
			this.dialect = dialect;
		}
	}
	
	public void addExcludedTable(String... excludedTables) {
		if (excludedTables != null) {
			for (String table : excludedTables) {
				this.excludedTables.add(table);
			}
		}
	}
	
	/**
	 * 设置需要被移除的表名前缀，仅用于生成 modelName 与  baseModelName
	 * 例如表名  "osc_account"，移除前缀 "osc_" 后变为 "account"
	 */
	public void setRemovedTableNamePrefixes(String... removedTableNamePrefixes) {
		this.removedTableNamePrefixes = removedTableNamePrefixes;
	}
	
	public void setTypeMapping(TypeMapping typeMapping) {
		if (typeMapping != null) {
			this.typeMapping = typeMapping;
		}
	}
	
	public List<TableMeta> build() {
		System.out.println("Build TableMeta ...");
		try {
			conn = dataSource.getConnection();
			dbMeta = conn.getMetaData();
			
			List<TableMeta> ret = new ArrayList<TableMeta>();
			buildTableNames(ret);
			for (TableMeta tableMeta : ret) {
				buildPrimaryKey(tableMeta);
				buildColumnMetas(tableMeta);
			}
			return ret;
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				try {conn.close();} catch (SQLException e) {throw new RuntimeException(e);}
		}
	}
	
	protected void buildTableNames(List<TableMeta> ret) throws SQLException {
		String userName = dialect.isOracle() ? dbMeta.getUserName() : null;
		ResultSet rs = dbMeta.getTables(conn.getCatalog(), userName, null, new String[]{"TABLE", "VIEW"});
		while (rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			
			if (excludedTables.contains(tableName)) {
				System.out.println("Skip excluded table :" + tableName);
			}
			else {
				TableMeta tableMeta = new TableMeta();
				tableMeta.name = tableName;
				tableMeta.remarks = rs.getString("REMARKS");
				
				// 移除表名前缀仅用于生成 modelName、baseModelName。tableMeta.name 表名自身不受影响
				if (removedTableNamePrefixes != null) {
					for (String prefix : removedTableNamePrefixes) {
						if (tableName.startsWith(prefix)) {
							tableName = tableName.replaceFirst(prefix, "");
							break;
						}
					}
				}
				if (dialect.isOracle()) {
					tableName = tableName.toLowerCase();
				}
				tableMeta.modelName = StrKit.firstCharToUpperCase(StrKit.toCamelCase(tableName));
				tableMeta.baseModelName = "Base" + tableMeta.modelName;
				ret.add(tableMeta);
			}
		}
		rs.close();
	}
	
	protected void buildPrimaryKey(TableMeta tableMeta) throws SQLException {
		ResultSet rs = dbMeta.getPrimaryKeys(conn.getCatalog(), null, tableMeta.name);
		
		String primaryKey = "";
		int index = 0;
		while (rs.next()) {
			if (index++ > 0)
				primaryKey += ",";
			primaryKey += rs.getString("COLUMN_NAME");
		}
		tableMeta.primaryKey = primaryKey;
		rs.close();
	}
	
	/**
	 * 文档参考：
	 * http://dev.mysql.com/doc/connector-j/en/connector-j-reference-type-conversions.html
	 * 
	 * JDBC 与时间有关类型转换规则，mysql 类型到 java 类型如下对应关系：
	 * DATE				java.sql.Date
	 * DATETIME			java.sql.Timestamp
	 * TIMESTAMP[(M)]	java.sql.Timestamp
	 * TIME				java.sql.Time
	 * 
	 * 对数据库的 DATE、DATETIME、TIMESTAMP、TIME 四种类型注入 new java.util.Date()对象保存到库以后可以达到“秒精度”
	 * 为了便捷性，getter、setter 方法中对上述四种字段类型采用 java.util.Date，可通过定制 TypeMapping 改变此映射规则
	 */
	protected void buildColumnMetas(TableMeta tableMeta) throws SQLException {
		String sql = dialect.forTableBuilderDoBuild(tableMeta.name);
		Statement stm = conn.createStatement();
		ResultSet rs = stm.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		
		for (int i=1; i<=rsmd.getColumnCount(); i++) {
			ColumnMeta cm = new ColumnMeta();
			cm.name = rsmd.getColumnName(i);
			
			String colClassName = rsmd.getColumnClassName(i);
			String typeStr = typeMapping.getType(colClassName);
			if (typeStr != null) {
				cm.javaType = typeStr;
			}
			else {
				int type = rsmd.getColumnType(i);
				if (type == Types.BINARY || type == Types.VARBINARY || type == Types.BLOB) {
					cm.javaType = "byte[]";
				}
				else if (type == Types.CLOB || type == Types.NCLOB) {
					cm.javaType = "java.lang.String";
				}
				else {
					cm.javaType = "java.lang.String";
				}
			}
			
			tableMeta.columnMetas.add(cm);
		}
		
		rs.close();
		stm.close();
	}
}







