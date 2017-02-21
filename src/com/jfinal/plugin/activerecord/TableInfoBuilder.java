/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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
import java.util.List;

/**
 * TableInfoBuilder build the mapping of model class and table info.
 */
class TableInfoBuilder {
	
	static boolean buildTableInfo(List<TableInfo> tableMappings) {
		boolean succeed = true;
		Connection conn = null;
		try {
			conn = DbKit.getDataSource().getConnection();
		} catch (SQLException e) {
			throw new ActiveRecordException(e);
		}
		
		TableInfoMapping tim = TableInfoMapping.me();
		for (TableInfo mapping : tableMappings) {
			try {
				TableInfo tableInfo = doBuildTableInfo(mapping, conn);
				tim.putTableInfo(mapping.getModelClass(), tableInfo);
			} catch (Exception e) {
				succeed = false;
				System.err.println("Can not build TableInfo, maybe the table " + mapping.getTableName() + " is not exists.");
				throw new ActiveRecordException(e);
			}
		}
		DbKit.close(conn);
		return succeed;
	}
	
	private static TableInfo doBuildTableInfo(TableInfo tableInfo, Connection conn) throws SQLException {
		TableInfo result = tableInfo;
		
		String sql = DbKit.getDialect().forTableInfoBuilderDoBuildTableInfo(tableInfo.getTableName());
		Statement stm = conn.createStatement();
		ResultSet rs = stm.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		
		for (int i=1; i<=rsmd.getColumnCount(); i++) {
			String colName = rsmd.getColumnName(i);
			String colClassName = rsmd.getColumnClassName(i);
			if ("java.lang.String".equals(colClassName)) {
				// varchar, char, enum, set, text, tinytext, mediumtext, longtext
				result.addInfo(colName, java.lang.String.class);
			}
			else if ("java.lang.Integer".equals(colClassName)) {
				// int, integer, tinyint, smallint, mediumint
				result.addInfo(colName, java.lang.Integer.class);
			}
			else if ("java.lang.Long".equals(colClassName)) {
				// bigint
				result.addInfo(colName, java.lang.Long.class);
			}
			// else if ("java.util.Date".equals(colClassName)) {		// java.util.Data can not be returned
				// java.sql.Date, java.sql.Time, java.sql.Timestamp all extends java.util.Data so getDate can return the three types data
				// result.addInfo(colName, java.util.Date.class);
			// }
			else if ("java.sql.Date".equals(colClassName)) {
				// date, year
				result.addInfo(colName, java.sql.Date.class);
			}
			else if ("java.lang.Double".equals(colClassName)) {
				// real, double
				result.addInfo(colName, java.lang.Double.class);
			}
			else if ("java.lang.Float".equals(colClassName)) {
				// float
				result.addInfo(colName, java.lang.Float.class);
			}
			else if ("java.lang.Boolean".equals(colClassName)) {
				// bit
				result.addInfo(colName, java.lang.Boolean.class);
			}
			else if ("java.sql.Time".equals(colClassName)) {
				// time
				result.addInfo(colName, java.sql.Time.class);
			}
			else if ("java.sql.Timestamp".equals(colClassName)) {
				// timestamp, datetime
				result.addInfo(colName, java.sql.Timestamp.class);
			}
			else if ("java.math.BigDecimal".equals(colClassName)) {
				// decimal, numeric
				result.addInfo(colName, java.math.BigDecimal.class);
			}
			else if ("[B".equals(colClassName)) {
				// binary, varbinary, tinyblob, blob, mediumblob, longblob
				// qjd project: print_info.content varbinary(61800);
				result.addInfo(colName, byte[].class);
			}
			else {
				int type = rsmd.getColumnType(i);
				if (type == Types.BLOB) {
					result.addInfo(colName, byte[].class);
				}
				else if (type == Types.CLOB || type == Types.NCLOB) {
					result.addInfo(colName, String.class);
				}
				else {
					result.addInfo(colName, String.class);
				}
				// core.TypeConverter
				// throw new RuntimeException("You've got new type to mapping. Please add code in " + TableInfoBuilder.class.getName() + ". The ColumnClassName can't be mapped: " + colClassName);
			}
		}
		
		rs.close();
		stm.close();
		return result;
	}
}

