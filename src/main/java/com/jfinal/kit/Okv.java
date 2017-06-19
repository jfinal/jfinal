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

import java.util.LinkedHashMap;
import java.util.Map;
import com.jfinal.json.Json;

/**
 * Okv ---> Ordered Key Value 
 * 
 * Okv 与 Kv 的唯一区别在于 Okv 继承自 LinkedHashMap，而 Kv 继承自 HashMap
 * 所以对 Okv 中的数据进行迭代输出的次序与数据插入的先后次序一致
 * 
 * 参数或者返回值封装，常用于业务层传参与返回值
 * 
 * Example：
 * 1：Okv para = Okv.by("id", 123);
 *    User user = user.findFirst(getSqlPara("find", para));
 * 
 * 2：return Okv.fail("msg", "用户名或密码错误");	// 登录失败返回
 *   return Okv.ok("loginUser", user);			// 登录成功返回
 * 
 * 3：Okv okv = loginService.login(...);
 *   renderJson(okv);
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class Okv extends LinkedHashMap {

	private static final String STATUS_OK = "isOk";
	private static final String STATUS_FAIL = "isFail";
	
	public Okv() {
	}
	
	public static Okv by(Object key, Object value) {
		return new Okv().set(key, value);
	}
	
	public static Okv create() {
		return new Okv();
	}
	
	public static Okv ok() {
		return new Okv().setOk();
	}
	
	public static Okv ok(Object key, Object value) {
		return ok().set(key, value);
	}
	
	public static Okv fail() {
		return new Okv().setFail();
	}
	
	public static Okv fail(Object key, Object value) {
		return fail().set(key, value);
	}
	
	public Okv setOk() {
		super.put(STATUS_OK, Boolean.TRUE);
		super.put(STATUS_FAIL, Boolean.FALSE);
		return this;
	}
	
	public Okv setFail() {
		super.put(STATUS_OK, Boolean.FALSE);
		super.put(STATUS_FAIL, Boolean.TRUE);
		return this;
	}
	
	public boolean isOk() {
		Boolean isOk = (Boolean)get(STATUS_OK);
		return isOk != null && isOk;
	}
	
	public boolean isFail() {
		Boolean isFail = (Boolean)get(STATUS_FAIL);
		return isFail != null && isFail;
	}
	
	public Okv set(Object key, Object value) {
		super.put(key, value);
		return this;
	}
	
	public Okv set(Map map) {
		super.putAll(map);
		return this;
	}
	
	public Okv set(Okv okv) {
		super.putAll(okv);
		return this;
	}
	
	public Okv delete(Object key) {
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
	
	public boolean equals(Object okv) {
		return okv instanceof Okv && super.equals(okv);
	}
}


