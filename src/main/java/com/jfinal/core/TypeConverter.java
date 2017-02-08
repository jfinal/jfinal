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

package com.jfinal.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Convert String to other type object.
 */
final class TypeConverter {
	
	private static final String timeStampPattern = "yyyy-MM-dd HH:mm:ss";
	private static final String datePattern = "yyyy-MM-dd";
	private static final int dateLen = datePattern.length();
	
	/**
	 * test for all types of mysql
	 * 
	 * 表单提交测试结果:
	 * 1: 表单中的域,就算不输入任何内容,也会传过来 "", 也即永远不可能为 null.
	 * 2: 如果输入空格也会提交上来
	 * 3: 需要考 model中的 string属性,在传过来 "" 时是该转成 null还是不该转换,
	 *    我想, 因为用户没有输入那么肯定是 null, 而不该是 ""
	 * 
	 * 注意: 1:当type参数不为String.class, 且参数s为空串blank的情况,
	 *       此情况下转换结果为 null, 而不应该抛出异常
	 *      2:调用者需要对被转换数据做 null 判断，参见 ModelInjector 的两处调用
	 */
	public static final Object convert(Class<?> type, String s) throws ParseException {
		// mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
		if (type == String.class) {
			return ("".equals(s) ? null : s);	// 用户在表单域中没有输入内容时将提交过来 "", 因为没有输入,所以要转成 null.
		}
		s = s.trim();
		if ("".equals(s)) {	// 前面的 String跳过以后,所有的空字符串全都转成 null,  这是合理的
			return null;
		}
		// 以上两种情况无需转换,直接返回, 注意, 本方法不接受null为 s 参数(经测试永远不可能传来null, 因为无输入传来的也是"")
		
		
		// mysql type: int, integer, tinyint(n) n > 1, smallint, mediumint
		if (type == Integer.class || type == int.class) {
			return Integer.parseInt(s);
		}
		
		// mysql type: bigint
		if (type == Long.class || type == long.class) {
			return Long.parseLong(s);
		}
		
		// java.util.Date 类型专为传统 java bean 带有该类型的 setter 方法转换做准备，万不可去掉
		// 经测试 JDBC 不会返回 java.util.Data 类型。java.sql.Date, java.sql.Time,java.sql.Timestamp 全部直接继承自 java.util.Data, 所以 getDate可以返回这三类数据
		if (type == java.util.Date.class) {
			if (s.length() > dateLen) {	// if (x < timeStampLen) 改用 datePattern 转换，更智能
				// Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]
				// return new java.util.Date(java.sql.Timestamp.valueOf(s).getTime());	// error under jdk 64bit(maybe)
				return new SimpleDateFormat(timeStampPattern).parse(s);
			}
			else {
				// return new java.util.Date(java.sql.Date.valueOf(s).getTime());	// error under jdk 64bit
				return new SimpleDateFormat(datePattern).parse(s);
			}
		}
		
		// mysql type: date, year
		if (type == java.sql.Date.class) {
			if (s.length() > dateLen) {	// if (x < timeStampLen) 改用 datePattern 转换，更智能
				// return new java.sql.Date(java.sql.Timestamp.valueOf(s).getTime());	// error under jdk 64bit(maybe)
				return new java.sql.Date(new SimpleDateFormat(timeStampPattern).parse(s).getTime());
			}
			else {
				// return new java.sql.Date(java.sql.Date.valueOf(s).getTime());	// error under jdk 64bit
				return new java.sql.Date(new SimpleDateFormat(datePattern).parse(s).getTime());
			}
		}
		
		// mysql type: time
		if (type == java.sql.Time.class) {
			return java.sql.Time.valueOf(s);
		}
		
		// mysql type: timestamp, datetime
		if (type == java.sql.Timestamp.class) {
			if (s.length() > dateLen) {
				return java.sql.Timestamp.valueOf(s);
			}
			else {
				return new java.sql.Timestamp(new SimpleDateFormat(datePattern).parse(s).getTime());
			}
		}
		
		// mysql type: real, double
		if (type == Double.class || type == double.class) {
			return Double.parseDouble(s);
		}
		
		// mysql type: float
		if (type == Float.class || type == float.class) {
			return Float.parseFloat(s);
		}
		
		// mysql type: bit, tinyint(1)
		if (type == Boolean.class || type == boolean.class) {
			String value = s.toLowerCase();
			if ("1".equals(value) || "true".equals(value)) {
				return Boolean.TRUE;
			}
			else if ("0".equals(value) || "false".equals(value)) {
				return Boolean.FALSE;
			}
			else {
				throw new RuntimeException("Can not parse to boolean type of value: " + s);
			}
		}
		
		// mysql type: decimal, numeric
		if (type == java.math.BigDecimal.class) {
			return new java.math.BigDecimal(s);
		}
		
		// mysql type: unsigned bigint
		if (type == java.math.BigInteger.class) {
			return new java.math.BigInteger(s);
		}
		
		// mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob. I have not finished the test.
		if (type == byte[].class) {
			return s.getBytes();
		}
		
		if (Config.getConstants().getDevMode()) {
			throw new RuntimeException("Please add code in " + TypeConverter.class  + ". The type can't be converted: " + type.getName());
		} else {
			throw new RuntimeException(type.getName() + " can not be converted, please use other type of attributes in your model!");
		}
	}
}

