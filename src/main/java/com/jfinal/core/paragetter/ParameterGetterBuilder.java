/**
 * Copyright (c) 2011-2017, 玛雅牛 (myaniu AT gmail.com).
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
package com.jfinal.core.paragetter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import com.jfinal.core.Controller;

public class ParameterGetterBuilder {

	private static ParameterGetterBuilder me = new ParameterGetterBuilder();
	private Map<String, Holder> typeMap = new HashMap<>();

	private ParameterGetterBuilder() {
		registType("short", ShortParameterGetter.class, "0");
		registType("int", IntParameterGetter.class, "0");
		registType("long", LongParameterGetter.class, "0");
		registType("float", FloatParameterGetter.class, "0");
		registType("double", DoubleParameterGetter.class, "0");
		registType("boolean", BooleanParameterGetter.class, "false");
		registType("java.lang.Short", ShortParameterGetter.class, null);
		registType("java.lang.Integer", IntParameterGetter.class, null);
		registType("java.lang.Long", LongParameterGetter.class, null);
		registType("java.lang.Float", FloatParameterGetter.class, null);
		registType("java.lang.Double", DoubleParameterGetter.class, null);
		registType("java.lang.Boolean", BooleanParameterGetter.class, null);
		registType("java.lang.String", StringParameterGetter.class, null);
		registType("java.util.Date", DateParameterGetter.class, null);
		registType("java.math.BigDecimal", BigDecimalParameterGetter.class, null);
		registType("java.math.BigInteger", BigIntegerParameterGetter.class, null);
		registType("com.jfinal.upload.UploadFile", FileParameterGetter.class, null);
		registType("java.util.List<com.jfinal.upload.UploadFile>", FileArrayParameterGetter.class, null);
		registType("java.lang.String[]", StringArrayParameterGetter.class, null);
		registType("int[]", IntArrayParameterGetter.class, null);
		registType("java.lang.Integer[]", IntArrayParameterGetter.class, null);
		registType("long[]", LongArrayParameterGetter.class, null);
		registType("java.lang.Long[]", LongArrayParameterGetter.class, null);
	}

	public static ParameterGetterBuilder me() {
		return me;
	}
	
	/**
	 * 注册一个类型识别器
	 * @param type 类型，例如 int, java.lang.Integer
	 * @param clazz 参数获取器实现类，必须继承ParameterGetter
	 * @param defaultValue，默认值，比如int的默认值为0， java.lang.Integer的默认值为null
	 */
	public void registType(String type, Class<? extends ParameterGetter<?>> clazz, String defaultValue){
		this.typeMap.put(type, new Holder(clazz, defaultValue));
	}

	public ParameterGetterProcessor build(Class<? extends Controller> controllerClass, Method method) {
		final int parameterCount = method.getParameterCount();
		ParameterGetterProcessor opag = new ParameterGetterProcessor(parameterCount);
		if (0 == parameterCount) {
			return opag;
		}
		for (Parameter p : method.getParameters()) {
			IParameterGetter<?> pg = createParameterGetter(controllerClass, method, p);
			if (pg instanceof FileParameterGetter || pg instanceof FileArrayParameterGetter) {
				opag.addParameterGetterToHeader(pg);
			} else {
				opag.addParameterGetter(pg);
			}
		}
		return opag;
	}

	private IParameterGetter<?> createParameterGetter(Class<? extends Controller> controllerClass, Method method,
			Parameter p) {
		String parameterName = p.getName();
		String defaultValue = null;
		String type = p.getParameterizedType().getTypeName();
		Para para = p.getAnnotation(Para.class);
		if (para != null) {
			parameterName = para.value().trim();
			defaultValue = para.defaultValue().trim();
			if (defaultValue.isEmpty()) {
				defaultValue = null;
			}
		}
		Holder holder = typeMap.get(type);
		if (holder != null) {
			if (null == defaultValue) {
				defaultValue = holder.getDefaultValue();
			}
			try {
				return holder.born(parameterName, defaultValue);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		// 判断是否是com.jfinal.plugin.activerecord.Model的子类
		if (com.jfinal.plugin.activerecord.Model.class.isAssignableFrom(p.getType())) {
			return new ModelParameterGetter<>(p.getType(), parameterName);
		} else {
			return new BeanParameterGetter<>(p.getType(), parameterName);
		}
	}

	private static class Holder {
		private final String defaultValue;
		private final Class<? extends ParameterGetter<?>> clazz;

		Holder(Class<? extends ParameterGetter<?>> clazz, String defaultValue) {
			this.clazz = clazz;
			this.defaultValue = defaultValue;
		}
		final String getDefaultValue() {
			return defaultValue;
		}
		ParameterGetter<?> born(String parameterName, String defaultValue) throws Exception {
			Constructor<? extends ParameterGetter<?>> con = clazz.getConstructor(String.class, String.class);
			return con.newInstance(parameterName, defaultValue);
		}
	}
}
