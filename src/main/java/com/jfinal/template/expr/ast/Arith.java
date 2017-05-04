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
import java.math.RoundingMode;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.Sym;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Arithmetic
 * 1：支持 int long float double BigDecimal 的 + - * / % 运算
 * 2：支持字符串加法运算
 */
public class Arith extends Expr {
	
	public static final int INT = 0;
	public static final int LONG = 1;
	public static final int FLOAT = 2;
	public static final int DOUBLE = 3;
	public static final int BIGDECIMAL = 4;
	
	// 后续版本考虑支持
	// public static final int BYTE = 5;
	// public static final int SHORT = 6;
	// public static final int BIGINTEGER = 7;
	
	private Sym op;
	private Expr left;
	private Expr right;
	
	public Arith(Sym op, Expr left, Expr right, Location location) {
		if (left == null || right == null) {
			throw new ParseException("The target of \"" + op.value() + "\" operator can not be blank", location);
		}
		this.op = op;
		this.left = left;
		this.right = right;
		this.location = location;
	}
	
	public Object eval(Scope scope) {
		try {
			return doEval(scope);
		} catch (TemplateException e) {
			throw e;
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
	
	private Object doEval(Scope scope) {
		Object leftValue = left.eval(scope);
		Object rightValue = right.eval(scope);
		
		if (leftValue instanceof Number && rightValue instanceof Number) {
			Number l = (Number)leftValue;
			Number r = (Number)rightValue;
			int maxType = getMaxType(l, r);
			
			switch (op) {
			case ADD:
				return add(maxType, l, r);
			case SUB:
				return sub(maxType, l, r);
			case MUL:
				return mul(maxType, l, r);
			case DIV:
				return div(maxType, l, r);
			case MOD:
				return mod(maxType, l, r);
			default :
				throw new TemplateException("Unsupported operator: " + op.value(), location);
			}
		}
		
		// 字符串加法运算
		if (leftValue instanceof String || rightValue instanceof String) {
            return String.valueOf(leftValue).concat(String.valueOf(rightValue));
        }
		
		String leftObj = leftValue != null ? leftValue.getClass().getName() : "null";
		String rightObj = rightValue != null ? rightValue.getClass().getName() : "null";
		throw new TemplateException("Unsupported operation type: " + leftObj + " " +  op.value() + " " + rightObj, location);
	}
	
	private int getMaxType(Number obj1, Number obj2) {
		int t1 = getType(obj1);
		if (t1 == BIGDECIMAL) {
			return BIGDECIMAL;
		}
		int t2 = getType(obj2);
		return t1 > t2 ? t1 : t2;
	}
	
	/**
	 * 注意：调用此方法的前提是，其中有一个对象的类型已经确定是 BigDecimal
	 */
	private BigDecimal[] toBigDecimals(Number left, Number right) {
		BigDecimal[] ret = new BigDecimal[2];
		if (left instanceof BigDecimal) {
			ret[0] = (BigDecimal)left;
			ret[1] = new BigDecimal(right.toString());
		} else {
			ret[0] = new BigDecimal(left.toString());
			ret[1] = (BigDecimal)right;
		}
		return ret;
	}
	
	private int getType(Number obj) {
		if (obj instanceof Integer) {
			return INT;
		} else if (obj instanceof Long) {
			return LONG;
		} else if (obj instanceof Float) {
			return FLOAT;
		} else if (obj instanceof Double) {
			return DOUBLE;
		} else if (obj instanceof BigDecimal) {
			return BIGDECIMAL;
		}
		throw new TemplateException("Unsupported data type: " + obj.getClass().getName(), location);
	}
	
	private Number add(int maxType, Number left, Number right) {
		switch (maxType) {
		case INT:
			return Integer.valueOf(left.intValue() + right.intValue());
		case LONG:
			return Long.valueOf(left.longValue() + right.longValue());
		case FLOAT:
			return Float.valueOf(left.floatValue() + right.floatValue());
		case DOUBLE:
			return Double.valueOf(left.doubleValue() + right.doubleValue());
		case BIGDECIMAL:
			BigDecimal[] bd = toBigDecimals(left, right);
			return (bd[0]).add(bd[1]);
		}
		throw new TemplateException("Unsupported data type", location);
	}
	
	private Number sub(int maxType, Number left, Number right) {
		switch (maxType) {
		case INT:
			return Integer.valueOf(left.intValue() - right.intValue());
		case LONG:
			return Long.valueOf(left.longValue() - right.longValue());
		case FLOAT:
			return Float.valueOf(left.floatValue() - right.floatValue());
		case DOUBLE:
			return Double.valueOf(left.doubleValue() - right.doubleValue());
		case BIGDECIMAL:
			BigDecimal[] bd = toBigDecimals(left, right);
			return (bd[0]).subtract(bd[1]);
		}
		throw new TemplateException("Unsupported data type", location);
	}
	
	private Number mul(int maxType, Number left, Number right) {
		switch (maxType) {
		case INT:
			return Integer.valueOf(left.intValue() * right.intValue());
		case LONG:
			return Long.valueOf(left.longValue() * right.longValue());
		case FLOAT:
			return Float.valueOf(left.floatValue() * right.floatValue());
		case DOUBLE:
			return Double.valueOf(left.doubleValue() * right.doubleValue());
		case BIGDECIMAL:
			BigDecimal[] bd = toBigDecimals(left, right);
			return (bd[0]).multiply(bd[1]);
		}
		throw new TemplateException("Unsupported data type", location);
	}
	
	private Number div(int maxType, Number left, Number right) {
		switch (maxType) {
		case INT:
			return Integer.valueOf(left.intValue() / right.intValue());
		case LONG:
			return Long.valueOf(left.longValue() / right.longValue());
		case FLOAT:
			return Float.valueOf(left.floatValue() / right.floatValue());
		case DOUBLE:
			return Double.valueOf(left.doubleValue() / right.doubleValue());
		case BIGDECIMAL:
			BigDecimal[] bd = toBigDecimals(left, right);
			// return (bd[0]).divide(bd[1]);
			return (bd[0]).divide(bd[1], RoundingMode.HALF_EVEN);	// 银行家舍入法
		}
		throw new TemplateException("Unsupported data type", location);
	}
	
	private Number mod(int maxType, Number left, Number right) {
		switch (maxType) {
		case INT:
			return Integer.valueOf(left.intValue() % right.intValue());
		case LONG:
			return Long.valueOf(left.longValue() % right.longValue());
		case FLOAT:
			return Float.valueOf(left.floatValue() % right.floatValue());
		case DOUBLE:
			return Double.valueOf(left.doubleValue() % right.doubleValue());
		case BIGDECIMAL:
			BigDecimal[] bd = toBigDecimals(left, right);
			return (bd[0]).divideAndRemainder(bd[1])[1];
		}
		throw new TemplateException("Unsupported data type", location);
	}
}




