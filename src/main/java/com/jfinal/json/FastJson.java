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

package com.jfinal.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Json 转换 fastjson 实现.
 */
public class FastJson extends Json {
	
	public static FastJson getJson() {
		return new FastJson();
	}
	
	public String toJson(Object object) {
		// 优先使用对象级的属性 datePattern, 然后才是全局性的 defaultDatePattern
		String dp = datePattern != null ? datePattern : getDefaultDatePattern();
		if (dp == null) {
			return JSON.toJSONString(object);
		} else {
			return JSON.toJSONStringWithDateFormat(object, dp, SerializerFeature.WriteDateUseDateFormat);	// return JSON.toJSONString(object, SerializerFeature.WriteDateUseDateFormat);
		}
	}
	
	public <T> T parse(String jsonString, Class<T> type) {
		return JSON.parseObject(jsonString, type);
	}
}


