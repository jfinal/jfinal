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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.Sym;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * 自增与自减
 */
public class IncDec extends Expr {
	
	private Sym op;
	private String id;
	private boolean isPost;	// 是否是后缀形式： i++  i--
	
	public IncDec(Sym op, boolean isPost, Expr id, Location location) {
		if (id == null) {
			throw new ParseException(op.value() + " operator requires target to be operational", location);
		}
		if ( !(id instanceof Id) ) {
			throw new ParseException(op.value() + " operator only supports identifiers", location);
		}
		
		this.op = op;
		this.id = ((Id)id).getId();
		this.isPost = isPost;
		this.location = location;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Object eval(Scope scope) {
		Map map = scope.getMapOfValue(id);
		if (map == null) {
			if (scope.getCtrl().isNullSafe()) {
				return null;
			}
			throw new TemplateException("The target of " + op.value() + " operator can not be null", location);
		}
		Object value = map.get(id);
		if ( !(value instanceof Number) ) {
			throw new TemplateException(op.value() + " operator only support int long float double and BigDecimal type", location);
		}
		
		Number newValue;
		switch (op) {
		case INC:
			newValue = inc((Number)value);
			break ;
		case DEC:
			newValue = dec((Number)value);
			break ;
		default:
			throw new TemplateException("Unsupported operator: " + op.value(), location);
		}
		map.put(id, newValue);
		return isPost ? value : newValue;
	}
	
	private Number inc(Number num) {
		if (num instanceof Integer) {
			return Integer.valueOf(num.intValue() + 1);
		}
		if (num instanceof Long) {
			return Long.valueOf(num.longValue() + 1L);
		}
		if (num instanceof Float) {
			return Float.valueOf(num.floatValue() + 1F);
		}
		if (num instanceof Double) {
			return Double.valueOf(num.doubleValue() + 1D);
		}
		if (num instanceof BigDecimal) {
			return ((BigDecimal)num).add(BigDecimal.ONE);
		}
		if (num instanceof BigInteger) {
			return ((BigInteger)num).add(BigInteger.ONE);
		}
		if (num instanceof Short) {
			return (short)(((Short)num).shortValue() + 1);
		}
		if (num instanceof Byte) {
			return (byte)(((Byte)num).byteValue() + 1);
		}
		return num.intValue() + 1;
	}
	
	private Number dec(Number num) {
		if (num instanceof Integer) {
			return Integer.valueOf(num.intValue() - 1);
		}
		if (num instanceof Long) {
			return Long.valueOf(num.longValue() - 1L);
		}
		if (num instanceof Float) {
			return Float.valueOf(num.floatValue() - 1F);
		}
		if (num instanceof Double) {
			return Double.valueOf(num.doubleValue() - 1D);
		}
		if (num instanceof BigDecimal) {
			return ((BigDecimal)num).subtract(BigDecimal.ONE);
		}
		if (num instanceof BigInteger) {
			return ((BigInteger)num).subtract(BigInteger.ONE);
		}
		if (num instanceof Short) {
			return (short)(((Short)num).shortValue() - 1);
		}
		if (num instanceof Byte) {
			return (byte)(((Byte)num).byteValue() - 1);
		}
		return num.intValue() - 1;
	}
}



