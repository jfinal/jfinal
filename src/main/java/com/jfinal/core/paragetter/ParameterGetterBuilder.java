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

	private final static ParameterGetterBuilder me = new ParameterGetterBuilder();
	private Map<Class<?>, Holder> typeMap = new HashMap<>();

	private ParameterGetterBuilder() {
		regist(short.class, ShortParameterGetter.class, "0");
		regist(int.class, IntegerParameterGetter.class, "0");
		regist(long.class, LongParameterGetter.class, "0");
		regist(float.class, FloatParameterGetter.class, "0");
		regist(double.class, DoubleParameterGetter.class, "0");
		regist(boolean.class, BooleanParameterGetter.class, "false");
		regist(java.lang.Short.class, ShortParameterGetter.class, null);
		regist(java.lang.Integer.class, IntegerParameterGetter.class, null);
		regist(java.lang.Long.class, LongParameterGetter.class, null);
		regist(java.lang.Float.class, FloatParameterGetter.class, null);
		regist(java.lang.Double.class, DoubleParameterGetter.class, null);
		regist(java.lang.Boolean.class, BooleanParameterGetter.class, null);
		regist(java.lang.String.class, StringParameterGetter.class, null);
		regist(java.util.Date.class, DateParameterGetter.class, null);
		regist(java.sql.Date.class, SqlDateParameterGetter.class, null);
		regist(java.sql.Time.class, TimeParameterGetter.class, null);
		regist(java.sql.Timestamp.class, TimestampParameterGetter.class, null);
		regist(java.math.BigDecimal.class, BigDecimalParameterGetter.class, null);
		regist(java.math.BigInteger.class, BigIntegerParameterGetter.class, null);
		regist(com.jfinal.upload.UploadFile.class, FileParameterGetter.class, null);
		regist(java.lang.String[].class, StringArrayParameterGetter.class, null);
		regist(java.lang.Integer[].class, IntegerArrayParameterGetter.class, null);
		regist(java.lang.Long[].class, LongArrayParameterGetter.class, null);
	}

	public static ParameterGetterBuilder me() {
		return me;
	}
	
	/**
	 * 注册一个类型对应的参数获取器 
	 * ParameterGetterBuilder.me().regist(java.lang.String.class, StringParameterGetter.class, null);
	 * @param typeClass 类型，例如 java.lang.Integer.class
	 * @param pgClass 参数获取器实现类，必须继承ParameterGetter
	 * @param defaultValue，默认值，比如int的默认值为0， java.lang.Integer的默认值为null
	 */
	public <T> void regist(Class<T> typeClass, Class<? extends ParameterGetter<T>> pgClass, String defaultValue){
		this.typeMap.put(typeClass, new Holder(pgClass, defaultValue));
	}

	public ParameterGetterProcessor build(Class<? extends Controller> controllerClass, Method method) {
		final int parameterCount = method.getParameterCount();
		ParameterGetterProcessor opag = new ParameterGetterProcessor(parameterCount);
		if (0 == parameterCount) {
			return opag;
		}
		for (Parameter p : method.getParameters()) {
			IParameterGetter<?> pg = createParameterGetter(controllerClass, method, p);
			//存在文件的情况下，文件需要优先获取才行
			if (pg instanceof FileParameterGetter) {
				opag.addParameterGetterToHeader(pg);
			} else {
				opag.addParameterGetter(pg);
			}
		}
		return opag;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IParameterGetter<?> createParameterGetter(Class<? extends Controller> controllerClass, Method method,
			Parameter p) {
		String parameterName = p.getName();
		String defaultValue = null;
		Class<?> typeClass = p.getType();
		Para para = p.getAnnotation(Para.class);
		if (para != null) {
			parameterName = para.value().trim();
			defaultValue = para.defaultValue().trim();
			if (defaultValue.isEmpty()) {
				defaultValue = null;
			}
		}
		Holder holder = typeMap.get(typeClass);
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
		//枚举
		if(Enum.class.isAssignableFrom(typeClass)){
			return new EnumParameterGetter(typeClass,parameterName,defaultValue);
		}else if (com.jfinal.plugin.activerecord.Model.class.isAssignableFrom(typeClass)) {
			return new ModelParameterGetter<>(typeClass, parameterName);
		} else {
			return new BeanParameterGetter<>(typeClass, parameterName);
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
