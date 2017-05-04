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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RecordBuilder.
 */
public class RecordBuilder {
	
	public static final RecordBuilder me = new RecordBuilder();
	
	@SuppressWarnings("unchecked")
	public List<Record> build(Config config, ResultSet rs) throws SQLException {
		List<Record> result = new ArrayList<Record>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		String[] labelNames = new String[columnCount + 1];
		int[] types = new int[columnCount + 1];
		buildLabelNamesAndTypes(rsmd, labelNames, types);
		while (rs.next()) {
			Record record = new Record();
			record.setColumnsMap(config.containerFactory.getColumnsMap());
			Map<String, Object> columns = record.getColumns();
			for (int i=1; i<=columnCount; i++) {
				Object value;
				if (types[i] < Types.BLOB)
					value = rs.getObject(i);
				else if (types[i] == Types.CLOB)
					value = ModelBuilder.me.handleClob(rs.getClob(i));
				else if (types[i] == Types.NCLOB)
					value = ModelBuilder.me.handleClob(rs.getNClob(i));
				else if (types[i] == Types.BLOB)
					value = ModelBuilder.me.handleBlob(rs.getBlob(i));
				else
					value = rs.getObject(i);
				
				columns.put(labelNames[i], value);
			}
			result.add(record);
		}
		return result;
	}
	
	public void buildLabelNamesAndTypes(ResultSetMetaData rsmd, String[] labelNames, int[] types) throws SQLException {
		for (int i=1; i<labelNames.length; i++) {
			labelNames[i] = rsmd.getColumnLabel(i);
			types[i] = rsmd.getColumnType(i);
		}
	}
	
	/* backup before use columnType
	static final List<Record> build(ResultSet rs) throws SQLException {
		List<Record> result = new ArrayList<Record>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		String[] labelNames = getLabelNames(rsmd, columnCount);
		while (rs.next()) {
			Record record = new Record();
			Map<String, Object> columns = record.getColumns();
			for (int i=1; i<=columnCount; i++) {
				Object value = rs.getObject(i);
				columns.put(labelNames[i], value);
			}
			result.add(record);
		}
		return result;
	}
	
	private static final String[] getLabelNames(ResultSetMetaData rsmd, int columnCount) throws SQLException {
		String[] result = new String[columnCount + 1];
		for (int i=1; i<=columnCount; i++)
			result[i] = rsmd.getColumnLabel(i);
		return result;
	}
	*/
	
	/* backup
	static final List<Record> build(ResultSet rs) throws SQLException {
		List<Record> result = new ArrayList<Record>();
		ResultSetMetaData rsmd = rs.getMetaData();
		List<String> labelNames = getLabelNames(rsmd);
		while (rs.next()) {
			Record record = new Record();
			Map<String, Object> columns = record.getColumns();
			for (String lableName : labelNames) {
				Object value = rs.getObject(lableName);
				columns.put(lableName, value);
			}
			result.add(record);
		}
		return result;
	}
	
	private static final List<String> getLabelNames(ResultSetMetaData rsmd) throws SQLException {
		int columCount = rsmd.getColumnCount();
		List<String> result = new ArrayList<String>();
		for (int i=1; i<=columCount; i++) {
			result.add(rsmd.getColumnLabel(i));
		}
		return result;
	}
	*/
}





