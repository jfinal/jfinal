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

package com.jfinal.core;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.TableMapping;

/**
 * Injector.
 */
public class Injector {
	
	private static <T> T createInstance(Class<T> objClass) {
		try {
			return objClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T injectModel(Class<T> modelClass, HttpServletRequest request, boolean skipConvertError) {
		String modelName = modelClass.getSimpleName();
		return (T)injectModel(modelClass, StrKit.firstCharToLowerCase(modelName), request, skipConvertError);
	}
	
	public static <T> T injectBean(Class<T> beanClass, HttpServletRequest request, boolean skipConvertError) {
		String beanName = beanClass.getSimpleName();
		return (T)injectBean(beanClass, StrKit.firstCharToLowerCase(beanName), request, skipConvertError);
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T injectBean(Class<T> beanClass, String beanName, HttpServletRequest request, boolean skipConvertError) {
		Object bean = createInstance(beanClass);
		String modelNameAndDot = StrKit.notBlank(beanName) ? beanName + "." : null;
		
		Map<String, String[]> parasMap = request.getParameterMap();
		Method[] methods = beanClass.getMethods();
		for (Method method : methods) {
			String methodName = method.getName();
			if (methodName.startsWith("set") == false || methodName.length() <= 3) {	// only setter method
				continue;
			}
			Class<?>[] types = method.getParameterTypes();
			if (types.length != 1) {						// only one parameter
				continue;
			}
			
			String attrName = StrKit.firstCharToLowerCase(methodName.substring(3));
			String paraName = modelNameAndDot != null ? modelNameAndDot + attrName : attrName;
			if (parasMap.containsKey(paraName)) {
				try {
					String paraValue = request.getParameter(paraName);
					Object value = paraValue != null ? TypeConverter.convert(types[0], paraValue) : null;
					method.invoke(bean, value);
				} catch (Exception e) {
					if (skipConvertError == false) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		return (T)bean;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T injectModel(Class<T> modelClass, String modelName, HttpServletRequest request, boolean skipConvertError) {
		Object temp = createInstance(modelClass);
		if (temp instanceof Model == false) {
			throw new IllegalArgumentException("getModel only support class of Model, using getBean for other class.");
		}
		
		Model<?> model = (Model<?>)temp;
		Table table = TableMapping.me().getTable(model.getClass());
		if (table == null) {
			throw new ActiveRecordException("The Table mapping of model: " + modelClass.getName() + 
					" not exists or the ActiveRecordPlugin not start.");
		}
		
		String modelNameAndDot = StrKit.notBlank(modelName) ? modelName + "." : null;
		Map<String, String[]> parasMap = request.getParameterMap();
		// 对 paraMap进行遍历而不是对table.getColumnTypeMapEntrySet()进行遍历，以便支持 CaseInsensitiveContainerFactory
		// 以及支持界面的 attrName有误时可以感知并抛出异常避免出错
		for (Entry<String, String[]> entry : parasMap.entrySet()) {
			String paraName = entry.getKey();
			String attrName;
			if (modelNameAndDot != null) {
				if (paraName.startsWith(modelNameAndDot)) {
					attrName = paraName.substring(modelNameAndDot.length());
				} else {
					continue ;
				}
			} else {
				attrName = paraName;
			}
			
			Class<?> colType = table.getColumnType(attrName);
			if (colType == null) {
				if (skipConvertError) {
					continue ;
				} else {
					throw new ActiveRecordException("The model attribute " + attrName + " is not exists.");
				}
			}
			
			try {
				String[] paraValueArray = entry.getValue();
				String paraValue = (paraValueArray != null && paraValueArray.length > 0) ? paraValueArray[0] : null;
				
				Object value = paraValue != null ? TypeConverter.convert(colType, paraValue) : null;
				model.set(attrName, value);
			} catch (Exception e) {
				if (skipConvertError == false) {
					throw new RuntimeException("Can not convert parameter: " + paraName, e);
				}
			}
		}
		
		return (T)model;
	}
}

