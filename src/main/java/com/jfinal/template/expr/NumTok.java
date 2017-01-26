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

package com.jfinal.template.expr;

import java.math.BigDecimal;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;

/**
 * NumToken 封装所有数值类型，并进行类型转换，以便尽早抛出异常
 * 
 * java 数值类型规则：
 * 1：科学计数法默认为 double 类型，通过 Object v = 123E1; 测试可知
 * 2：出现小数点的浮点数默认为 double 类型，无需指定 D/d 后缀。 而 float 类型必须指令 F/f 后缀
 * 3：double、float (出现小数点即为浮点数) 只支持 10 进制：16 进制形式去书写直接报错，8 进制形式去书写被当成 10 进制
 * 4：16 进制不支持科学计数法，因为 E/e 后缀会被当成是普通的 16 进制数字，而 +/- 号则被当成了加/减法运算
 * 5： 8 进制在本质上不支持科学计数法，010E1 这样的科学计数写法会被当成 10 进制，去掉后面的 E1 变为 010 时才被当成 8 进制
 * 6：所以 16 8 进制都不支持科学计数法，结论是对科学计数法的类型转换无需指定 radix 参数，而 BigDecimal 正好也不支持这个参数
 * 
 * 概要：
 * 1：16 8 进制不支持浮点数
 *   前者直接报错，后者直接忽略前缀 0 并当作 10 进制处理
 *   
 * 2：16 8 进制不支持科学计数法
 *   虽然二者在书写方式上被允许写成 16 8 进制，但只将其当成 10 进制处理，前者将 E/e 当成16进制数字
 *   后者忽略前缀 0 当成 10 进制处理，即看似 8 进制的科学计数法，实质是 10 进制科学计数法
 *   
 * 3： 科学计数法在本质上是 double，所以总结为一点 ---> 16 8 进制只支持整型数据
 */
public class NumTok extends Tok {
	
	private Object value;
	
	NumTok(Sym sym, String s, int radix, boolean isScientificNotation, Location location) {
		super(sym, location.getRow());
		try {
			typeConvert(sym, s, radix, isScientificNotation, location);
		} catch (Exception e) {
			throw new ParseException(e.getMessage(), location, e);
		}
	}
	
	private void typeConvert(Sym sym, String s, int radix, boolean isScientificNotation, Location location) {
		switch (sym) {
		case INT:
			if (isScientificNotation) {
				value = new BigDecimal(s).intValue();
			} else {
				value = Integer.valueOf(s, radix);		// 整型数据才支持 16 8 进制
			}
			break ;
		case LONG:
			if (isScientificNotation) {
				value = new BigDecimal(s).longValue();
			} else {
				value = Long.valueOf(s, radix);			// 整型数据才支持 16 8 进制
			}
			break ;
		case FLOAT:
			if (isScientificNotation) {
				value = new BigDecimal(s).floatValue();
			} else {
				value = Float.valueOf(s);				// 浮点数只支持 10 进制
			}
			break ;
		case DOUBLE:
			if (isScientificNotation) {
				value = new BigDecimal(s).doubleValue();
			} else {
				value = Double.valueOf(s);				// 浮点数只支持 10 进制
			}
			break ;
		default :
			throw new ParseException("Unsupported type: " + sym.value(), location);
		}
	}
	
	public String value() {
		return value.toString();
	}
	
	public Object getNumberValue() {
		return value;
	}
	
	public String toString() {
		return sym.value() + " : " + value;
	}
}
