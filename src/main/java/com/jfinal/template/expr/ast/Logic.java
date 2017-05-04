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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.Sym;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Logic
 * 
 * 支持逻辑运算： !  &&  ||
 */
public class Logic extends Expr {
	
	private Sym op;
	private Expr left;		// ! 运算没有 left 参数
	private Expr right;
	
	/**
	 * 构造 || && 结点
	 */
	public Logic(Sym op, Expr left, Expr right, Location location) {
		if (left == null) {
			throw new ParseException("The target of \"" + op.value() + "\" operator on the left side can not be blank", location);
		}
		if (right == null) {
			throw new ParseException("The target of \"" + op.value() + "\" operator on the right side can not be blank", location);
		}
		this.op = op;
		this.left = left;
		this.right = right;
		this.location = location;
	}
	
	/**
	 * 构造 ! 结点，left 为 null
	 */
	public Logic(Sym op, Expr right, Location location) {
		if (right == null) {
			throw new ParseException("The target of \"" + op.value() + "\" operator on the right side can not be blank", location);
		}
		this.op = op;
		this.left = null;
		this.right = right;
		this.location = location;
	}
	
	public Object eval(Scope scope) {
		switch (op) {
		case NOT:
			return evalNot(scope);
		case AND:
			return evalAnd(scope);
		case OR:
			return evalOr(scope);
		default:
			throw new TemplateException("Unsupported operator: " + op.value(), location);
		}
	}
	
	Object evalNot(Scope scope) {
		return ! isTrue(right.eval(scope));
	}
	
	Object evalAnd(Scope scope) {
		return isTrue(left.eval(scope)) && isTrue(right.eval(scope));
	}
	
	Object evalOr(Scope scope) {
		return isTrue(left.eval(scope)) || isTrue(right.eval(scope));
	}
	
	/**
	 * 规则：
	 * 1：null 返回 false
	 * 2：boolean 类型，原值返回
	 * 3：Map、Connection(List被包括在内) 返回 size() > 0
	 * 4：数组，返回 length > 0
	 * 5：String、StringBuilder、StringBuffer 等继承自 CharSequence 类的对象，返回 length > 0
	 * 6：Number 类型，返回 value != 0
	 * 7：Iterator 返回 hasNext() 值
	 * 8：其它返回 true
	 */
	public static boolean isTrue(Object v) {
		if (v == null) {
			return false;
		}
		if (v instanceof Boolean) {
			return (Boolean)v;
		}
		if (v instanceof Collection) {
			return ((Collection<?>)v).size() > 0;
		}
		if (v instanceof Map) {
			return ((Map<?, ?>)v).size() > 0;
		}
		if (v.getClass().isArray()) {
			return Array.getLength(v) > 0;
		}
		if (v instanceof CharSequence) {
			return ((CharSequence)v).length() > 0;
		}
		if (v instanceof Number) {
			if (v instanceof Double) {
				return ((Number)v).doubleValue() != 0;
			}
			if (v instanceof Float) {
				return ((Number)v).floatValue() != 0;
			}
			return ((Number)v).intValue() != 0;
		}
		if (v instanceof Iterator) {
			return ((Iterator<?>)v).hasNext();
		}
		return true;
	}
	
	public static boolean isFalse(Object v) {
		return !isTrue(v);
	}
}



