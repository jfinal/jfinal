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

package com.jfinal.template;

import java.io.File;
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
		try (ByteWriter byteWriter = env.engineConfig.writerBuffer.getByteWriter(outputStream)) {
			ast.exec(env, new Scope(data, env.engineConfig.sharedObjectMap), byteWriter);
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
		try (CharWriter charWriter = env.engineConfig.writerBuffer.getCharWriter(writer)) {
			ast.exec(env, new Scope(data, env.engineConfig.sharedObjectMap), charWriter);
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
		try (FastStringWriter fsw = env.engineConfig.writerBuffer.getFastStringWriter()) {
			render(data, fsw);
			return fsw.toString();
		}
	}
	
	/**
	 * 支持无 data 参数，渲染到 String 中去 <br>
	 * 适用于数据在模板中通过表达式和语句直接计算得出等等应用场景
	 */
	public String renderToString() {
		return renderToString(null);
	}
	
	/**
	 * 渲染到 StringBuilder 中去
	 */
	public StringBuilder renderToStringBuilder(Map<?, ?> data) {
		FastStringWriter fsw = new FastStringWriter();
		render(data, fsw);
		return fsw.toStringBuilder();
	}
	
	/**
	 * 渲染到 File 中去
	 * 适用于代码生成器类似应用场景
	 */
	public void render(Map<?, ?> data, File file) {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			render(data, fos);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
	
	// --------------------------------------------------------------------
	
	/**
	 * Func 接口用于接管内部的 Stat ast、Env env、Scope scope 变量
	 * 实现更加灵活、强大的功能
	 */
	@FunctionalInterface
	public interface Func<T> {
		void call(Stat ast, Env env, Scope scope, T t);
	}
	
	/**
	 * 渲染到 String 中去
	 * 
	 * Func 接口用于接管内部的 Stat ast、Env env、Scope scope 变量，并且便于
	 * 向 Ctrl 传入 attachment 参数
	 * 
	 * <pre>
	 * 例子：
	 *   Map<Object, Object> data = new HashMap<>();
	 *   data.put("key", 123);
	 *   
	 *   String ret = template.renderToString(data, (ast, env, scope, writer) -> {
	 *      // 可以传入任意类型的 attachment 参数，以下以 Kv 对象为例
	 *      // 该参数可以在指令中通过 scope.getCtrl().getAttachment() 获取
	 *      scope.getCtrl().setAttachment(Kv.by("key", 456));
	 *      
	 *      // 接管内部的 ast、env、scope、writer，执行 ast.exec(...)
	 *      ast.exec(env, scope, writer);
	 *   });
	 *   
	 *   System.out.println(ret);
	 * </pre>
	 */
	public String renderToString(Map<?, ?> data, Func<CharWriter> func) {
		try (FastStringWriter fsw = env.engineConfig.writerBuffer.getFastStringWriter();
				CharWriter charWriter = env.engineConfig.writerBuffer.getCharWriter(fsw)) {
			func.call(ast, env, new Scope(data, env.engineConfig.sharedObjectMap), charWriter);
			return fsw.toString();
		}
	}
	
	/**
	 * 渲染到 OutputStream 中去
	 */
	public void render(Map<?, ?> data, OutputStream outputStream, Func<ByteWriter> func) {
		try (ByteWriter byteWriter = env.engineConfig.writerBuffer.getByteWriter(outputStream)) {
			func.call(ast, env, new Scope(data, env.engineConfig.sharedObjectMap), byteWriter);
		}
	}
	
	/**
	 * 渲染到 Writer 中去
	 */
	public void render(Map<?, ?> data, Writer writer, Func<CharWriter> func) {
		try (CharWriter charWriter = env.engineConfig.writerBuffer.getCharWriter(writer)) {
			func.call(ast, env, new Scope(data, env.engineConfig.sharedObjectMap), charWriter);
		}
	}
	
	/**
	 * 渲染到 File 中去
	 * 适用于代码生成器类似应用场景
	 */
	public void render(Map<?, ?> data, File file, Func<ByteWriter> func) {
		try (FileOutputStream fos = new FileOutputStream(file);
				ByteWriter byteWriter = env.engineConfig.writerBuffer.getByteWriter(fos)) {
			func.call(ast, env, new Scope(data, env.engineConfig.sharedObjectMap), byteWriter);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	// ---------
	
    private void close(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception ignored) {
            }
        }
    }
	
    /**
     * 渲染到 OutputStream 中去，autoCloseOutputStream 指定是否自动关闭 OutputStream
     */
    public void render(Map<?, ?> data, OutputStream outputStream, boolean autoCloseOutputStream) {
        try (ByteWriter byteWriter = env.engineConfig.writerBuffer.getByteWriter(outputStream)) {
            ast.exec(env, new Scope(data, env.engineConfig.sharedObjectMap), byteWriter);
        } finally {
            if (autoCloseOutputStream) {
                close(outputStream);
            }
        }
    }
    
    /**
     * 渲染到 Writer 中去，autoCloseWriter 指定是否自动关闭 Writer
     */
    public void render(Map<?, ?> data, Writer writer, boolean autoCloseWriter) {
        try (CharWriter charWriter = env.engineConfig.writerBuffer.getCharWriter(writer)) {
            ast.exec(env, new Scope(data, env.engineConfig.sharedObjectMap), charWriter);
        } finally {
            if (autoCloseWriter) {
                close(writer);
            }
        }
    }
}





