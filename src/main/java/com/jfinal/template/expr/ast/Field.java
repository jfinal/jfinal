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

import java.lang.reflect.Array;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Field
 * 
 * field 表达式取值优先次序，以 user.name 为例
 * 1：假如 user.getName() 存在，则优先调用
 * 2：假如 user 为 Model 子类，则调用 user.get("name")
 * 3：假如 user 为 Record，则调用 user.get("name")
 * 4：假如 user 为 Map，则调用 user.get("name")
 * 5：假如 user 具有 public name 属性，则取 user.name 属性值
 */
public class Field extends Expr {
	
	private Expr expr;
	private String fieldName;
	private String getterName;
	
	public Field(Expr expr, String fieldName, Location location) {
		if (expr == null) {
			throw new ParseException("The object for field access can not be null", location);
		}
		this.expr = expr;
		this.fieldName = fieldName;
		this.getterName = "get" + StrKit.firstCharToUpperCase(fieldName);
		this.location = location;
	}
	
	public Object eval(Scope scope) {
		Object target = expr.eval(scope);
		if (target == null) {
			if (scope.getCtrl().isNullSafe()) {
				return null;
			}
			if (expr instanceof Id) {
				String id = ((Id)expr).getId();
				throw new TemplateException("\"" + id + "\" can not be null for accessed by \"" + id + "." + fieldName + "\"", location);
			}
			throw new TemplateException("Can not accessed by \"" + fieldName + "\" field from null target", location);
		}
		
		Class<?> targetClass = target.getClass();
		String key = FieldKit.getFieldKey(targetClass, getterName);
		MethodInfo getter;
		try {
			getter = MethodKit.getGetterMethod(key, targetClass, getterName);
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
		
		try {
			if (getter != null) {
				return getter.invoke(target, ExprList.NULL_OBJECT_ARRAY);
			}
			if (target instanceof Model) {
				return ((Model<?>)target).get(fieldName);
			}
			if (target instanceof Record) {
				return ((Record)target).get(fieldName);
			}
			if (target instanceof java.util.Map) {
				return ((java.util.Map<?, ?>)target).get(fieldName);
			}
			// if (target instanceof com.jfinal.kit.Ret) {
				// return ((com.jfinal.kit.Ret)target).get(fieldName);
			// }
			java.lang.reflect.Field field = FieldKit.getField(key, targetClass, fieldName);
			if (field != null) {
				return field.get(target);
			}
			
			// 支持获取数组长度： array.length
			if ("length".equals(fieldName) && target.getClass().isArray()) {
				return Array.getLength(target);
			}
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
		
		if (scope.getCtrl().isNullSafe()) {
			return null;
		}
		
		if (expr instanceof Id) {
			String id = ((Id)expr).getId();
			throw new TemplateException("Field not found: \"" + id + "." + fieldName + "\" and getter method not found: \"" + id + "." + getterName + "()\"", location);
		}
		throw new TemplateException("Field not found: \"" + fieldName + "\" and getter method not found: \"" + getterName + "()\"", location);
	}
}






