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

import java.io.IOException;
import java.nio.charset.Charset;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.io.IWritable;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Scope;

/**
 * Text 输出纯文本块以及使用 "#[[" 与 "]]#" 定义的原样输出块
 */
public class Text extends Stat implements IWritable {
	
	// content、bytes、chars 三者必有一者不为 null
	// 在 OutputStream、Writer 混合模式下 bytes、chars 同时不为null
	private StringBuilder content;
	private Charset charset;
	private byte[] bytes;
	private char[] chars;
	
	// content 初始值在 Lexer 中已确保不为 null
	public Text(StringBuilder content, String encoding) {
		this.content = content;
		this.charset = Charset.forName(encoding);
		this.bytes = null;
		this.chars = null;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		try {
			writer.write(this);
		} catch (IOException e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
	
	public byte[] getBytes() {
		if (bytes != null) {
			return bytes;
		}
		
		synchronized (this) {
			if (bytes != null) {
				return bytes;
			}
			
			if (content != null) {
				bytes = content.toString().getBytes(charset);
				content = null;
				return bytes;
			} else {
				bytes = new String(chars).getBytes(charset);
				return bytes;
			}
		}
	}
	
	public char[] getChars() {
		if (chars != null) {
			return chars;
		}
		
		synchronized (this) {
			if (chars != null) {
				return chars;
			}
			
			if (content != null) {
				char[] charsTemp = new char[content.length()];
				content.getChars(0, content.length(), charsTemp, 0);
				chars = charsTemp;
				content = null;
				return chars;
			} else {
				String strTemp = new String(bytes, charset);
				char[] charsTemp = new char[strTemp.length()];
				strTemp.getChars(0, strTemp.length(), charsTemp, 0);
				chars = charsTemp;
				return chars;
			}
		}
	}
	
	public boolean isEmpty() {
		if (content != null) {
			return content.length() == 0;
		} else if (bytes != null) {
			return bytes.length == 0;
		} else {
			return chars.length == 0;
		}
	}
	
//	public String getContent() {
//		return text != null ? new String(text) : null;
//	}
	
	public String toString() {
		if (bytes != null) {
			return new String(bytes, charset);
		} else if (chars != null) {
			return new String(chars);
		} else {
			return content.toString();
		}
	}
}



