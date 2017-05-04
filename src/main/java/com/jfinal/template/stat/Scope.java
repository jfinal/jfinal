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

package com.jfinal.template.stat;

import java.util.HashMap;
import java.util.Map;

/**
 * Scope
 * 1：顶层 scope.parent 为 null
 * 2：scope.set(...) 自内向外查找赋值
 * 3：scope.get(...) 自内向外查找获取
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Scope {
	
	private final Scope parent;
	private final Ctrl ctrl;
	private Map data;
	private Map<String, Object> sharedObjectMap;
	
	/**
	 * 构建顶层 Scope， parent 为 null 是顶层 Scope 的标志
	 * @param data 用于在模板中使用的数据，data 支持 null 值
	 * @param sharedObjectMap 共享对象
	 */
	public Scope(Map data, Map<String, Object> sharedObjectMap) {
		this.parent = null;
		this.ctrl = new Ctrl();
		this.data = data;
		this.sharedObjectMap = sharedObjectMap;
	}
	
	/**
	 * 构建 AST 执行过程中作用域栈
	 */
	public Scope(Scope parent) {
		if (parent == null) {
			throw new IllegalArgumentException("parent can not be null.");
		}
		this.parent = parent;
		this.ctrl = parent.ctrl;
		this.data = null;
		this.sharedObjectMap = parent.sharedObjectMap;
	}
	
	public Ctrl getCtrl() {
		return ctrl;
	}
	
	/**
	 * 设置变量
	 * 自内向外在作用域栈中查找变量，如果找到则改写变量值，否则将变量存放到顶层 Scope
	 */
	public void set(Object key, Object value) {
		for (Scope cur=this; true; cur=cur.parent) {
			// HashMap 允许有 null 值 value，必须要做 containsKey 判断
			if (cur.data != null && cur.data.containsKey(key)) {
				cur.data.put(key, value);
				return ;
			}
			
			if (cur.parent == null) {
				if (cur.data == null) {			// 支持顶层 data 为 null 值
					cur.data = new HashMap();
				}
				cur.data.put(key, value);
				return ;
			}
		}
	}
	
	/**
	 * 获取变量
	 * 自内向外在作用域栈中查找变量，返回最先找到的变量
	 */
	public Object get(Object key) {
		for (Scope cur=this; cur!=null; cur=cur.parent) {
			if (cur.data != null && cur.data.containsKey(key)) {
				return cur.data.get(key);
			}
		}
		// return null;
		return sharedObjectMap != null ? sharedObjectMap.get(key) : null;
	}
	
	/**
	 * 移除变量
	 * 自内向外在作用域栈中查找变量，移除最先找到的变量
	 */
	public void remove(Object key) {
		for (Scope cur=this; cur!=null; cur=cur.parent) {
			if (cur.data != null && cur.data.containsKey(key)) {
				cur.data.remove(key);
				return ;
			}
		}
	}
	
	/**
	 * 设置局部变量
	 */
	public void setLocal(Object key, Object value) {
		if (data == null) {
			data = new HashMap();
		}
		data.put(key, value);
	}
	
	/**
	 * 获取局部变量
	 */
	public Object getLocal(Object key) {
		return data != null ? data.get(key) : null;
	}
	
	/**
	 * 移除局部变量
	 */
	public void removeLocal(Object key) {
		if (data != null) {
			data.remove(key);
		}
	}
	
	/**
	 * 设置全局变量
	 * 全局作用域是指本次请求的整个 template
	 */
	public void setGlobal(Object key, Object value) {
		for (Scope cur=this; true; cur=cur.parent) {
			if (cur.parent == null) {
				cur.data.put(key, value);
				return ;
			}
		}
	}
	
	/**
	 * 获取全局变量
	 * 全局作用域是指本次请求的整个 template
	 */
	public Object getGlobal(Object key) {
		for (Scope cur=this; true; cur=cur.parent) {
			if (cur.parent == null) {
				return cur.data.get(key);
			}
		}
	}
	
	/**
	 * 移除全局变量
	 * 全局作用域是指本次请求的整个 template
	 */
	public void removeGlobal(Object key) {
		for (Scope cur=this; true; cur=cur.parent) {
			if (cur.parent == null) {
				cur.data.remove(key);
				return ;
			}
		}
	}
	
	/**
	 * 自内向外在作用域栈中查找变量，获取变量所在的 Map，主要用于 IncDec
	 */
	public Map getMapOfValue(Object key) {
		for (Scope cur=this; cur!=null; cur=cur.parent) {
			if (cur.data != null && cur.data.containsKey(key)) {
				return cur.data;
			}
		}
		return null;
	}
	
	/**
	 * 获取本层作用域 data，可能为 null 值
	 */
	public Map getData() {
		return data;
	}
	
	/**
	 * 设置/替换本层作用域 data，通常用于在扩展指令中使用现成可用的 Map 来存放数据，
	 * 从而避免 Scope 内部创建 data，节省时空
	 * 
	 * 注意：本方法会替换掉已经存在的 data 对象
	 */
	public void setData(Map data) {
		this.data = data;
	}
	
	/**
	 * 获取顶层作用域 data，可能为 null 值
	 */
	public Map getRootData() {
		for (Scope cur=this; true; cur=cur.parent) {
			if (cur.parent == null) {
				return cur.data;
			}
		}
	}
	
	/**
	 * 设置/替换顶层作用域 data，可以在扩展指令之中通过此方法切换掉顶层作用域
	 * 实现作用域完全隔离的功能
	 * 
	 * 注意：本方法会替换掉顶层已经存在的 data 对象
	 */
	public void setRootData(Map data) {
		for (Scope cur=this; true; cur=cur.parent) {
			if (cur.parent == null) {
				cur.data = data;
				return ;
			}
		}
	}
}



