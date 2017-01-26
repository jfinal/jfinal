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

import java.text.SimpleDateFormat;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Json 转换 jackson 实现.
 * 
 * json 到 java 类型转换规则: http://wiki.fasterxml.com/JacksonInFiveMinutes
 * JSON TYPE				JAVA TYPE
 * object					LinkedHashMap<String,Object>
 * array					ArrayList<Object>
 * string					String
 * number (no fraction)		Integer, Long or BigInteger (smallest applicable)
 * number (fraction)		Double (configurable to use BigDecimal)
 * true|false				Boolean
 * null						null
 */
public class Jackson extends Json {
	
	// Jackson 生成 json 的默认行为是生成 null value，可设置此值全局改变默认行为
	private static boolean defaultGenerateNullValue = true;
	
	// generateNullValue 通过设置此值，可临时改变默认生成 null value 的行为
	protected Boolean generateNullValue = null;
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	public static void setDefaultGenerateNullValue(boolean defaultGenerateNullValue) {
		Jackson.defaultGenerateNullValue = defaultGenerateNullValue;
	}
	
	public Jackson setGenerateNullValue(boolean generateNullValue) {
		this.generateNullValue = generateNullValue;
		return this;
	}
	
	/**
	 * 通过获取 ObjectMapper 进行更个性化设置，满足少数特殊情况
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	public static Jackson getJson() {
		return new Jackson();
	}
	
	public String toJson(Object object) {
		try {
			// 优先使用对象级的属性 datePattern, 然后才是全局性的 defaultDatePattern
			String dp = datePattern != null ? datePattern : getDefaultDatePattern();
			if (dp != null) {
				objectMapper.setDateFormat(new SimpleDateFormat(dp));
			}
			
			// 优先使用对象属性 generateNullValue，决定转换 json时是否生成 null value
			Boolean pnv = generateNullValue != null ? generateNullValue : defaultGenerateNullValue;
			if (pnv == false) {
				objectMapper.setSerializationInclusion(Include.NON_NULL);
			}
			
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
		}
	}
	
	public <T> T parse(String jsonString, Class<T> type) {
		try {
			return objectMapper.readValue(jsonString, type);
		} catch (Exception e) {
			throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
		}
	}
}



