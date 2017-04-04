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
 * TextToken
 * 词法分析时，合并相邻 TextToken
 */
class TextToken extends Token {
	
	// 接管父类的 value
	private StringBuilder text;
	
	public TextToken(StringBuilder value, int row) {
		super(Symbol.TEXT, row);
		this.text = value;
	}
	
	public void append(StringBuilder content) {
		if (content != null) {
			text.append(content);	// 不要使用 toString()，性能不如直接这样快
		}
	}
	
	/**
	 * 1：当前指令"后方"全是空白字符并且以 '\n' 或 EOF 结尾，当前指令"前方"为 TextToken 时调用此方法
	 * 2：当前指令本行内前方为空白字符(必须遭遇 '\n')，则删掉前方的空白字符
	 * 3：当前指令前方全为空白字符(不含 '\n')，表明是两个指令之间全为空白字符的情况，
	 *   或者两指令不在同一行且第二个指令前方全是空白字符的情况，则删掉这两指令之间的全部空白字符
	 * 4：返回 true，告知调用方需要吃掉本指令行尾的 '\n'
	 * 
	 * 简单描述：
	 * 1：当前指令独占一行，删除当前指令前方空白字符，并告知调用方吃掉行尾 '\n'
	 * 2：当前指令前方仍然是指令，两指令之间有空白字符，吃掉前方(即所有)的空白字符，并告知调用方吃掉行尾 '\n'
	 * 3：情况 2 时，相当于本 TextToken 内容变成了空字符串，后续的 Parser 将过滤掉这类节点
	 */
	public boolean deleteBlankTails() {
		for (int i = text.length() - 1; i >= 0; i--) {
			if (CharTable.isBlank(text.charAt(i))) {
				continue ;
			}
			
			if (text.charAt(i) == '\n') {
				text.delete(i+1, text.length());
				return true;
			} else {
				return false;
			}
		}
		
		// 两个指令之间全是空白字符， 设置其长度为 0，为 Parser 过滤内容为空的 Text 节点做准备
		text.setLength(0);
		return true;		// 当两指令之间全为空白字符时，告知调用方需要吃掉行尾的 '\n'
	}
	
	public String value() {
		return text.toString();
	}
	
	public StringBuilder getContent() {
		return text;
	}
	
	public String toString() {
		return text.toString();
	}
	
	public void print() {
		System.out.print("[");
		System.out.print(row);
		System.out.print(", TEXT, ");
		System.out.print(text.toString());
		System.out.println("]");
	}
}


