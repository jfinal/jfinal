/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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
	
	private final Map<Class<? extends Model<?>>, Table> modelToTableMap = new HashMap<Class<? extends Model<?>>, Table>(512, 0.5F);
	
	private static TableMapping me = new TableMapping(); 
	
	private TableMapping() {}
	
	public static TableMapping me() {
		return me;
	}
	
	public void putTable(Table table) {
		if (modelToTableMap.containsKey(table.getModelClass())) {
			// 支持运行时动态添加 Table 映射，不再抛出异常
			// throw new RuntimeException("Model mapping already exists : " + table.getModelClass().getName());
			System.err.println("Model mapping already exists : " + table.getModelClass().getName());
		}
		
		modelToTableMap.put(table.getModelClass(), table);
	}
	
	@SuppressWarnings("rawtypes")
	public Table getTable(Class<? extends Model> modelClass) {
		return modelToTableMap.get(modelClass);
	}
}


