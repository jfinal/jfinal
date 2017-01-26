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

package com.jfinal.template.stat;

/**
 * ParaToken
 */
public class ParaToken extends Token {
	
	// 接管父类的 value，content 可能为 null
	private StringBuilder content;
	
	public ParaToken(StringBuilder content, int row) {
		super(Symbol.PARA, row);
		this.content = content;
	}
	
	public String value() {
		return content.toString();
	}
	
	public StringBuilder getContent() {
		return content;
	}
	
	public String toString() {
		return content != null ? content.toString() : "null";
	}
	
	public void print() {
		System.out.print("[");
		System.out.print(row);
		System.out.print(", PARA, ");
		System.out.print(toString());
		System.out.println("]");
	}
}

