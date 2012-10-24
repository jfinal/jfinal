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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Record
 */
public class Record implements Serializable {
	
	private static final long serialVersionUID = -3254070837297655225L;
	@SuppressWarnings("unchecked")
	private Map<String, Object> columns = DbKit.mapFactory.getColumnsMap();	// new HashMap<String, Object>();
	
	/**
	 * Return columns map.
	 */
	public Map<String, Object> getColumns() {
		return columns;
	}
	
	/**
	 * Set columns value with map.
	 * @param columns the columns map
	 */
	public Record setColumns(Map<String, Object> columns) {
		this.columns.putAll(columns);
		return this;
	}
	
	/**
	 * Set columns value with record.
	 * @param record the record
	 */
	public Record setColumns(Record record) {
		columns.putAll(record.getColumns());
		return this;
	}
	
	/**
	 * Remove attribute of this record.
	 * @param column the column name of the record
	 */
	public Record remove(String column) {
		columns.remove(column);
		return this;
	}
	
	/**
	 * Remove columns of this record.
	 * @param columns the column names of the record
	 */
	public Record remove(String... columns) {
		if (columns != null)
			for (String c : columns)
				this.columns.remove(c);
		return this;
	}
	
	/**
	 * Remove columns if it is null.
	 */
	public Record removeNullValueColumns() {
		for (java.util.Iterator<Entry<String, Object>> it = columns.entrySet().iterator(); it.hasNext();) {
			Entry<String, Object> e = it.next();
			if (e.getValue() == null) {
				it.remove();
			}
		}
		return this;
	}
	
	/**
	 * Keep columns of this record and remove other columns.
	 * @param columns the column names of the record
	 */
	public Record keep(String... columns) {
		if (columns != null && columns.length > 0) {
			Map<String, Object> newColumns = new HashMap<String, Object>(columns.length);
			for (String c : columns)
				if (this.columns.containsKey(c))	// prevent put null value to the newColumns
					newColumns.put(c, this.columns.get(c));
			this.columns = newColumns;
		}
		else
			this.columns.clear();
		return this;
	}
	
	/**
	 * Keep column of this record and remove other columns.
	 * @param column the column names of the record
	 */
	public Record keep(String column) {
		if (columns.containsKey(column)) {	// prevent put null value to the newColumns
			Object keepIt = columns.get(column);
			columns.clear();
			columns.put(column, keepIt);
		}
		else
			columns.clear();
		return this;
	}
	
	/**
	 * Remove all columns of this record.
	 */
	public Record clear() {
		columns.clear();
		return this;
	}
	
	/**
	 * Set column to record.
	 * @param column the column name
	 * @param value the value of the column
	 */
	public Record set(String column, Object value) {
		columns.put(column, value);
		return this;
	}
	
	/**
	 * Get column of any mysql type
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String column) {
		return (T)columns.get(column);
	}
	
	/**
	 * Get column of any mysql type. Returns defaultValue if null.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String column, Object defaultValue) {
		Object result = columns.get(column);
		return (T)(result != null ? result : defaultValue);
	}
	
	/**
	 * Get column of mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
	 */
	public String getStr(String column) {
		return (String)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: int, integer, tinyint(n) n > 1, smallint, mediumint
	 */
	public Integer getInt(String column) {
		return (Integer)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: bigint
	 */
	public Long getLong(String column) {
		return (Long)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: date, year
	 */
	public java.sql.Date getDate(String column) {
		return (java.sql.Date)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: time
	 */
	public java.sql.Time getTime(String column) {
		return (java.sql.Time)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: timestamp, datetime
	 */
	public java.sql.Timestamp getTimestamp(String column) {
		return (java.sql.Timestamp)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: real, double
	 */
	public Double getDouble(String column) {
		return (Double)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: float
	 */
	public Float getFloat(String column) {
		return (Float)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: bit, tinyint(1)
	 */
	public Boolean getBoolean(String column) {
		return (Boolean)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: decimal, numeric
	 */
	public java.math.BigDecimal getBigDecimal(String column) {
		return (java.math.BigDecimal)columns.get(column);
	}
	
	/**
	 * Get column of mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob
	 * I have not finished the test.
	 */
	public byte[] getBytes(String column) {
		return (byte[])columns.get(column);
	}
	
	/**
	 * Get column of any type that extends from Number
	 */
	public Number getNumber(String column) {
		return (Number)columns.get(column);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append(" {");
		boolean first = true;
		for (Entry<String, Object> e : columns.entrySet()) {
			if (first)
				first = false;
			else
				sb.append(", ");
			
			Object value = e.getValue();
			if (value != null)
				value = value.toString();
			sb.append(e.getKey()).append(":").append(value);
		}
		sb.append("}");
		return sb.toString();
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Record))
            return false;
		if (o == this)
			return true;
		return this.columns.equals(((Record)o).columns);
	}
	
	public int hashCode() {
		return columns == null ? 0 : columns.hashCode();
	}
	
	/**
	 * Return column names of this record.
	 */
	public String[] getcolumnNames() {
		Set<String> attrNameSet = columns.keySet();
		return attrNameSet.toArray(new String[attrNameSet.size()]);
	}
	
	/**
	 * Return column values of this record.
	 */
	public Object[] getcolumnValues() {
		java.util.Collection<Object> attrValueCollection = columns.values();
		return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
	}
	
	/**
	 * Return json string of this record.
	 */
	public String toJson() {
		return com.jfinal.util.JsonBuilder.toJson(columns, 4);
	}
}




