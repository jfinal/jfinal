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

import java.util.List;
import java.util.Map;
import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Assign
 * 
 * 支持三种赋值，其中第二种如果括号中是 ID 或 STR 则演变为第三种是对 map 赋值：
 * 1：ID = expr
 * 2：ID [ expr ] = expr
 *   如果 expr 为 int 或 long 型，则是对 array 赋值
 *   如果 expr 为 ID、STR 型，则是对 map 进行赋值
 *   否则抛异常出来
 * 3：ID [ ID ] = expr 或者 ID [ STR ] = expr
 * 4：支持无限连：id = array[ i = 0 ] = array[1] = 123
 */
public class Assign extends Expr {
	
	private String id;
	private Expr index;	// index 用于支持 ID [ expr ] = expr 这种形式
	private Expr right;
	
	/**
	 * 数组赋值表达式
	 */
	public Assign(String id, Expr index, Expr right, Location location) {
		if (index == null) {
			throw new ParseException("The index expression of array assignment can not be null", location);
		}
		if (right == null) {
			throw new ParseException("The expression on the right side of an assignment expression can not be null", location);
		}
		this.id = id;
		this.index = index;
		this.right = right;
		this.location = location;
	}
	
	/**
	 * 普通赋值表达式
	 */
	public Assign(String id, Expr right, Location location) {
		if (right == null) {
			throw new ParseException("The expression on the right side of an assignment expression can not be null", location);
		}
		this.id = id;
		this.index = null;
		this.right = right;
		this.location = location;
	}
	
	/**
	 * 赋值语句有返回值，可以用于表达式计算
	 */
	public Object eval(Scope scope) {
		if (index == null) {
			return assignVariable(scope);
		} else {
			return assignElement(scope);
		}
	}
	
	Object assignVariable(Scope scope) {
		Object rightValue = right.eval(scope);
		if (scope.getCtrl().isWisdomAssignment()) {
			scope.set(id, rightValue);
		} else if (scope.getCtrl().isLocalAssignment()) {
			scope.setLocal(id, rightValue);
		} else {
			scope.setGlobal(id, rightValue);
		}
		
		return rightValue;
	}
	
	/**
	 * 数组或 Map 赋值
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	Object assignElement(Scope scope) {
		Object target = scope.get(id);
		if (target == null) {
			throw new TemplateException("The assigned targets \"" + id + "\" can not be null", location);
		}
		Object idx = index.eval(scope);
		if (idx == null) {
			throw new TemplateException("The index of list/array and the key of map can not be null", location);
		}
		
		Object value;
		if (target instanceof Map) {
			value = right.eval(scope);
			((Map)target).put(idx, value);
			return value;
		}
		
		if ( !(idx instanceof Integer) ) {
			throw new TemplateException("The index of list/array can only be integer", location);
		}
		
		if (target instanceof List) {
			value = right.eval(scope);
			((List)target).set((Integer)idx, value);
			return value;
		}
		if (target.getClass().isArray()) {
			value = right.eval(scope);
			java.lang.reflect.Array.set(target, (Integer)idx, value);
			return value;
		}
		
		throw new TemplateException("Only the list array and map is supported by index assignment", location);
	}
}







