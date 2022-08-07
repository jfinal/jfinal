/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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
 * 二、实例
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
 *   String json = getRawData();
 *   Ret ret = FastJson.getJson().parse(json, Ret.class);
 *   if (ret.isOk()) {
 *   	...
 *   }
 *   
 *   if (ret.isFail()) {
 *   	...
 *   }
 * 
 * 三、定制 Ret
 * 1：将状态字段名由 "state" 改为 "success"，将状态值 "ok" 改为 true、"fail" 改为 false
 *     CPI.setRetState("success", true, false);
 * 
 * 2：将状态字段名由 "state" 改为 "code"，将状态值 "ok" 改为 200、"fail" 改为 500
 *     CPI.setRetState("code", 200, 500);
 * 
 * 3：将消息字段名由 "msg" 改为 "message"
 *     CPI.setRetMsgName("message")
 * 
 * 4：配置 Ret 的 data(Object) 方法伴随 ok 状态，默认值为：false
 *     CPI.setRetDataWithOkState(true)
 * 
 * 5：配置监听 state 值，当值为 "ok" 时，额外放入 "success" 值为 true，否则为 false
 *     CPI.setRetStateWatcher((ret, state, value) -> {
 *         ret.set("success", "ok".equals(value));
 *     });
 *   在前后端分离项目中，有些前端框架需要该返回值："success" : true/false
 * 
 * 6：配置 Ret.isOk()、Ret.isFail() 在前两个 if 判断都没有 return 之后的处理回调
 *    用于支持多于两个状态的情况，也即在 ok、fail 两个状态之外还引入了其它状态
 *     CPI.setRetOkFailHandler((isOkMethod, value) -> {
 *         if (isOkMethod == Boolean.TRUE) {
 *             return false;
 *         } else {
 *             return true;
 *         }
 *     });
 * </pre>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Ret extends HashMap {
	
	private static final long serialVersionUID = -2150729333382285526L;
	
	/**
	 * 状态
	 */
	static String STATE = "state";
	static Object STATE_OK = "ok";
	static Object STATE_FAIL = "fail";
	static Func.F30<Ret, String, Object> stateWatcher = null;
	static Func.F21<Boolean, Object, Boolean> okFailHandler = null;
	
	/**
	 * 数据
	 */
	static String DATA = "data";
	static boolean dataWithOkState = false;			// data(Object) 方法伴随 ok 状态
	
	/**
	 * 消息
	 */
	static String MSG = "msg";
	
	public Ret() {
	}
	
	public static Ret of(Object key, Object value) {
		return new Ret().set(key, value);
	}
	
	public static Ret by(Object key, Object value) {
		return new Ret().set(key, value);
	}
	
	public static Ret create() {
		return new Ret();
	}
	
	public static Ret ok() {
		return new Ret().setOk();
	}
	
	public static Ret ok(String msg) {
		return new Ret().setOk()._setMsg(msg);
	}
	
	public static Ret ok(Object key, Object value) {
		return new Ret().setOk().set(key, value);
	}
	
	public static Ret fail() {
		return new Ret().setFail();
	}
	
	public static Ret fail(String msg) {
		return new Ret().setFail()._setMsg(msg);
	}
	
	@Deprecated
	public static Ret fail(Object key, Object value) {
		return new Ret().setFail().set(key, value);
	}
	
	public static Ret state(Object value) {
		return new Ret()._setState(value);
	}
	
	public static Ret data(Object data) {
		return new Ret()._setData(data);
	}
	
	public static Ret msg(String msg) {
		return new Ret()._setMsg(msg);
	}
	
	/**
	 * 避免产生 setter/getter 方法，以免影响第三方 json 工具的行为
	 * 
	 * 如果未来开放为 public，当 stateWatcher 不为 null 且 dataWithOkState 为 true
	 * 与 _setData 可以形成死循环调用
	 */
	protected Ret _setState(Object value) {
		super.put(STATE, value);
		if (stateWatcher != null) {
			stateWatcher.call(this, STATE, value);
		}
		return this;
	}
	
	/**
	 * 避免产生 setter/getter 方法，以免影响第三方 json 工具的行为
	 * 
	 * 如果未来开放为 public，当 stateWatcher 不为 null 且 dataWithOkState 为 true
	 * 与 _setState 可以形成死循环调用
	 */
	protected Ret _setData(Object data) {
		super.put(DATA, data);
		if (dataWithOkState) {
			_setState(STATE_OK);
		}
		return this;
	}
	
	// 避免产生 setter/getter 方法，以免影响第三方 json 工具的行为
	protected Ret _setMsg(String msg) {
		super.put(MSG, msg);
		return this;
	}
	
	public Ret setOk() {
		return _setState(STATE_OK);
	}
	
	public Ret setFail() {
		return _setState(STATE_FAIL);
	}
	
	public boolean isOk() {
		Object state = get(STATE);
		if (STATE_OK.equals(state)) {
			return true;
		}
		if (STATE_FAIL.equals(state)) {
			return false;
		}
		if (okFailHandler != null) {
			return okFailHandler.call(Boolean.TRUE, state);
		}
		
		throw new IllegalStateException("调用 isOk() 之前，必须先调用 ok()、fail() 或者 setOk()、setFail() 方法");
	}
	
	public boolean isFail() {
		Object state = get(STATE);
		if (STATE_FAIL.equals(state)) {
			return true;
		}
		if (STATE_OK.equals(state)) {
			return false;
		}
		if (okFailHandler != null) {
			return okFailHandler.call(Boolean.FALSE, state);
		}
		
		throw new IllegalStateException("调用 isFail() 之前，必须先调用 ok()、fail() 或者 setOk()、setFail() 方法");
	}
	
	public Ret set(Object key, Object value) {
		super.put(key, value);
		return this;
	}
	
	public Ret setIfNotBlank(Object key, String value) {
		if (StrKit.notBlank(value)) {
			set(key, value);
		}
		return this;
	}
	
	public Ret setIfNotNull(Object key, Object value) {
		if (value != null) {
			set(key, value);
		}
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
	
	public <T> T getAs(Object key, T defaultValue) {
		Object ret = get(key);
		return ret != null ? (T) ret : defaultValue;
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
	
	public Double getDouble(Object key) {
		Number n = (Number)get(key);
		return n != null ? n.doubleValue() : null;
	}
	
	public Float getFloat(Object key) {
		Number n = (Number)get(key);
		return n != null ? n.floatValue() : null;
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


