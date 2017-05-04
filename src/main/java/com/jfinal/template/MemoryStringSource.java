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

package com.jfinal.template;

import com.jfinal.core.Const;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.StrKit;

/**
 * MemoryStringSource
 */
public class MemoryStringSource implements IStringSource {
	
	private String key;
	private StringBuilder content;
	
	public MemoryStringSource(String content) {
		if (StrKit.isBlank(content)) {
			throw new IllegalArgumentException("content can not be blank");
		}
		this.content = new StringBuilder(content);
		this.key = HashKit.md5(content);
	}
	
	public MemoryStringSource(StringBuilder content) {
		if (content == null || content.length() == 0) {
			throw new IllegalArgumentException("content can not be blank");
		}
		this.content = content;
		this.key = HashKit.md5(content.toString());
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
		return Const.DEFAULT_ENCODING;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Key : ").append(key).append("\n");
		sb.append("Content : ").append(content).append("\n");
		return sb.toString();
	}
}







