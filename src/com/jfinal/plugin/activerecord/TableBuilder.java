/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.plugin.activerecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TableBuilder build the mapping of model between class and table.
 */
class TableBuilder {
	
	private static final Map<String, Class<?>> strToType = new HashMap<String, Class<?>>() {
		private static final long serialVersionUID = -8651755311062618532L; {
		
		// varchar, char, enum, set, text, tinytext, mediumtext, longtext
		put("java.lang.String", java.lang.String.class);
		
		// int, integer, tinyint, smallint, mediumint
		put("java.lang.Integer", java.lang.Integer.class);
		
		// bigint
		put("java.lang.Long", java.lang.Long.class);
		
		// java.util.Data can not be returned
		// java.sql.Date, java.sql.Time, java.sql.Timestamp all extends java.util.Data so getDate can return the three types data
		// put("java.util.Date", java.util.Date.class);
		
		// date, year
		put("java.sql.Date", java.sql.Date.class);
		
		// real, double
		put("java.lang.Double", java.lang.Double.class);
		
		// float
		put("java.lang.Float", java.lang.Float.class);
		
		// bit
		put("java.lang.Boolean", java.lang.Boolean.class);
		
		// time
		put("java.sql.Time", java.sql.Time.class);
		
		// timestamp, datetime
		put("java.sql.Timestamp", java.sql.Timestamp.class);
		
		// decimal, numeric
		put("java.math.BigDecimal", java.math.BigDecimal.class);
		
		// binary, varbinary, tinyblob, blob, mediumblob, longblob
		// qjd project: print_info.content varbinary(61800);
		put("[B", byte[].class);
	}};
	
	static void build(List<Table> tableList, Config config) {
		Table temp = null;
		Connection conn = null;
		try {
			conn = config.dataSource.getConnection();
			TableMapping tableMapping = TableMapping.me();
			for (Table table : tableList) {
				temp = table;
				doBuild(table, conn, config);
				tableMapping.putTable(table);
				DbKit.addModelToConfigMapping(table.getModelClass(), config);
			}
		} catch (Exception e) {
			if (temp != null)
				System.err.println("Can not create Table object, maybe the table " + temp.getName() + " is not exists.");
			throw new ActiveRecordException(e);
		}
		finally {
			config.close(conn);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void doBuild(Table table, Connection conn, Config config) throws SQLException {
		table.setColumnTypeMap(config.containerFactory.getAttrsMap());
		if (table.getPrimaryKey() == null)
			table.setPrimaryKey(config.dialect.getDefaultPrimaryKey());
		
		String sql = config.dialect.forTableBuilderDoBuild(table.getName());
		Statement stm = conn.createStatement();
		ResultSet rs = stm.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		
		for (int i=1; i<=rsmd.getColumnCount(); i++) {
			String colName = rsmd.getColumnName(i);
			String colClassName = rsmd.getColumnClassName(i);
			
			Class<?> clazz = strToType.get(colClassName);
			if (clazz != null) {
				table.setColumnType(colName, clazz);
			}
			else {
				int type = rsmd.getColumnType(i);
				if (type == Types.BLOB) {
					table.setColumnType(colName, byte[].class);
				}
				else if (type == Types.CLOB || type == Types.NCLOB) {
					table.setColumnType(colName, String.class);
				}
				else {
					table.setColumnType(colName, String.class);
				}
				// core.TypeConverter
				// throw new RuntimeException("You've got new type to mapping. Please add code in " + TableBuilder.class.getName() + ". The ColumnClassName can't be mapped: " + colClassName);
			}
		}
		
		rs.close();
		stm.close();
	}
}

