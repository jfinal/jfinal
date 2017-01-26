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
	 * 如果下一个是指令(输出指令除外)，需要移除该指令前方的空格，直到碰到 '\n'
	 * 这些空格会影响到指令后面输出的数据，那些数据会额外添加一些空格进来
	 */
	public void deleteBlankTails_old() {
		int i = text.length() - 1;
		for (; i>=0; i--) {
			if ( !CharTable.isBlank(text.charAt(i)) ) {
				break ;
			}
		}
		text.delete(i+1, text.length());
	}
	
	/**
	 * 当下一个指令与当前 TextToken 不在同一行时，返回 true
	 * 如果下一个是指令(输出指令除外)，需要移除该指令前方的空格，直到碰到 '\n'
	 * 这些空格会影响到指令后面输出的数据，那些数据会额外添加一些空格进来
	 */
	// 在调用这个之前，应该先判断 paraToken 之后是否没有非空字符，如果有的话，不用删除这里
	// 上面的 old 方法从代码上看，是没有删除最后一个 \n 字符的，这个需要测试
	public boolean deleteBlankTails() {
		for (int i = text.length() - 1; i >= 0; i--) {
			if (CharTable.isBlank(text.charAt(i))) {
				continue ;
			}
			// 新方法这里是最大的不同，只有在碰到 \n 后，才真正去删，就是说如果 text 与后续 token 不在同一行才去删
			// 无 \n 表示在同一行，则不去删这些东东
			// Lexer 的事情改完了，再测试这里的新旧两个方法，新方法应该是更合理的，但需要严格测试
			if (text.charAt(i) == '\n') {
				text.delete(i+1, text.length());
				return true;
			}
			break ;
		}
		return false;
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


