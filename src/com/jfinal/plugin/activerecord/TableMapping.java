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

import java.util.HashMap;
import java.util.Map;

/**
 * TableMapping save the mapping between model class and table.
 */
public class TableMapping {
	
	private final Map<Class<? extends Model<?>>, Table> modelToTableMap = new HashMap<Class<? extends Model<?>>, Table>();
	
	private static TableMapping me = new TableMapping(); 
	
	private TableMapping() {}
	
	public static TableMapping me() {
		return me;
	}
	
	public void putTable(Table table) {
		modelToTableMap.put(table.getModelClass(), table);
	}
	
	@SuppressWarnings("rawtypes")
	public Table getTable(Class<? extends Model> modelClass) {
		Table table = modelToTableMap.get(modelClass);
		if (table == null)
			throw new RuntimeException("The Table mapping of model: " + modelClass.getName() + " not exists. Please add mapping to ActiveRecordPlugin: activeRecordPlugin.addMapping(tableName, YourModel.class).");
		
		return table;
	}
}


