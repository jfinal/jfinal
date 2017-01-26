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

import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;
import java.lang.reflect.Field;

/**
 * StaticField : ID_list '::' ID
 * 动态获取静态变量值，变量值改变时仍可正确获取
 * 用法：com.jfinal.core.Const::JFINAL_VERSION
 */
public class StaticField extends Expr {
	
	private Class<?> clazz;
	private String fieldName;
	private Field field;
	
	public StaticField(String className, String fieldName, Location location) {
		try {
			this.clazz = Class.forName(className);
			this.fieldName = fieldName;
			this.field = clazz.getField(fieldName);
			this.location = location;
		} catch (Exception e) {
			throw new ParseException(e.getMessage(), location, e);
		}
	}
	
	public Object eval(Scope scope) {
		try {
			return field.get(null);
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
	
	public String toString() {
		return clazz.getName() + "::" + fieldName;
	}
}







