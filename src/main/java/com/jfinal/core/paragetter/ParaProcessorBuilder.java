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
import com.jfinal.kit.LogKit;

public class ParaProcessorBuilder {

	private final static ParaProcessorBuilder me = new ParaProcessorBuilder();
	private Map<String, Holder> typeMap = new HashMap<String, Holder>();

	private ParaProcessorBuilder() {
		regist(short.class, ShortGetter.class, "0");
		regist(int.class, IntegerGetter.class, "0");
		regist(long.class, LongGetter.class, "0");
		regist(float.class, FloatGetter.class, "0");
		regist(double.class, DoubleGetter.class, "0");
		regist(boolean.class, BooleanGetter.class, "false");
		regist(java.lang.Short.class, ShortGetter.class, null);
		regist(java.lang.Integer.class, IntegerGetter.class, null);
		regist(java.lang.Long.class, LongGetter.class, null);
		regist(java.lang.Float.class, FloatGetter.class, null);
		regist(java.lang.Double.class, DoubleGetter.class, null);
		regist(java.lang.Boolean.class, BooleanGetter.class, null);
		regist(java.lang.String.class, StringGetter.class, "");
		regist(java.util.Date.class, DateGetter.class, null);
		regist(java.sql.Date.class, SqlDateGetter.class, null);
		regist(java.sql.Time.class, TimeGetter.class, null);
		regist(java.sql.Timestamp.class, TimestampGetter.class, null);
		regist(java.math.BigDecimal.class, BigDecimalGetter.class, null);
		regist(java.math.BigInteger.class, BigIntegerGetter.class, null);
		regist(java.io.File.class, FileGetter.class, null);
		regist(com.jfinal.upload.UploadFile.class, UploadFileGetter.class, null);
		regist(java.lang.String[].class, StringArrayGetter.class, null);
		regist(java.lang.Integer[].class, IntegerArrayGetter.class, null);
		regist(java.lang.Long[].class, LongArrayGetter.class, null);
		regist(com.jfinal.core.paragetter.RawPostData.class, RawPostDataGetter.class, null);
		
	}

	public static ParaProcessorBuilder me() {
		return me;
	}
	
	/**
	 * 注册一个类型对应的参数获取器 
	 * ParameterGetterBuilder.me().regist(java.lang.String.class, StringParaGetter.class, null);
	 * @param typeClass 类型，例如 java.lang.Integer.class
	 * @param pgClass 参数获取器实现类，必须继承ParaGetter
	 * @param defaultValue，默认值，比如int的默认值为0， java.lang.Integer的默认值为null
	 */
	public <T> void regist(Class<T> typeClass, Class<? extends ParaGetter<T>> pgClass, String defaultValue){
		this.typeMap.put(typeClass.getName(), new Holder(pgClass, defaultValue));
	}

	public ParaProcessor build(Class<? extends Controller> controllerClass, Method method) {
		final int parameterCount = method.getParameterCount();
		ParaProcessor opag = new ParaProcessor(parameterCount);
		if (0 == parameterCount) {
			return opag;
		}
		for (Parameter p : method.getParameters()) {
			IParaGetter<?> pg = createParaGetter(controllerClass, method, p);
			//存在文件的情况下，文件需要优先获取才行
			if (pg instanceof FileGetter) {
				opag.addParaGetterToHeader(pg);
			} else {
				opag.addParaGetter(pg);
			}
		}
		return opag;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IParaGetter<?> createParaGetter(Class<? extends Controller> controllerClass, Method method,
			Parameter p) {
		if(!p.isNamePresent()) {
			LogKit.warn("you should add compiler flag -parameters to support parameter auto binding");
		}
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
		Holder holder = typeMap.get(typeClass.getName());
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
			return new EnumGetter(typeClass,parameterName,defaultValue);
		}else if (com.jfinal.plugin.activerecord.IBean.class.isAssignableFrom(typeClass)) {
			//实现了IBean接口，优先按BeanGetter来处理。
			return new BeanGetter(typeClass, parameterName);
		}else if (com.jfinal.plugin.activerecord.Model.class.isAssignableFrom(typeClass)) {
			return new ModelGetter(typeClass, parameterName);
		} else {
			return new BeanGetter(typeClass, parameterName);
		}
	}

	private static class Holder {
		private final String defaultValue;
		private final Class<? extends ParaGetter<?>> clazz;

		Holder(Class<? extends ParaGetter<?>> clazz, String defaultValue) {
			this.clazz = clazz;
			this.defaultValue = defaultValue;
		}
		final String getDefaultValue() {
			return defaultValue;
		}
		ParaGetter<?> born(String parameterName, String defaultValue) throws Exception {
			Constructor<? extends ParaGetter<?>> con = clazz.getConstructor(String.class, String.class);
			return con.newInstance(parameterName, defaultValue);
		}
	}
}
