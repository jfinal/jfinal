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

import java.util.Map;
import com.jfinal.core.Action;
import com.jfinal.core.ActionHandler;
import com.jfinal.core.Controller;
import com.jfinal.kit.ReflectKit;
import com.jfinal.plugin.activerecord.Model;

/**
 * 注意：json 请求中的字段名必须与数据库字段名一致，常见错误是 json 字段用了驼峰而数据库字段用了下划线
 */
public class ModelGetter<T> extends ParaGetter<T> {

	private final Class<T> modelClass;
	
	public ModelGetter(Class<T> modelClass, String parameterName) {
		super(parameterName,null);
		this.modelClass = modelClass;
	}
	
	@Override
	public T get(Action action, Controller c) {
		// 支持 json 数据请求注入 action 形参
		if (ActionHandler.resolveJson && c.isJsonRequest()) {
			return resolveJson((JsonRequest)c.getRequest());
		} else {
			return c.getModel(modelClass, this.getParameterName(), true);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private T resolveJson(JsonRequest req) {
		com.alibaba.fastjson.JSONObject jsonObj = req.getJSONObject();
		if (jsonObj == null) {
			return null;
		}
		
		String paraName = this.getParameterName();
		Map<String, Object> attrs;
		if (jsonObj.containsKey(paraName)) {
			// 存在与 action 形参名相同的 request 参数则使用其 value 值进行转换
			// attrs = JSON.parseObject(req.getParameter(paraName), Map.class);
			attrs = jsonObj.getObject(paraName, Map.class);
		} else {
			// 否则使用整个请求中的 json 进行转换
			// attrs = JSON.parseObject(c.getRawData(), Map.class);
			attrs = jsonObj.toJavaObject(Map.class);
		}
		
		Model ret = (Model) ReflectKit.newInstance(modelClass);
		return (T)ret._setOrPut(attrs);
	}
	
	@Override
	protected T to(String v) {
		return null;
	}
}
