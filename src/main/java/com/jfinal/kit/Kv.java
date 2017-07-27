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
 * Kv ---> Key Value 
 * Kv 用于取代 JMap，前者输入量少，且输入更顺滑
 * 
 * 参数或者返回值封装，常用于业务层传参与返回值
 * 
 * Example：
 * 1：Kv para = Kv.by("id", 123);
 *    User user = user.findFirst(getSqlPara("find", para));
 * 
 * 2：return Kv.fail("msg", "用户名或密码错误");	// 登录失败返回
 *   return Kv.ok("loginUser", user);			// 登录成功返回
 * 
 * 3：Kv kv = loginService.login(...);
 *   renderJson(kv);
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class Kv extends HashMap {

	private static final String STATE_OK = "isOk";
	private static final String STATE_FAIL = "isFail";
	
	// 状态联动，与 Ret 不同，这里的默认值为 false
	private static boolean stateLinkage = false;
	
	/**
	 * 设置状态联动
	 * <pre>
	 * 1：设置为 true，则在 setOk() 与 setFail() 中，同时处理 isOk 与 isFail 两个状态
	 * 2：设置为 false，则 setOk() 与 setFile() 只处理与其相关的一个状态
	 * 3：设置为联动状态，有利于 javascript 中 if(isOk) 与 if(isFail) 的判断逻辑
	 * 4：设置为非联动状态，有利于通信数据为 json 的 API 服务端项目，节省一个键值对的生成
	 * </pre>
	 */
	public static void setStateLinkage(boolean stateLinkage) {
		Kv.stateLinkage = stateLinkage;
	}
	
	public Kv() {
	}
	
	public static Kv by(Object key, Object value) {
		return new Kv().set(key, value);
	}
	
	public static Kv create() {
		return new Kv();
	}
	
	public static Kv ok() {
		return new Kv().setOk();
	}
	
	public static Kv ok(Object key, Object value) {
		return ok().set(key, value);
	}
	
	public static Kv fail() {
		return new Kv().setFail();
	}
	
	public static Kv fail(Object key, Object value) {
		return fail().set(key, value);
	}
	
	public Kv setOk() {
		super.put(STATE_OK, Boolean.TRUE);
		if (stateLinkage) {
			super.put(STATE_FAIL, Boolean.FALSE);
		}
		return this;
	}
	
	public Kv setFail() {
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
	
	public boolean equals(Object kv) {
		return kv instanceof Kv && super.equals(kv);
	}
}


