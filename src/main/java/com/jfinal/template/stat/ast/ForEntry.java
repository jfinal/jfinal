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

package com.jfinal.template.stat.ast;

import java.util.Map.Entry;

/**
 * ForEntry 包装 HashMap、LinkedHashMap 等 Map 类型的 Entry 对象
 */
public class ForEntry implements Entry<Object, Object> {
	
	private Entry<Object, Object> entry;
	
	public ForEntry(Entry<Object, Object> entry) {
		this.entry = entry;
	}
	
	public Object getKey() {
		return entry.getKey();
	}
	
	public Object getValue() {
		return entry.getValue();
	}
	
	public Object setValue(Object value) {
		return entry.setValue(value);
	}
}



