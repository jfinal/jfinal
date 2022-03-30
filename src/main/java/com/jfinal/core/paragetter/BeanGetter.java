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

import com.jfinal.core.Action;
import com.jfinal.core.Controller;

public class BeanGetter<T> extends ParaGetter<T> {
	
	private final Class<T> beanClass;
	
	public BeanGetter(Class<T> beanClass, String parameterName) {
		super(parameterName, null);
		this.beanClass = beanClass;
	}
	
	@Override
	public T get(Action action, Controller c) {
		// 支持 json 数据请求注入 action 形参
		if (ParaProcessor.resolveJson && c.isJsonRequest()) {
			return resolveJson((JsonRequest)c.getRequest());
		} else {
			return c.getBean(beanClass, this.getParameterName(), true);
		}
	}
	
	private T resolveJson(JsonRequest req) {
		com.alibaba.fastjson.JSONObject jsonObj = req.getJSONObject();
		if (jsonObj == null) {
			return null;
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
	
	@Override
	protected T to(String v) {
		return null;
	}
}
