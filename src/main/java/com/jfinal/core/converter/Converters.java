package com.jfinal.core.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Converters {
	private static final String timeStampPattern = "yyyy-MM-dd HH:mm:ss";
	private static final String datePattern = "yyyy-MM-dd";
	private static final int dateLen = datePattern.length();
	private static final int timeStampWithoutSecPatternLen = "yyyy-MM-dd HH:mm".length();
	private static final int timePatternLen = "hh:mm:ss".length();
	private static final int timeWithoutSecPatternLen = "hh:mm".length();
	
	private Converters(){}
	
	public static class IntegerConverter implements IConverter<Integer> {
		@Override
		public Integer convert(String s)  {
			return Integer.parseInt(s);
		}
	}
	
	public static class LongConverter implements IConverter<Long> {
		@Override
		public Long convert(String s) {
			return Long.parseLong(s);
		}
	}
	
	public static class FloatConverter implements IConverter<Float> {
		@Override
		public Float convert(String s) {
			return Float.parseFloat(s);
		}
	}
	
	public static class DoubleConverter implements IConverter<Double> {
		@Override
		public Double convert(String s) {
			return Double.parseDouble(s);
		}
	}
	
	public static class ByteConverter implements IConverter<byte[]> {
		@Override
		public byte[] convert(String s) {
			return s.getBytes();
		}
	}
	
	public static class BigIntegerConverter implements IConverter<java.math.BigInteger> {
		@Override
		public java.math.BigInteger convert(String s) {
			return new java.math.BigInteger(s);
		}
	}
	
	public static class BigDecimalConverter implements IConverter<java.math.BigDecimal> {
		@Override
		public java.math.BigDecimal convert(String s) {
			return new java.math.BigDecimal(s);
		}
	}
	
	public static class BooleanConverter implements IConverter<Boolean> {
		@Override
		public Boolean convert(String s) {
			String value = s.toLowerCase();
			if ("1".equals(value) || "true".equals(value) || "yes".equals(value)|| "on".equals(value)) {
				return Boolean.TRUE;
			}
			else if ("0".equals(value) || "false".equals(value) || "no".equals(value)|| "off".equals(value)) {
				return Boolean.FALSE;
			}
			else {
				throw new RuntimeException("Can not parse to boolean type of value: " + s);
			}
		}
	}
	
	public static class DateConverter implements IConverter<java.util.Date> {
		@Override
		public java.util.Date convert(String s) throws ParseException {
			if(timeStampWithoutSecPatternLen == s.length()){
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
		@Override
		public java.sql.Date convert(String s) throws ParseException {
			if(timeStampWithoutSecPatternLen == s.length()){
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
		@Override
		public java.sql.Time convert(String s) throws ParseException {
			int len = s.length();
			if(len == timeWithoutSecPatternLen){
				s = s + ":00";
			}
			if(len > timePatternLen){
				s = s.substring(0, timePatternLen);
			}
			return java.sql.Time.valueOf(s);
		}
	}
	
	public static class TimestampConverter implements IConverter<java.sql.Timestamp> {
		@Override
		public java.sql.Timestamp convert(String s) throws ParseException {
			if(timeStampWithoutSecPatternLen == s.length()){
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
