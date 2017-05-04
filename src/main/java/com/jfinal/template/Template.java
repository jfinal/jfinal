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

import java.io.Writer;
import java.util.Map;
import com.jfinal.template.stat.Scope;
import com.jfinal.template.stat.ast.Stat;

/**
 * Template
 * 
 * 用法：
 * Template template = Engine.use().getTemplate(...);
 * template.render(data, writer);
 * template.renderToString(data);
 */
public class Template {
	
	private Env env;
	private Stat ast;
	
	public Template(Env env, Stat ast) {
		if (env == null || ast == null) {
			throw new IllegalArgumentException("env and ast can not be null");
		}
		this.env = env;
		this.ast = ast;
	}
	
	/**
	 * 渲染到 Writer 中去
	 */
	public void render(Map<?, ?> data, Writer writer) {
		ast.exec(env, new Scope(data, env.engineConfig.sharedObjectMap), writer);
	}
	
	/**
	 * 支持无 data 参数，渲染到 Writer 中去 <br>
	 * 适用于数据在模板中通过表达式和语句直接计算得出等等应用场景<br>
	 * 此外，其它所有 render 方法也支持传入 null 值 data 参数
	 */
	public void render(Writer writer) {
		ast.exec(env, new Scope(null, env.engineConfig.sharedObjectMap), writer);
	}
	
	/**
	 * 渲染到 FastStringWriter 中去
	 */
	public void render(Map<?, ?> data, FastStringWriter fastStringWriter) {
		ast.exec(env, new Scope(data, env.engineConfig.sharedObjectMap), fastStringWriter);
	}
	
	/**
	 * 渲染到 StringBuilder 中去
	 */
	public StringBuilder renderToStringBuilder(Map<?, ?> data) {
		FastStringWriter fsw = new FastStringWriter();
		render(data, fsw);
		return fsw.getBuffer();
	}
	
	/**
	 * 渲染到 String 中去
	 */
	public String renderToString(Map<?, ?> data) {
		return renderToStringBuilder(data).toString();
	}
	
	public boolean isModified() {
		return env.isStringSourceListModified();
	}
}





