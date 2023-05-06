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

package com.jfinal.template.expr.ast;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.jfinal.kit.StrKit;
import com.jfinal.proxy.ProxyClassLoader;

/**
 * 使用 jfinal proxy 机制消除 java.lang.reflect.Method.invoke(...)
 * 提升性能，并且同时支持动态类型的 field 表达式取值
 */
public class FastFieldGetter extends FieldGetter {

	protected static ProxyGenerator generator = new ProxyGenerator();
	protected static ProxyCompiler compiler = new ProxyCompiler();
	protected static ProxyClassLoader classLoader = new ProxyClassLoader();
	protected static Map<Class<?>, Proxy> cache = new ConcurrentHashMap<>(512, 0.25F);

	protected static boolean outputCompileError = false;

	protected Proxy proxy;
	protected java.lang.reflect.Method getterMethod;

	public FastFieldGetter(Proxy proxy, java.lang.reflect.Method getterMethod) {
		this.proxy = proxy;
		this.getterMethod = getterMethod;
	}

	/**
	 * 仅用于配置 Engine.addFieldGetter(0, new FastFieldGetter());
	 */
	public FastFieldGetter() {
		this(null, null);
	}

	public FieldGetter takeOver(Class<?> targetClass, String fieldName) {
		if (MethodKit.isForbiddenClass(targetClass)) {
			throw new RuntimeException("Forbidden class: " + targetClass.getName());
		}

		String getterName = "get" + StrKit.firstCharToUpperCase(fieldName);
		java.lang.reflect.Method[] methodArray = targetClass.getMethods();
		for (java.lang.reflect.Method method : methodArray) {
			if (method.getName().equals(getterName) && method.getParameterCount() == 0) {

				Proxy proxy = cache.computeIfAbsent(targetClass, key -> {
					try {
						return createProxy(key, fieldName);
					} catch (Throwable e) {
						return null;
					}
				});
				return proxy != null ? new FastFieldGetter(proxy, method) : null;
				
			}
		}

		return null;
	}

	public Object get(Object target, String fieldName) throws Exception {
		// return getterMethod.invoke(target, ExprList.NULL_OBJECT_ARRAY);
		return proxy.getValue(target, fieldName);
	}

	protected Proxy createProxy(Class<?> targetClass, String fieldName) {
		ProxyClass proxyClass = new ProxyClass(targetClass);
		String sourceCode = generator.generate(proxyClass);
		// System.out.println(sourceCode);

		proxyClass.setSourceCode(sourceCode);
		compiler.compile(proxyClass);
		Class<?> retClass = classLoader.loadProxyClass(proxyClass);
		proxyClass.setClazz(retClass);
		try {
			return (Proxy)retClass.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		return getterMethod.toString();
	}

	// ---------

	/**
	 * 代理接口
	 */
	public static interface Proxy {
		public Object getValue(Object target, String fieldName);
	}

	// ---------

	/**
	 * 代理类
	 */
	static class ProxyClass extends com.jfinal.proxy.ProxyClass {

		private String name;		// 类名

		public ProxyClass(Class<?> target) {
			super(target);

			name = target.getSimpleName() + "$$EnhancerByJFinal_FieldGetter";
		}

		public String getName() {
			return name;
		}
	}

	// ---------

	/**
	 * 代理生成器
	 */
	static class ProxyGenerator {

		String generate(ProxyClass proxyClass) {
			StringBuilder ret = new StringBuilder(1024);

			Class<?> targetClass = proxyClass.getTarget();
			String className = proxyClass.getName();

			ret.append("package ").append(proxyClass.getPkg()).append(";\n\n");
			ret.append("import com.jfinal.template.expr.ast.FastFieldGetter.Proxy;\n\n");
			ret.append("public class ").append(className).append(" implements Proxy {\n\n");
			ret.append("\tpublic Object getValue(Object target, String fieldName) {\n");
			ret.append("\t\tint hash = fieldName.hashCode();\n");
			ret.append("\t\tswitch (hash) {\n");

			java.lang.reflect.Method[] methodArray = targetClass.getMethods();
			for (java.lang.reflect.Method method : methodArray) {
				String mn = method.getName();
				if (method.getParameterCount() == 0 && mn.startsWith("get") && (!mn.equals("getClass"))) {
					String fieldName = StrKit.firstCharToLowerCase(mn.substring(3));
					ret.append("\t\tcase ").append(fieldName.hashCode()).append(" :\n");
					ret.append("\t\t\treturn ((").append(targetClass.getName()).append(")target).").append(mn).append("();\n");
				}
			}

			ret.append("\t\tdefault :\n");
			ret.append("\t\t\tthrow new RuntimeException(\"Can not access the field \\\"\" + target.getClass().getName() + \".\" + fieldName + \"\\\"\");\n");

			ret.append("\t\t}\n");
			ret.append("\t}\n");
			ret.append("}\n");

			return ret.toString();
		}
	}

	// ---------

	public static void setOutputCompileError(boolean outputCompileError) {
		FastFieldGetter.outputCompileError = outputCompileError;
	}

	/**
	 * 代理编译器
	 */
	static class ProxyCompiler extends com.jfinal.proxy.ProxyCompiler {
		@Override
		protected void outputCompileError(Boolean result, javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> collector) {
			if (outputCompileError) {
				super.outputCompileError(result, collector);
			}
		}
	}
}

