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

/**
 * 用于定制个性化 Ret
 *
 * <pre>
 * 例子：
 *   CPI.setRetState("success", true, false);
 *   将 Ret 的状态字段名由 "state" 改为 "success"，将状态值 "ok" 改为 true、"fail" 改为 false
 *
 *   CPI.setRetState("code", 200, 500);
 *   将 Ret 的状态字段名由 "state" 改为 "code"，将状态值 "ok" 改为 200、"fail" 改为 500
 *
 *   CPI.setRetMsgName("message")
 *   将 Ret 的消息字段名由 "msg" 改为 "message"
 * </pre>
 */
public class CPI {

	/**
	 * 配置 Ret 的状态名、成功状态值、失败状态值，默认值分别为："state"、"ok"、"fail"
	 * <pre>
	 * 例子：
	 *   CPI.setRetState("success", true, false);
	 *   将 Ret 的状态字段名由 "state" 改为 "success"，将状态值 "ok" 改为 true、"fail" 改为 false
	 *
	 *   CPI.setRetState("code", 200, 500);
	 *   将 Ret 的状态字段名由 "state" 改为 "code"，将状态值 "ok" 改为 200、"fail" 改为 500
	 * </pre>
	 */
	public static void setRetState(String stateName, Object stateOkValue, Object stateFailValue) {
		if (StrKit.isBlank(stateName)) {
			throw new IllegalArgumentException("stateName 不能为空");
		}
		if (stateOkValue == null) {
			throw new IllegalArgumentException("stateOkValue 不能为 null");
		}
		if (stateFailValue == null) {
			throw new IllegalArgumentException("stateFailValue 不能为 null");
		}

		Ret.STATE = stateName;
		Ret.STATE_OK = stateOkValue;
		Ret.STATE_FAIL = stateFailValue;
	}

	/**
	 * 配置 Ret 的消息名，默认值为："msg"
	 * <pre>
	 * 例子：
	 *   CPI.setRetMsgName("message")
	 *   将 Ret 的消息字段名由 "msg" 改为 "message"
	 * </pre>
	 */
	public static void setRetMsgName(String msgName) {
		if (StrKit.isBlank(msgName)) {
			throw new IllegalArgumentException("msgName 不能为空");
		}
		Ret.MSG = msgName;
	}

	/**
	 * 配置 Ret 的 data 名，默认值为："data"
	 * <pre>
	 * 例子：
	 *   CPI.setRetDataName("body")
	 *   将 Ret 的数据字段名由 "data" 改为 "body"
	 * </pre>
	 */
	public static void setRetDataName(String dataName) {
		if (StrKit.isBlank(dataName)) {
			throw new IllegalArgumentException("dataName 不能为空");
		}
		Ret.DATA = dataName;
	}

	/**
	 * 配置 Ret 的 data 方法伴随 ok 状态，默认值为：true
	 * <pre>
	 * 例子：
	 *   CPI.setRetDataWithOkState(false)
	 *   将 Ret 的 data 方法伴随 ok 状态，改为不伴随 ok 状态
	 * </pre>
	 */
	public static void setRetDataWithOkState(boolean dataWithOkState) {
		Ret.dataWithOkState = dataWithOkState;
	}

	/**
	 * 配置 state 监听
	 * <pre>
	 * 例子：
	 *   CPI.setRetStateWatcher((ret, state, value) -> {
	 *     ret.set("success", "ok".equals(value));
	 *   });
	 *   监听 state，当值为 "ok" 时，额外放入 "success" 值为 true，否则为 false，
	 *   在前后端分离项目中，有些前端框架需要该返回值："success" : true/false
	 * </pre>
	 */
	public static void setRetStateWatcher(Func.F30<Ret, String, Object> stateWatcher) {
		if (stateWatcher == null) {
			throw new IllegalArgumentException("stateWatcher 不能 null");
		}
		Ret.stateWatcher = stateWatcher;
	}

	/**
	 * 配置 Ret.isOk()、Ret.isFail() 在前两个 if 判断都没有 return 之后的处理回调
	 * 用于支持多于两个状态的情况，也即在 ok、fail 两个状态之外还引入了其它状态
	 * <pre>
	 * 例子：
	 *   CPI.setRetOkFailHandler((isOkMethod, value) -> {
	 *     if (isOkMethod == Boolean.TRUE) {
	 *        return false;
	 *     } else {
	 *        return true;
	 *     }
	 *   });
	 * </pre>
	 */
	public static void setRetOkFailHandler(Func.F21<Boolean, Object, Boolean> okFailHandler) {
		if (okFailHandler == null) {
			throw new IllegalArgumentException("okFailHandler 不能 null");
		}
		Ret.okFailHandler = okFailHandler;
	}

	public static String getRetStateName() {
		return Ret.STATE;
	}

	public static Object getRetStateOkValue() {
		return Ret.STATE_OK;
	}

	public static Object getRetStateFailValue() {
		return Ret.STATE_FAIL;
	}

	public static String getRetMsgName() {
		return Ret.MSG;
	}

	public static String getRetDataName() {
		return Ret.DATA;
	}

	public static boolean getRetDataWithOkState() {
		return Ret.dataWithOkState;
	}

	public static Func.F30<Ret, String, Object> getRetStateWatcher() {
		return Ret.stateWatcher;
	}

	public static Func.F21<Boolean, Object, Boolean> getRetOkFailHandler() {
		return Ret.okFailHandler;
	}
}





