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

package com.jfinal.template.ext.directive;

import java.io.Writer;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.stat.Scope;

/**
 * Escape 对字符串进行转义
 * 用法:
 * #escape(value)
 */
public class EscapeDirective extends Directive {
	
	public void exec(Env env, Scope scope, Writer writer) {
		Object value = exprList.eval(scope);
		if (value != null) {
			write(writer, escape(value.toString()));
		}
	}
	
	// TODO 挪到 StrKit 中
	private String escape(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		
		int len = str.length();
		StringBuilder ret = new StringBuilder(len * 2);
		for (int i = 0; i < len; i++) {
			char cur = str.charAt(i);
			switch (cur) {
			case '<':
				ret.append("&lt;");
				break;
			case '>':
				ret.append("&gt;");
				break;
			case '\"':
				ret.append("&quot;");
				break;
			case '\'':
				ret.append("&apos;");	// IE 不支持 &apos; 考虑 &#39;
				break;
			case '&':
				ret.append("&amp;");
				break;
			default:
				ret.append(cur);
				break;
			}
		}

		return ret.toString();
	}
}
