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

package com.jfinal.plugin.activerecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

/**
 * TableBuilder build the mapping of model between class and table.
 */
class TableBuilder {
	
	private JavaType javaType = new JavaType();
	
	void build(List<Table> tableList, Config config) {
		// 支持 useAsDataTransfer(...) 中的 arp.start() 正常运作
		if (config.dataSource instanceof NullDataSource) {
			return ;
		}
		
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
			if (temp != null) {
				System.err.println("Can not create Table object, maybe the table " + temp.getName() + " is not exists.");
			}
			throw new ActiveRecordException(e);
		}
		finally {
			config.close(conn);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void doBuild(Table table, Connection conn, Config config) throws SQLException {
		table.setColumnTypeMap(config.containerFactory.getAttrsMap());
		if (table.getPrimaryKey() == null) {
			table.setPrimaryKey(config.dialect.getDefaultPrimaryKey());
		}
		
		String sql = config.dialect.forTableBuilderDoBuild(table.getName());
		Statement stm = conn.createStatement();
		ResultSet rs = stm.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		
		// setColumnType(...) 置入的 java 类型主要用于 core 包下面的 parameter 转成正确的 java 类型
		for (int i=1; i<=rsmd.getColumnCount(); i++) {
			String colName = rsmd.getColumnName(i);
			String colClassName = rsmd.getColumnClassName(i);
			
			Class<?> clazz = javaType.getType(colClassName);
			if (clazz != null) {
				table.setColumnType(colName, clazz);
			}
			else {
				int type = rsmd.getColumnType(i);
				if (type == Types.BINARY || type == Types.VARBINARY || type == Types.BLOB) {
					table.setColumnType(colName, byte[].class);
				}
				else if (type == Types.CLOB || type == Types.NCLOB) {
					table.setColumnType(colName, String.class);
				}
				// 支持 oracle.sql.TIMESTAMP
				else if (type == Types.TIMESTAMP) {
					table.setColumnType(colName, java.sql.Timestamp.class);
				}
				// 支持 oracle.sql.DATE
				// 实际情况是 oracle DATE 字段仍然返回的是 Types.TIMESTAMP，而且 oralce 的 DATE 字段上的 getColumnClassName(i) 方法返回的是 java.sql.Timestamp 可以被正确处理
				// 所以，此处的 if 判断一是为了逻辑上的正确性、完备性，二是其它类型的数据库可能用得着
				else if (type == Types.DATE) {
					table.setColumnType(colName, java.sql.Date.class);
				}
				// 支持 PostgreSql 的 jsonb json
				else if (type == Types.OTHER) {
					table.setColumnType(colName, Object.class);
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

