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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import com.jfinal.template.io.ByteWriter;
import com.jfinal.template.io.CharWriter;
import com.jfinal.template.io.FastStringWriter;
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
	 * 渲染到 OutputStream 中去
	 */
	public void render(Map<?, ?> data, OutputStream outputStream) {
		ByteWriter byteWriter = env.engineConfig.writerBuffer.getByteWriter(outputStream);
		try {
			ast.exec(env, new Scope(data, env.engineConfig.sharedObjectMap), byteWriter);
		} finally {
			byteWriter.close();
		}
	}
	
	/**
	 * 支持无 data 参数，渲染到 OutputStream 中去 <br>
	 * 适用于数据在模板中通过表达式和语句直接计算得出等等应用场景
	 */
	public void render(OutputStream outputStream) {
		render(null, outputStream);
	}
	
	/**
	 * 渲染到 Writer 中去
	 */
	public void render(Map<?, ?> data, Writer writer) {
		CharWriter charWriter = env.engineConfig.writerBuffer.getCharWriter(writer);
		try {
			ast.exec(env, new Scope(data, env.engineConfig.sharedObjectMap), charWriter);
		} finally {
			charWriter.close();
		}
	}
	
	/**
	 * 支持无 data 参数，渲染到 Writer 中去 <br>
	 * 适用于数据在模板中通过表达式和语句直接计算得出等等应用场景
	 */
	public void render(Writer writer) {
		render(null, writer);
	}
	
	/**
	 * 渲染到 String 中去
	 */
	public String renderToString(Map<?, ?> data) {
		FastStringWriter fsw = env.engineConfig.writerBuffer.getFastStringWriter();
		try {
			render(data, fsw);
			return fsw.toString();
		} finally {
			fsw.close();
		}
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
	 * 渲染到 File 中去
	 * 适用于代码生成器类似应用场景
	 */
	public void render(Map<?, ?> data, File file) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			render(data, fos);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {fos.close();} catch (IOException e) {e.printStackTrace(System.err);}
			}
		}
	}
	
	/**
	 * 渲染到 String fileName 参数所指定的文件中去
	 * 适用于代码生成器类似应用场景
	 */
	public void render(Map<?, ?> data, String fileName) {
		render(data, new File(fileName));
	}
	
	public boolean isModified() {
		return env.isSourceListModified();
	}
}





