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

package com.jfinal.kit;

import java.util.HashMap;
import java.util.Map;
import com.jfinal.json.Json;

/**
 * 返回值封装，常用于业务层需要多个返回值
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class Ret extends HashMap {

	private static final String STATE_OK = "isOk";
	private static final String STATE_FAIL = "isFail";
	
	// 状态联动
	private static boolean stateLinkage = false;
	
	/**
	 * 设置状态联动
	 * <pre>
	 * 1：设置为 true，则在 setOk() 与 setFail() 中，同时处理 isOk 与 isFail 两个状态
	 * 2：设置为 false，则 setOk() 与 setFile() 只处理与其相关的一个状态
	 * </pre>
	 */
	public static void setStateLinkage(boolean stateLinkage) {
		Ret.stateLinkage = stateLinkage;
	}
	
	public Ret() {
	}
	
	public static Ret by(Object key, Object value) {
		return new Ret().set(key, value);
	}
	
	public static Ret create(Object key, Object value) {
		return new Ret().set(key, value);
	}
	
	public static Ret create() {
		return new Ret();
	}
	
	public static Ret ok() {
		return new Ret().setOk();
	}
	
	public static Ret ok(Object key, Object value) {
		return ok().set(key, value);
	}
	
	public static Ret fail() {
		return new Ret().setFail();
	}
	
	public static Ret fail(Object key, Object value) {
		return fail().set(key, value);
	}
	
	public Ret setOk() {
		super.put(STATE_OK, Boolean.TRUE);
		if (stateLinkage) {
			super.put(STATE_FAIL, Boolean.FALSE);
		}
		return this;
	}
	
	public Ret setFail() {
		super.put(STATE_FAIL, Boolean.TRUE);
		if (stateLinkage) {
			super.put(STATE_OK, Boolean.FALSE);
		}
		return this;
	}
	
	public boolean isOk() {
		Boolean isOk = (Boolean)get(STATE_OK);
		return isOk != null && isOk;
	}
	
	public boolean isFail() {
		Boolean isFail = (Boolean)get(STATE_FAIL);
		return isFail != null && isFail;
	}
	
	public Ret set(Object key, Object value) {
		super.put(key, value);
		return this;
	}
	
	public Ret set(Map map) {
		super.putAll(map);
		return this;
	}
	
	public Ret set(Ret ret) {
		super.putAll(ret);
		return this;
	}
	
	public Ret delete(Object key) {
		super.remove(key);
		return this;
	}
	
	public <T> T getAs(Object key) {
		return (T)get(key);
	}
	
	public String getStr(Object key) {
		return (String)get(key);
	}

	public Integer getInt(Object key) {
		return (Integer)get(key);
	}

	public Long getLong(Object key) {
		return (Long)get(key);
	}

	public Boolean getBoolean(Object key) {
		return (Boolean)get(key);
	}
	
	/**
	 * key 存在，并且 value 不为 null
	 */
	public boolean notNull(Object key) {
		return get(key) != null;
	}
	
	/**
	 * key 不存在，或者 key 存在但 value 为null
	 */
	public boolean isNull(Object key) {
		return get(key) == null;
	}
	
	/**
	 * key 存在，并且 value 为 true，则返回 true
	 */
	public boolean isTrue(Object key) {
		Object value = get(key);
		return (value instanceof Boolean && ((Boolean)value == true));
	}
	
	/**
	 * key 存在，并且 value 为 false，则返回 true
	 */
	public boolean isFalse(Object key) {
		Object value = get(key);
		return (value instanceof Boolean && ((Boolean)value == false));
	}
	
	public String toJson() {
		return Json.getJson().toJson(this);
	}
	
	public boolean equals(Object ret) {
		return ret instanceof Ret && super.equals(ret);
	}
}


