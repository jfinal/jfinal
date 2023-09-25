/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

import java.io.IOException;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Escape 对字符串进行转义
 * 用法:
 * #escape(value)
 */
public class EscapeDirective extends Directive {
	
	public void exec(Env env, Scope scope, Writer writer) {
		try {
			Object value = exprList.eval(scope);
			
			if (value instanceof String) {
				escape((String)value, writer);
			} else if (value instanceof Number) {
				Class<?> c = value.getClass();
				if (c == Integer.class) {
					writer.write((Integer)value);
				} else if (c == Long.class) {
					writer.write((Long)value);
				} else if (c == Double.class) {
					writer.write((Double)value);
				} else if (c == Float.class) {
					writer.write((Float)value);
				} else {
					writer.write(value.toString());
				}
			} else if (value != null) {
				escape(value.toString(), writer);
			}
		} catch (TemplateException | ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
	
	private void escape(String str, Writer w) throws IOException {
		for (int i = 0, len = str.length(); i < len; i++) {
			char cur = str.charAt(i);
			switch (cur) {
			case '<':
				w.write("&lt;");
				break;
			case '>':
				w.write("&gt;");
				break;
			case '"':
				w.write("&quot;");
				break;
			case '\'':
				// w.write("&apos;");	// IE 不支持 &apos; 考虑 &#39;
				w.write("&#39;");
				break;
			case '&':
				w.write("&amp;");
				break;
			default:
				w.write(str, i, 1);
				break;
			}
		}
	}
}


