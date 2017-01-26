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

import java.util.AbstractList;
import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * RangeArray : [expr .. expr]
 */
public class RangeArray extends Expr {
	
	private Expr start;
	private Expr end;
	
	/**
	 * array : '[' exprList ? | range ? ']'
	 * exprList : expr (',' expr)*
	 * range : expr .. expr
	 */
	public RangeArray(Expr start, Expr end, Location location) {
		if (start == null) {
			throw new ParseException("The start value of range array can not be blank", location);
		}
		if (end == null) {
			throw new ParseException("The end value of range array can not be blank", location);
		}
		this.start = start;
		this.end = end;
		this.location = location;
	}
	
	public Object eval(Scope scope) {
		Object startValue = start.eval(scope);
		if ( !(startValue instanceof Integer) ) {
			throw new TemplateException("The start value of range array must be Integer", location);
		}
		Object endValue = end.eval(scope);
		if ( !(endValue instanceof Integer) ) {
			throw new TemplateException("The end value of range array must be Integer", location);
		}
		
		return new RangeList((Integer)startValue, (Integer)endValue, location);
	}
	
	public static class RangeList extends AbstractList<Integer> {
		
		final int start;
		final int size;
		final int increment;
		final Location location;
		
		public RangeList(int start, int end, Location location) {
			this.start = start;
			this.increment = start <= end ? 1 : -1;
			this.size = Math.abs(end - start) + 1;
			this.location = location;
		}
		
		public Integer get(int index) {
			if (index < 0 || index >= size) {
				throw new TemplateException("Index out of bounds. Index: " + index + ", Size: " + size, location);
			}
			return start + index * increment;
		}
		
		public int size() {
			return size;
		}
	}
}




