/**
 * Copyright (c) 2011-2023, 玛雅牛 (myaniu AT gmail.com).
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

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import com.jfinal.core.Action;
import com.jfinal.core.ActionHandler;
import com.jfinal.core.Controller;

public class BeanGetter<T> extends ParaGetter<T> {
	
	private final Class<T> beanClass;
	
	// 存放参数泛型，支持将 json 数组转化为带有泛型的 List，例如： List<User>、List<Integer>
	private final Class<?> parameterizedType;
	
	public BeanGetter(Class<T> beanClass, String parameterName, Parameter parameter) {
		super(parameterName, null);
		this.beanClass = beanClass;
		this.parameterizedType = getParameterizedType(parameter);
	}
	
	private Class<?> getParameterizedType(Parameter parameter) {
		if (parameter != null) {
			Type type = parameter.getParameterizedType();
			if (type instanceof ParameterizedType) {
				Type[] ts = ((ParameterizedType)type).getActualTypeArguments();
				if (ts != null && ts.length > 0) {
					return ts[0] instanceof Class ? (Class<?>)ts[0] : null;
				}
			}
		}
		return null;
	}
	
	@Override
	public T get(Action action, Controller c) {
		// 支持 json 数据请求注入 action 形参
		if (ActionHandler.resolveJson && c.isJsonRequest()) {
			return resolveJson((JsonRequest)c.getRequest());
		} else {
			return c.getBean(beanClass, this.getParameterName(), true);
		}
	}
	
	private T resolveJson(JsonRequest req) {
		com.alibaba.fastjson.JSONObject jsonObj = req.getJSONObject();
		if (jsonObj == null) {
			return toList(req.getJSONArray());
		}
		
		String paraName = this.getParameterName();
		if (jsonObj.containsKey(paraName)) {
			// 存在与 action 形参名相同的 request 参数则使用其 value 值进行转换
			return jsonObj.getObject(paraName, beanClass);
		} else {
			// 否则使用整个请求中的 json 进行转换
			return jsonObj.toJavaObject(beanClass);
		}
	}
	
	@SuppressWarnings("unchecked")
	private T toList(com.alibaba.fastjson.JSONArray jsonArr) {
		if (jsonArr == null) {
			return null;
		}
		
		if (parameterizedType != null) {
			return (T) jsonArr.toJavaList(parameterizedType);
		} else {
			return (T) jsonArr.toJavaList(Object.class);
		}
	}
	
	@Override
	protected T to(String v) {
		return null;
	}
}
