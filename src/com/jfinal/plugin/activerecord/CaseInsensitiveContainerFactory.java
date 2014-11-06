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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CaseInsensitiveContainerFactory implements IContainerFactory {
	
	private static boolean toLowerCase = false;
	
	public CaseInsensitiveContainerFactory() {
	}
	
	public CaseInsensitiveContainerFactory(boolean toLowerCase) {
		CaseInsensitiveContainerFactory.toLowerCase = toLowerCase;
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
	
	private static Object convertCase(Object key) {
		if (key instanceof String)
			return toLowerCase ? ((String)key).toLowerCase() : ((String)key).toUpperCase();
		return key;
	}
	
	/*
	 * 1：非静态内部类拥有对外部类的所有成员的完全访问权限，包括实例字段和方法，
	 *    为实现这一行为，非静态内部类存储着对外部类的实例的一个隐式引用
	 * 2：序列化时要求所有的成员变量是Serializable 包括上面谈到的引式引用
	 * 3：外部类CaseInsensitiveContainerFactory 需要 implements Serializable 才能被序列化
	 * 4：可以使用静态内部类来实现内部类的序列化，而非让外部类实现 implements Serializable 
	 */
	public static class CaseInsensitiveSet extends HashSet {
		
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
		
		public boolean addAll(Collection c) {
			boolean modified = false;
			for (Object o : c)
				if (super.add(convertCase(o)))
					modified = true;
			return modified;
		}
	}
	
	public static class CaseInsensitiveMap extends HashMap {
		
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
				super.put(convertCase(e.getKey()), e.getValue());
		}
		
		public Object remove(Object key) {
			return super.remove(convertCase(key));
		}
	}
}

