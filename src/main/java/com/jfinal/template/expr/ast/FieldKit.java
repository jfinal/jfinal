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

package com.jfinal.template.expr.ast;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FieldKit
 */
public class FieldKit {
	
	private static final ConcurrentHashMap<String, Object> fieldCache = new ConcurrentHashMap<String, Object>();
	
	public static Field getField(String key, Class<?> targetClass, String fieldName) {
		Object field = fieldCache.get(key);
		if (field == null) {
			field = doGetField(targetClass, fieldName);
			if (field != null) {
				fieldCache.putIfAbsent(key, field);
			} else {
				// 对于不存在的 Field，只进行一次获取操作，主要为了支持 null safe，未来需要考虑内存泄漏风险
				fieldCache.put(key, Boolean.FALSE);
			}
		}
		return field instanceof Field ? (Field)field : null;
	}
	
	private static Field doGetField(Class<?> targetClass, String fieldName) {
		Field[] fs = targetClass.getFields();
		for (Field f : fs) {
			if (f.getName().equals(fieldName)) {
				return f;
			}
		}
		return null;
	}
	
	/**
	 * 获取 Field 用于缓存的 key
	 */
	public static String getFieldKey(Class<?> targetClass, String getterName) {
        return new StringBuilder(64).append(targetClass.getName())
        		.append('.').append(getterName).toString();
    }
}





