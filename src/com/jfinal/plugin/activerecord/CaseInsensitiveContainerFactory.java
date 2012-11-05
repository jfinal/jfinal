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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CaseInsensitiveContainerFactory implements IContainerFactory {
	
	private boolean toLowerCase = false;
	
	public CaseInsensitiveContainerFactory() {
	}
	
	public CaseInsensitiveContainerFactory(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}
	
	public Map<String, Object> getAttrsMap() {
		return new CaseInsensitiveMap();
	}
	
	public Map<String, Object> getColumnsMap() {
		return new CaseInsensitiveMap();
	}
	
	public Set<String> getModifyFlagSet() {
		return new CaseInsensitiveSet();
	}
	
	private Object convertCase(Object key) {
		if (key instanceof String)
			return toLowerCase ? ((String)key).toLowerCase() : ((String)key).toUpperCase();
		return key;
	}
	
	class CaseInsensitiveSet extends HashSet {
		
		private static final long serialVersionUID = 102410961064096233L;
		
		public boolean add(Object e) {
			return super.add(convertCase(e));
		}
		
		public boolean remove(Object e) {
			return super.remove(convertCase(e));
		}
		
		public boolean contains(Object e) {
			return super.contains(convertCase(e));
		}
	}
	
	class CaseInsensitiveMap extends HashMap {
		
		private static final long serialVersionUID = 6843981594457576677L;
		
		public Object get(Object key) {
			return super.get(convertCase(key));
		}
		
		public boolean containsKey(Object key) {
			return super.containsKey(convertCase(key));
		}
		
		public Object put(Object key, Object value) {
			return super.put(convertCase(key), value);
		}
		
		public void putAll(Map m) {
			for (Map.Entry e : (Set<Map.Entry>)(m.entrySet()))
	            put(e.getKey(), e.getValue());
		}
		
		public Object remove(Object key) {
			return super.remove(convertCase(key));
		}
	}
}

