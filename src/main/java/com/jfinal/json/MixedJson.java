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

package com.jfinal.json;

/**
 * JFinalJson 与 FastJson 混合做 json 转换
 * toJson 用 JFinalJson，parse 用 FastJson
 * 
 * 注意：
 * 1：需要添加 fastjson 相关 jar 包
 * 2：parse 方法转对象依赖于 setter 方法
 */
public class MixedJson extends Json {
	
	private JFinalJson jFinalJson;
	private FastJson fastJson;
	
	public static MixedJson getJson() {
		return new MixedJson();
	}
	
	public String toJson(Object object) {
		return getJFinalJson().toJson(object);
	}
	
	public <T> T parse(String jsonString, Class<T> type) {
		return getFastJson().parse(jsonString, type);
	}
	
	private JFinalJson getJFinalJson() {
		if (jFinalJson == null) {
			jFinalJson = JFinalJson.getJson();
		}
		if (datePattern != null) {
			jFinalJson.setDatePattern(datePattern);
		}
		return jFinalJson;
	}
	
	private FastJson getFastJson() {
		if (fastJson == null) {
			fastJson = FastJson.getJson();
		}
		if (datePattern != null) {
			fastJson.setDatePattern(datePattern);
		}
		return fastJson;
	}
}

