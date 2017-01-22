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
import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * index : expr '[' expr ']'
 * 
 * 支持 a[i]、 a[b[i]]、a[i][j]、a[i][j]...[n]
 */
public class Index extends Expr {
	
	private Expr expr;
	private Expr index;
	
	public Index(Expr expr, Expr index, Location location) {
		if (expr == null || index == null) {
			throw new ParseException("array/list/map and their index can not be null", location);
		}
		this.expr = expr;
		this.index = index;
		this.location = location;
	}
	
	@SuppressWarnings("rawtypes")
	public Object eval(Scope scope) {
		Object array = expr.eval(scope);
		if (array == null) {
			if (scope.getCtrl().isNullSafe()) {
				return null;
			}
			throw new TemplateException("The index access operation target can not be null", location);
		}
		
		Object idx = index.eval(scope);
		if (idx == null) {
			if (scope.getCtrl().isNullSafe()) {
				return null;
			}
			throw new TemplateException("The index of list/array and the key of map can not be null", location);
		}
		
		if (array instanceof List) {
			if (idx instanceof Integer) {
				return ((List<?>)array).get((Integer)idx);
			}
			throw new TemplateException("The index of list can only be integer", location);
		}
		
		if (array instanceof java.util.Map) {
			return ((java.util.Map)array).get(idx);
		}
		
		if (array.getClass().isArray()) {
			if (idx instanceof Integer) {
				return java.lang.reflect.Array.get(array, (Integer)idx);
			}
			throw new TemplateException("The index of array can only be integer", location);
		}
		
		throw new TemplateException("Only the list array and map is supported by index access", location);
	}
}




