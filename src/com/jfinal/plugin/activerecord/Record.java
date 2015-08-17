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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Record
 */
public class Record implements Serializable {
	
	private static final long serialVersionUID = 905784513600884082L;
	
	private Map<String, Object> columns;	// = getColumnsMap();	// getConfig().containerFactory.getColumnsMap();	// new HashMap<String, Object>();
	
	/**
	 * Set the containerFactory by configName.
	 * Only the containerFactory of the config used by Record for getColumnsMap()
	 * @param configName the config name
	 */
	public Record setContainerFactoryByConfigName(String configName) {
		Config config = DbKit.getConfig(configName);
		if (config == null)
			throw new IllegalArgumentException("Config not found: " + configName);
		
		processColumnsMap(config);
		return this;
	}
	
	// Only used by RecordBuilder
	void setColumnsMap(Map<String, Object> columns) {
		this.columns = columns;
	}
	
	@SuppressWarnings("unchecked")
	private void processColumnsMap(Config config) {
		if (columns == null || columns.size() == 0) {
			columns = config.containerFactory.getColumnsMap();
		} else {
			Map<String, Object> columnsOld = columns;
			columns = config.containerFactory.getColumnsMap();
			columns.putAll(columnsOld);
		}
	}
	
	/**
	 * Return columns map.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getColumns() {
		if (columns == null) {
			if (DbKit.config == null)
				columns = DbKit.brokenConfig.containerFactory.getColumnsMap();
			else
				columns = DbKit.config.containerFactory.getColumnsMap();
		}
		return columns;
	}
	
	/**
	 * Set columns value with map.
	 * @param columns the columns map
	 */
	public Record setColumns(Map<String, Object> columns) {
		this.getColumns().putAll(columns);
		return this;
	}
	
	/**
	 * Set columns value with Record.
	 * @param record the Record object
	 */
	public Record setColumns(Record record) {
		getColumns().putAll(record.getColumns());
		return this;
	}
	
	/**
	 * Set columns value with Model object.
	 * @param model the Model object
	 */
	public Record setColumns(Model<?> model) {
		getColumns().putAll(model.getAttrs());
		return this;
	}
	
	/**
	 * Remove attribute of this record.
	 * @param column the column name of the record
	 */
	public Record remove(String column) {
		getColumns().remove(column);
		return this;
	}
	
	/**
	 * Remove columns of this record.
	 * @param columns the column names of the record
	 */
	public Record remove(String... columns) {
		if (columns != null)
			for (String c : columns)
				this.getColumns().remove(c);
		return this;
	}
	
	/**
	 * Remove columns if it is null.
	 */
	public Record removeNullValueColumns() {
		for (java.util.Iterator<Entry<String, Object>> it = getColumns().entrySet().iterator(); it.hasNext();) {
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
			Map<String, Object> newColumns = new HashMap<String, Object>(columns.length);	// getConfig().containerFactory.getColumnsMap();
			for (String c : columns)
				if (this.getColumns().containsKey(c))	// prevent put null value to the newColumns
					newColumns.put(c, this.getColumns().get(c));
			
			this.getColumns().clear();
			this.getColumns().putAll(newColumns);
		}
		else
			this.getColumns().clear();
		return this;
	}
	
	/**
	 * Keep column of this record and remove other columns.
	 * @param column the column names of the record
	 */
	public Record keep(String column) {
		if (getColumns().containsKey(column)) {	// prevent put null value to the newColumns
			Object keepIt = getColumns().get(column);
			getColumns().clear();
			getColumns().put(column, keepIt);
		}
		else
			getColumns().clear();
		return this;
	}
	
	/**
	 * Remove all columns of this record.
	 */
	public Record clear() {
		getColumns().clear();
		return this;
	}
	
	/**
	 * Set column to record.
	 * @param column the column name
	 * @param value the value of the column
	 */
	public Record set(String column, Object value) {
		getColumns().put(column, value);
		return this;
	}
	
	/**
	 * Get column of any mysql type
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String column) {
		return (T)getColumns().get(column);
	}
	
	/**
	 * Get column of any mysql type. Returns defaultValue if null.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String column, Object defaultValue) {
		Object result = getColumns().get(column);
		return (T)(result != null ? result : defaultValue);
	}
	
	/**
	 * Get column of mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
	 */
	public String getStr(String column) {
		return (String)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: int, integer, tinyint(n) n > 1, smallint, mediumint
	 */
	public Integer getInt(String column) {
		return (Integer)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: bigint
	 */
	public Long getLong(String column) {
		return (Long)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: unsigned bigint
	 */
	public java.math.BigInteger getBigInteger(String column) {
		return (java.math.BigInteger)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: date, year
	 */
	public java.util.Date getDate(String column) {
		return (java.util.Date)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: time
	 */
	public java.sql.Time getTime(String column) {
		return (java.sql.Time)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: timestamp, datetime
	 */
	public java.sql.Timestamp getTimestamp(String column) {
		return (java.sql.Timestamp)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: real, double
	 */
	public Double getDouble(String column) {
		return (Double)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: float
	 */
	public Float getFloat(String column) {
		return (Float)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: bit, tinyint(1)
	 */
	public Boolean getBoolean(String column) {
		return (Boolean)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: decimal, numeric
	 */
	public java.math.BigDecimal getBigDecimal(String column) {
		return (java.math.BigDecimal)getColumns().get(column);
	}
	
	/**
	 * Get column of mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob
	 * I have not finished the test.
	 */
	public byte[] getBytes(String column) {
		return (byte[])getColumns().get(column);
	}
	
	/**
	 * Get column of any type that extends from Number
	 */
	public Number getNumber(String column) {
		return (Number)getColumns().get(column);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append(" {");
		boolean first = true;
		for (Entry<String, Object> e : getColumns().entrySet()) {
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
		return this.getColumns().equals(((Record)o).getColumns());
	}
	
	public int hashCode() {
		return getColumns() == null ? 0 : getColumns().hashCode();
	}
	
	/**
	 * Return column names of this record.
	 */
	public String[] getColumnNames() {
		Set<String> attrNameSet = getColumns().keySet();
		return attrNameSet.toArray(new String[attrNameSet.size()]);
	}
	
	/**
	 * Return column values of this record.
	 */
	public Object[] getColumnValues() {
		java.util.Collection<Object> attrValueCollection = getColumns().values();
		return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
	}
	
	/**
	 * Return json string of this record.
	 */
	public String toJson() {
		return com.jfinal.kit.JsonKit.toJson(getColumns(), 4);
	}
}




