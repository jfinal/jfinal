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

package com.jfinal.template.source;

import com.jfinal.kit.HashKit;
import com.jfinal.kit.StrKit;
import com.jfinal.template.EngineConfig;

/**
 * StringSource 用于从 String 变量中加载模板内容
 */
public class StringSource implements ISource {
	
	private String key;
	private StringBuilder content;
	
	/**
	 * 构造 StringSource
	 * @param content 模板内容
	 * @param cache true 则缓存 Template，否则不缓存
	 */
	public StringSource(String content, boolean cache) {
		if (StrKit.isBlank(content)) {
			throw new IllegalArgumentException("content can not be blank");
		}
		this.content = new StringBuilder(content);
		this.key = cache ? HashKit.md5(content) : null;	// 不缓存只要将 key 值赋为 null 即可
	}
	
	public StringSource(StringBuilder content, boolean cache) {
		if (content == null || content.length() == 0) {
			throw new IllegalArgumentException("content can not be blank");
		}
		this.content = content;
		this.key = cache ? HashKit.md5(content.toString()) : null;	// 不缓存只要将 key 值赋为 null 即可
	}
	
	public boolean isModified() {
		return false;
	}
	
	public String getKey() {
		return key;
	}
	
	public StringBuilder getContent() {
		return content;
	}
	
	public String getEncoding() {
		return EngineConfig.DEFAULT_ENCODING;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Key : ").append(key).append("\n");
		sb.append("Content : ").append(content).append("\n");
		return sb.toString();
	}
}







