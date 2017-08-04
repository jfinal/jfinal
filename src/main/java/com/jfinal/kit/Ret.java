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
 * Ret 用于返回值封装，也用于服务端与客户端的 json 数据通信
 * 
 * <pre>
 * 一、主要应用场景：
 * 1：业务层需要返回多个返回值，例如要返回业务状态以及数据
 * 2：renderJson(ret) 响应 json 数据给客户端
 * 
 * 二、两种工作模式：
 * 1：默认情况下即为新工作模式，当调用 ok()、fail()、setOk()、setFail() 方法之后
 *    Ret 生成的 json 数据的状态属性为：state:ok 以及 state:fail
 * 
 * 2：通过调用 Ret.setToOldWorkMode() 可以切换到旧工作模式，与新工作模式的不同在于
 *    Ret 生成的 json 数据的状态属性为：isOk:true/false 以及 isFail:true/false
 * 
 * 3：旧工作模式为了兼容 JFinal 3.2 之前的版本而保留，强烈建议使用新工作模式
 *    新工作模式非常有利于使用 json 数据格式的 API 类型项目
 * 
 * 三、实例
 * 1：服务端
 *    Ret ret = service.justDoIt(paras);
 *    renderJson(ret);
 * 
 * 2：javascript 客户端 ajax 回调函数通常这么用：
 *    success: function(ret) {
 *       if(ret.state == "ok") {
 *       	...
 *       }
 *       
 *       if (ret.state == "fail") {
 *       	...
 *       }
 *    }
 *  
 * 3：普通应用程序通常这么用：
 *   String json = HttpKit.readData(getRequest());
 *   Ret ret = FastJson.getJson().parse(json, Ret.class);
 *   if (ret.isOk()) {
 *   	...
 *   }
 *   
 *   if (ret.isFail()) {
 *   	...
 *   }
 *   
 * </pre>
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class Ret extends HashMap {
	
	private static final String STATE = "state";
	private static final String STATE_OK = "ok";
	private static final String STATE_FAIL = "fail";
	
	// 以下为旧工作模式下的常量名
	private static final String OLD_STATE_OK = "isOk";
	private static final String OLD_STATE_FAIL = "isFail";
	
	// 默认为新工作模式
	private static boolean newWorkMode = true;
	
	/**
	 * 设置为旧工作模式，为了兼容 jfinal 3.2 之前的版本
	 */
	public static void setToOldWorkMode() {
		newWorkMode = false;
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
		if (newWorkMode) {
			super.put(STATE, STATE_OK);
		} else {
			super.put(OLD_STATE_OK, Boolean.TRUE);
			super.put(OLD_STATE_FAIL, Boolean.FALSE);
		}
		return this;
	}
	
	public Ret setFail() {
		if (newWorkMode) {
			super.put(STATE, STATE_FAIL);
		} else {
			super.put(OLD_STATE_FAIL, Boolean.TRUE);
			super.put(OLD_STATE_OK, Boolean.FALSE);
		}
		return this;
	}
	
	public boolean isOk() {
		if (newWorkMode) {
			Object state = get(STATE);
			if (STATE_OK.equals(state)) {
				return true;
			}
			if (STATE_FAIL.equals(state)) {
				return false;
			}
		} else {
			Boolean isOk = (Boolean)get(OLD_STATE_OK);
			if (isOk != null) {
				return isOk;
			}
			Boolean isFail = (Boolean)get(OLD_STATE_FAIL);
			if (isFail != null) {
				return !isFail;
			}
		}
		
		throw new IllegalStateException("调用 isOk() 之前，必须先调用 ok()、fail() 或者 setOk()、setFail() 方法");
	}
	
	public boolean isFail() {
		if (newWorkMode) {
			Object state = get(STATE);
			if (STATE_FAIL.equals(state)) {
				return true;
			}
			if (STATE_OK.equals(state)) {
				return false;
			}
		} else {
			Boolean isFail = (Boolean)get(OLD_STATE_FAIL);
			if (isFail != null) {
				return isFail;
			}
			Boolean isOk = (Boolean)get(OLD_STATE_OK);
			if (isOk != null) {
				return !isOk;
			}
		}
		
		throw new IllegalStateException("调用 isFail() 之前，必须先调用 ok()、fail() 或者 setOk()、setFail() 方法");
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
	
	public boolean equals(Object ret) {
		return ret instanceof Ret && super.equals(ret);
	}
}


