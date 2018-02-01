/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com) / 玛雅牛 (myaniu AT gmail dot com).
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

package com.jfinal.core.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 针对 Integer、Long、Date 等类型实现 IConverter 接口
 */
public class Converters {
	
	private static final String timeStampPattern = "yyyy-MM-dd HH:mm:ss";
	private static final String datePattern = "yyyy-MM-dd";
	private static final int dateLen = datePattern.length();
	private static final int timeStampWithoutSecPatternLen = "yyyy-MM-dd HH:mm".length();
	private static final int timePatternLen = "hh:mm:ss".length();
	private static final int timeWithoutSecPatternLen = "hh:mm".length();
	
	private Converters() {}
	
	public static class IntegerConverter implements IConverter<Integer> {
		// mysql type: int, integer, tinyint(n) n > 1, smallint, mediumint
		@Override
		public Integer convert(String s) {
			return Integer.parseInt(s);
		}
	}
	
	// 支持需要保持 short 而非转成 int 的场景
	public static class ShortConverter implements IConverter<Short> {
		@Override
		public Short convert(String s) {
			return Short.parseShort(s);
		}
	}
	
	// 支持需要保持 byte 而非转成 int 的场景
	public static class ByteConverter implements IConverter<Byte> {
		@Override
		public Byte convert(String s) {
			return Byte.parseByte(s);
		}
	}
	
	public static class LongConverter implements IConverter<Long> {
		// mysql type: bigint
		@Override
		public Long convert(String s) {
			return Long.parseLong(s);
		}
	}
	
	public static class FloatConverter implements IConverter<Float> {
		// mysql type: float
		@Override
		public Float convert(String s) {
			return Float.parseFloat(s);
		}
	}
	
	public static class DoubleConverter implements IConverter<Double> {
		// mysql type: real, double
		@Override
		public Double convert(String s) {
			return Double.parseDouble(s);
		}
	}
	
	public static class ByteArrayConverter implements IConverter<byte[]> {
		// mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob. I have not finished the test.
		@Override
		public byte[] convert(String s) {
			return s.getBytes();
		}
	}
	
	public static class BigIntegerConverter implements IConverter<java.math.BigInteger> {
		// mysql type: unsigned bigint
		@Override
		public java.math.BigInteger convert(String s) {
			return new java.math.BigInteger(s);
		}
	}
	
	public static class BigDecimalConverter implements IConverter<java.math.BigDecimal> {
		// mysql type: decimal, numeric
		@Override
		public java.math.BigDecimal convert(String s) {
			return new java.math.BigDecimal(s);
		}
	}
	
	public static class BooleanConverter implements IConverter<Boolean> {
		// mysql type: bit, tinyint(1)
		@Override
		public Boolean convert(String s) {
			String value = s.toLowerCase();
			if ("true".equals(value) || "1".equals(value) /* || "yes".equals(value) || "on".equals(value) */) {
				return Boolean.TRUE;
			}
			else if ("false".equals(value) || "0".equals(value) /* || "no".equals(value) || "off".equals(value) */) {
				return Boolean.FALSE;
			}
			else {
				throw new RuntimeException("Can not parse to boolean type of value: " + s);
			}
		}
	}
	
	public static class DateConverter implements IConverter<java.util.Date> {
		// java.util.Date 类型专为传统 java bean 带有该类型的 setter 方法转换做准备，万不可去掉
		// 经测试 JDBC 不会返回 java.util.Data 类型。java.sql.Date, java.sql.Time,java.sql.Timestamp 全部直接继承自 java.util.Data, 所以 getDate可以返回这三类数据
		@Override
		public java.util.Date convert(String s) throws ParseException {
			if (timeStampWithoutSecPatternLen == s.length()) {
				s = s + ":00";
			}
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
	}
	
	public static class SqlDateConverter implements IConverter<java.sql.Date> {
		// mysql type: date, year
		@Override
		public java.sql.Date convert(String s) throws ParseException {
			if (timeStampWithoutSecPatternLen == s.length()) {
				s = s + ":00";
			}
			if (s.length() > dateLen) {	// if (x < timeStampLen) 改用 datePattern 转换，更智能
				// return new java.sql.Date(java.sql.Timestamp.valueOf(s).getTime());	// error under jdk 64bit(maybe)
				return new java.sql.Date(new SimpleDateFormat(timeStampPattern).parse(s).getTime());
			}
			else {
				// return new java.sql.Date(java.sql.Date.valueOf(s).getTime());	// error under jdk 64bit
				return new java.sql.Date(new SimpleDateFormat(datePattern).parse(s).getTime());
			}
		}
	}
	
	public static class TimeConverter implements IConverter<java.sql.Time> {
		// mysql type: time
		@Override
		public java.sql.Time convert(String s) throws ParseException {
			int len = s.length();
			if (len == timeWithoutSecPatternLen) {
				s = s + ":00";
			}
			if (len > timePatternLen) {
				s = s.substring(0, timePatternLen);
			}
			return java.sql.Time.valueOf(s);
		}
	}
	
	public static class TimestampConverter implements IConverter<java.sql.Timestamp> {
		// mysql type: timestamp, datetime
		@Override
		public java.sql.Timestamp convert(String s) throws ParseException {
			if (timeStampWithoutSecPatternLen == s.length()) {
				s = s + ":00";
			}
			if (s.length() > dateLen) {
				return java.sql.Timestamp.valueOf(s);
			}
			else {
				return new java.sql.Timestamp(new SimpleDateFormat(datePattern).parse(s).getTime());
			}
		}
	}
}
