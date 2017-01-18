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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * DaoContainerFactory
 */
public class DaoContainerFactory implements IContainerFactory {
	
	public static final Map<String, Object> daoMap = new DaoMap<Object>();
	public static final Set<String> daoSet = new DaoSet();
	
	private DaoContainerFactory() {
	}
	
	public Map<String, Object> getAttrsMap() {
		return daoMap;
	}
	
	public Map<String, Object> getColumnsMap() {
		return daoMap;
	}
	
	public Set<String> getModifyFlagSet() {
		return daoSet;
	}
	
	public static class DaoMap<V> implements Map<String, V> {
		
		public V put(String key, V value) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public void putAll(Map<? extends String, ? extends V> map) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public int size() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean isEmpty() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean containsKey(Object key) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean containsValue(Object value) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public V get(Object key) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public V remove(Object key) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public void clear() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public Set<String> keySet() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public Collection<V> values() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public Set<java.util.Map.Entry<String, V>> entrySet() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
	}
	
	public static class DaoSet implements Set<String> {
		
		public int size() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean isEmpty() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean contains(Object o) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public Iterator<String> iterator() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public Object[] toArray() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public <T> T[] toArray(T[] a) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean add(String e) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean remove(Object o) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean containsAll(Collection<?> c) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean addAll(Collection<? extends String> c) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean retainAll(Collection<?> c) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public boolean removeAll(Collection<?> c) {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
		
		public void clear() {
			throw new RuntimeException("dao 只允许调用查询方法");
		}
	}
}






