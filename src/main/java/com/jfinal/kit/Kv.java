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
 * Kv (Key Value)
 * 
 * Example：
 *    Kv para = Kv.by("id", 123);
 *    User user = user.findFirst(getSqlPara("find", para));
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class Kv extends HashMap {
	
	@Deprecated
	private static final String STATE_OK = "isOk";
	@Deprecated
	private static final String STATE_FAIL = "isFail";
	
	public Kv() {
	}
	
	public static Kv by(Object key, Object value) {
		return new Kv().set(key, value);
	}
	
	public static Kv create() {
		return new Kv();
	}
	
	@Deprecated
	public static Kv ok() {
		return new Kv().setOk();
	}
	
	@Deprecated
	public static Kv ok(Object key, Object value) {
		return ok().set(key, value);
	}
	
	@Deprecated
	public static Kv fail() {
		return new Kv().setFail();
	}
	
	@Deprecated
	public static Kv fail(Object key, Object value) {
		return fail().set(key, value);
	}
	
	@Deprecated
	public Kv setOk() {
		super.put(STATE_OK, Boolean.TRUE);
		super.put(STATE_FAIL, Boolean.FALSE);
		return this;
	}
	
	@Deprecated
	public Kv setFail() {
		super.put(STATE_FAIL, Boolean.TRUE);
		super.put(STATE_OK, Boolean.FALSE);
		return this;
	}
	
	@Deprecated
	public boolean isOk() {
		Boolean isOk = (Boolean)get(STATE_OK);
		if (isOk != null) {
			return isOk;
		}
		Boolean isFail = (Boolean)get(STATE_FAIL);
		if (isFail != null) {
			return !isFail;
		}
		
		throw new IllegalStateException("调用 isOk() 之前，必须先调用 ok()、fail() 或者 setOk()、setFail() 方法");
	}
	
	@Deprecated
	public boolean isFail() {
		Boolean isFail = (Boolean)get(STATE_FAIL);
		if (isFail != null) {
			return isFail;
		}
		Boolean isOk = (Boolean)get(STATE_OK);
		if (isOk != null) {
			return !isOk;
		}
		
		throw new IllegalStateException("调用 isFail() 之前，必须先调用 ok()、fail() 或者 setOk()、setFail() 方法");
	}
	
	public Kv set(Object key, Object value) {
		super.put(key, value);
		return this;
	}
	
	public Kv set(Map map) {
		super.putAll(map);
		return this;
	}
	
	public Kv set(Kv kv) {
		super.putAll(kv);
		return this;
	}
	
	public Kv delete(Object key) {
		super.remove(key);
		return this;
	}
	
	public <T> T getAs(Object key) {
		return (T)get(key);
	}
	
	public String getStr(Object key) {
		Object s = get(key);
		return s != null ? s.toString() : null;
	}
	
	public Integer getInt(Object key) {
		Number n = (Number)get(key);
		return n != null ? n.intValue() : null;
	}
	
	public Long getLong(Object key) {
		Number n = (Number)get(key);
		return n != null ? n.longValue() : null;
	}
	
	public Number getNumber(Object key) {
		return (Number)get(key);
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
	
	public boolean equals(Object kv) {
		return kv instanceof Kv && super.equals(kv);
	}
}


