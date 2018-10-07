/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.kit;

import java.util.HashMap;
import java.util.Map;

/**
 * SyncWriteMap 同步写 HashMap
 * 创建原因是 HashMap扩容时，遇到并发修改可能造成 100% CPU 占用
 * 
 * SyncWriteMap 拥有 HashMap 的性能，但不保障并发访问的线程安全
 * 只用于读多写少且不用保障线程安全的场景
 * 
 * 例如 MethodKit 中用于缓存 MethodInfo 的 cache，被写入的数据
 * 不用保障是单例，读取之后会做 null 值判断
 * 
 * ActionMapping 中的 HashMap 是系统启动时在独立线程内初始化的，
 * 不存在并发写，只存在并发读的情况，所以仍然可以使用 HashMap
 */
public class SyncWriteMap<K, V> extends HashMap<K, V> {
	
	private static final long serialVersionUID = -7287230891751869148L;
	
	public SyncWriteMap() {
	}
	
	public SyncWriteMap(int initialCapacity) {
		super(initialCapacity);
	}
	
	public SyncWriteMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
	
	public SyncWriteMap(Map<? extends K, ? extends V> m) {
		super(m);
	}
	
	@Override
	public V put(K key, V value) {
		synchronized (this) {
			return super.put(key, value);
		}
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		synchronized (this) {
			return super.putIfAbsent(key, value);
		}
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		synchronized (this) {
			super.putAll(m);
		}
	}
	
	@Override
	public V remove(Object key) {
		synchronized (this) {
			return super.remove(key);
		}
	}
	
	@Override
	public void clear() {
		synchronized (this) {
			super.clear();
		}
	}
}




