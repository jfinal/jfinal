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

import com.jfinal.kit.StrKit;

/**
 * json string 与 object 互转抽象
 */
public abstract class Json {
	
	private static IJsonFactory defaultJsonFactory = new JFinalJsonFactory();
	
	/**
	 * 当对象级的 datePattern 为 null 时使用 defaultDatePattern
	 * jfinal 2.1 版本暂定 defaultDatePattern 值为 null，即 jackson、fastjson
	 * 默认使用自己的 date 转换策略
	 */
	private static String defaultDatePattern = null;
	
	/**
	 * Json 继承类优先使用对象级的属性 datePattern, 然后才是全局性的 defaultDatePattern
	 */
	protected String datePattern = null;
	
	static void setDefaultJsonFactory(IJsonFactory defaultJsonFactory) {
		if (defaultJsonFactory == null) {
			throw new IllegalArgumentException("defaultJsonFactory can not be null.");
		}
		Json.defaultJsonFactory = defaultJsonFactory;
	}
	
	static void setDefaultDatePattern(String defaultDatePattern) {
		if (StrKit.isBlank(defaultDatePattern)) {
			throw new IllegalArgumentException("defaultDatePattern can not be blank.");
		}
		Json.defaultDatePattern = defaultDatePattern;
	}
	
	public Json setDatePattern(String datePattern) {
		if (StrKit.isBlank(datePattern)) {
			throw new IllegalArgumentException("datePattern can not be blank.");
		}
		this.datePattern = datePattern;
		return this;
	}
	
	public String getDatePattern() {
		return datePattern;
	}
	
	public String getDefaultDatePattern() {
		return defaultDatePattern;
	}
	
	public static Json getJson() {
		return defaultJsonFactory.getJson();
	}
	
	public abstract String toJson(Object object);
	
	public abstract <T> T parse(String jsonString, Class<T> type);
}




