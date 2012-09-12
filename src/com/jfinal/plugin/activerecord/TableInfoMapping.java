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

import java.util.HashMap;
import java.util.Map;

/**
 * TableInfoMapping save the mapping of model class and table info.
 */
public class TableInfoMapping {

	private static final Map<Class<? extends Model<?>>, TableInfo> tableInfoMap = new HashMap<Class<? extends Model<?>>, TableInfo>();
	
	private static TableInfoMapping me = new TableInfoMapping(); 
	
	// singleton
	private TableInfoMapping() {
	}
	
	public static TableInfoMapping me() {
		return me;
	}
	
	@SuppressWarnings("rawtypes")
	public TableInfo getTableInfo(Class<? extends Model> modelClass) {
		TableInfo result = tableInfoMap.get(modelClass);
		if (result == null) {
			throw new RuntimeException("The TableMapping of model: " + modelClass.getName() + " not exists. Please add mapping to ActiveRecordPlugin(activeRecordPlugin.addMapping(tableName, YourModel.class)).");
		}
		return result;
	}
	
	public void putTableInfo(Class<? extends Model<?>> modelClass, TableInfo tableInfo) {
		tableInfoMap.put(modelClass, tableInfo);
	}
}



