/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
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

import com.jfinal.kit.HashKit;
import com.jfinal.kit.StrKit;
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
	private long getterNameHash;
	
	public Field(Expr expr, String fieldName, Location location) {
		if (expr == null) {
			throw new ParseException("The object for field access can not be null", location);
		}
		this.expr = expr;
		this.fieldName = fieldName;
		this.getterName = "get" + StrKit.firstCharToUpperCase(fieldName);
		// fnv1a64 hash 到比 String.hashCode() 更大的 long 值范围
		this.getterNameHash = HashKit.fnv1a64(getterName);
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
		
		
		try {
			Class<?> targetClass = target.getClass();
			Object key = FieldKeyBuilder.instance.getFieldKey(targetClass, getterNameHash);
			FieldGetter fieldGetter = FieldKit.getFieldGetter(key, targetClass, fieldName);
			if (fieldGetter.notNull()) {
				return fieldGetter.get(target, fieldName);
			}
		} catch (TemplateException | ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
		
		
		if (scope.getCtrl().isNullSafe()) {
			return null;
		}
		if (expr instanceof Id) {
			String id = ((Id)expr).getId();
			throw new TemplateException("public field not found: \"" + id + "." + fieldName + "\" and public getter method not found: \"" + id + "." + getterName + "()\"", location);
		}
		throw new TemplateException("public field not found: \"" + fieldName + "\" and public getter method not found: \"" + getterName + "()\"", location);
	}
	
	// private Long buildFieldKey(Class<?> targetClass) {
		// return targetClass.getName().hashCode() ^ getterNameHash;
	// }
}






