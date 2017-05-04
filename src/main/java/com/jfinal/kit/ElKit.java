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

package com.jfinal.kit;

import java.io.Writer;
import java.util.Map;
import com.jfinal.template.Directive;
import com.jfinal.template.Engine;
import com.jfinal.template.Env;
import com.jfinal.template.Template;
import com.jfinal.template.stat.Scope;

/**
 * EL 表达式语言求值工具类
 * 
 * <pre>
 * 1：不带参示例
 * 	  Integer value = ElKit.eval("1 + 2 * 3");
 * 
 * 2：带参示例
 * 	  Kv data = Kv.by("a", 2).set("b", 3);
 * 	  Integer value = ElKit.eval("1 + a * b", data);
 * </pre>
 */
public class ElKit {
	
	private static Engine engine = new Engine();
	private static final String RETURN_VALUE_KEY = "_RETURN_VALUE_";
	
	static {
		engine.addDirective("eval", new InnerEvalDirective());
	}
	
	public Engine getEngine() {
		return engine;
	}
	
	public static <T> T eval(String expr) {
		return eval(expr, Kv.create());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T eval(String expr, Map<?, ?> data) {
		String stringTemplate = "#eval(" + expr + ")";
		Template template = engine.getTemplateByString(stringTemplate);
		template.render(data, null);
		return (T)data.get(RETURN_VALUE_KEY);
	}
	
	public static class InnerEvalDirective extends Directive {
		public void exec(Env env, Scope scope, Writer writer) {
			Object value = exprList.eval(scope);
			scope.set(RETURN_VALUE_KEY, value);
		}
	}
}




