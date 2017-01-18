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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.jfinal.kit.StrKit;

/**
 * Table save the table meta info like column name and column type.
 */
public class Table {
	
	private String name;
	private String[] primaryKey = null;
	private Map<String, Class<?>> columnTypeMap;	// config.containerFactory.getAttrsMap();
	
	private Class<? extends Model<?>> modelClass;
	
	public Table(String name, Class<? extends Model<?>> modelClass) {
		if (StrKit.isBlank(name))
			throw new IllegalArgumentException("Table name can not be blank.");
		if (modelClass == null)
			throw new IllegalArgumentException("Model class can not be null.");
		
		this.name = name.trim();
		this.modelClass = modelClass;
	}
	
	public Table(String name, String primaryKey, Class<? extends Model<?>> modelClass) {
		if (StrKit.isBlank(name))
			throw new IllegalArgumentException("Table name can not be blank.");
		if (StrKit.isBlank(primaryKey))
			throw new IllegalArgumentException("Primary key can not be blank.");
		if (modelClass == null)
			throw new IllegalArgumentException("Model class can not be null.");
		
		this.name = name.trim();
		setPrimaryKey(primaryKey.trim());
		this.modelClass = modelClass;
	}
	
	void setPrimaryKey(String primaryKey) {
		String[] arr = primaryKey.split(",");
		for (int i=0; i<arr.length; i++)
			arr[i] = arr[i].trim();
		this.primaryKey = arr;
	}
	
	void setColumnTypeMap(Map<String, Class<?>> columnTypeMap) {
		if (columnTypeMap == null)
			throw new IllegalArgumentException("columnTypeMap can not be null");
		
		this.columnTypeMap = columnTypeMap;
	}
	
	public String getName() {
		return name;
	}
	
	void setColumnType(String columnLabel, Class<?> columnType) {
		columnTypeMap.put(columnLabel, columnType);
	}
	
	public Class<?> getColumnType(String columnLabel) {
		return columnTypeMap.get(columnLabel);
	}
	
	/**
	 * Model.save() need know what columns belongs to himself that he can saving to db.
	 * Think about auto saving the related table's column in the future.
	 */
	public boolean hasColumnLabel(String columnLabel) {
		return columnTypeMap.containsKey(columnLabel);
	}
	
	/**
	 * update() and delete() need this method.
	 */
	public String[] getPrimaryKey() {
		return primaryKey;
	}
	
	public Class<? extends Model<?>> getModelClass() {
		return modelClass;
	}
	
	public Map<String, Class<?>> getColumnTypeMap() {
		return Collections.unmodifiableMap(columnTypeMap);
	}
	
	public Set<Entry<String, Class<?>>> getColumnTypeMapEntrySet() {
		return columnTypeMap.entrySet();
	}
}





