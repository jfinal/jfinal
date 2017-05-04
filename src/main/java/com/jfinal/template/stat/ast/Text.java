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

package com.jfinal.template.stat.ast;

import java.io.Writer;
import com.jfinal.template.Env;
import com.jfinal.template.stat.Scope;

/**
 * Text 输出纯文本块以及使用 "#[[" 与 "]]#" 指定的非解析块 
 */
public class Text extends Stat {
	
	private char[] text;
	
	public Text(StringBuilder content) {
		this.text = new char[content.length()];
		content.getChars(0, content.length(), this.text, 0);
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		write(writer, text);
	}
	
	public boolean isEmpty() {
		return text.length == 0;
	}
	
	public String getContent() {
		return text != null ? new String(text) : null;
	}
	
	public String toString() {
		return text != null ? new String(text) : "";
	}
}



