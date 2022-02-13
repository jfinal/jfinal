/**
 * Copyright (c) 2011-2025, James Zhan 詹波 (jfinal@126.com).
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
 *   CPI.configRetState("success", true, false);
 *   将 Ret 的状态字段名由 "state" 改为 "success"，将状态值 "ok" 改为 true、"fail" 改为 false
 *   
 *   CPI.configRetState("code", 200, 500);
 *   将 Ret 的状态字段名由 "state" 改为 "code"，将状态值 "ok" 改为 200、"fail" 改为 500
 * </pre>
 */
public class CPI {
	
	/**
	 * 配置 Ret 的状态名、成功状态值、失败状态值，默认值分别为："state"、"ok"、"fail"
	 * <pre>
	 * 例子：
	 *   CPI.configRetState("success", true, false);
	 *   将 Ret 的状态字段名由 "state" 改为 "success"，将状态值 "ok" 改为 true、"fail" 改为 false
	 *   
	 *   CPI.configRetState("code", 200, 500);
	 *   将 Ret 的状态字段名由 "state" 改为 "code"，将状态值 "ok" 改为 200、"fail" 改为 500
	 *   
	 *   CPI.configRetMsg("message")
	 *   将 Ret 的消息字段名由 "msg" 改为 "message"
	 * </pre>
	 */
	public static void configRetState(String stateName, Object stateOkValue, Object stateFailValue) {
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
	 *   CPI.configRetMsg("message")
	 *   将 Ret 的消息字段名由 "msg" 改为 "message"
	 * </pre>
	 */
	public static void configRetMsg(String msgName) {
		Ret.MSG = msgName;
	}
	
	public static String getRetStateName() {
		return Ret.STATE;
	}
	
	public static Object getStateOkValue() {
		return Ret.STATE_OK;
	}
	
	public static Object getStateFailValue() {
		return Ret.STATE_FAIL;
	}
	
	public static String getMsgName() {
		return Ret.MSG;
	}
}




