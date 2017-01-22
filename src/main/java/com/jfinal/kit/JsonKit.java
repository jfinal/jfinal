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

import com.jfinal.json.JFinalJson;
import com.jfinal.json.Json;

/**
 * JsonKit.
 */
public class JsonKit {
	
	public static String toJson(Object object) {
		return Json.getJson().toJson(object);
	}
	
	public static <T> T parse(String jsonString, Class<T> type) {
		return Json.getJson().parse(jsonString, type);
	}
	
	/**
	 * 兼容 jfinal 2.1 之前版本
	 */
	@Deprecated
	public static String toJson(Object value, int depth) {
		Json json = Json.getJson();
		// 仅 JFinalJson 实现支持 int depth 参数
		if (json instanceof JFinalJson) {
			((JFinalJson)json).setConvertDepth(depth);
		}
		return json.toJson(value);
	}
	
	/*
	public static String toJson(Object target, String dataPattern) {
		return null;
	}
	*/
}

