package com.jfinal.kit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;

/**
 * TypeKit
 */
public class TypeKit {
	
	private static final String datePattern = "yyyy-MM-dd";
	private static final int dateLen = datePattern.length();
	
	private static final String dateTimeWithoutSecondPattern = "yyyy-MM-dd HH:mm";
	private static final int dateTimeWithoutSecondLen = dateTimeWithoutSecondPattern.length();
	
	private static final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
	
	public static String toStr(Object s) {
		return s != null ? s.toString() : null;
	}
	
	public static Integer toInt(Object n) {
		if (n instanceof Integer) {
			return (Integer)n;
		} else if (n instanceof Number) {
			return ((Number)n).intValue();
		}
		// 支持 String 类型转换
		return n != null ? Integer.parseInt(n.toString()) : null;
	}
	
	public static Long toLong(Object n) {
		if (n instanceof Long) {
			return (Long)n;
		} else if (n instanceof Number) {
			return ((Number)n).longValue();
		}
		// 支持 String 类型转换
		return n != null ? Long.parseLong(n.toString()) : null;
	}
	
	public static Double toDouble(Object n) {
		if (n instanceof Double) {
			return (Double)n;
		} else if (n instanceof Number) {
			return ((Number)n).doubleValue();
		}
		// 支持 String 类型转换
		return n != null ? Double.parseDouble(n.toString()) : null;
	}
	
	public static BigDecimal toBigDecimal(Object n) {
		if (n instanceof BigDecimal) {
			return (BigDecimal)n;
		} else if (n != null) {
			return new BigDecimal(n.toString());
		} else {
			return null;
		}
	}
	
	public static Float toFloat(Object n) {
		if (n instanceof Float) {
			return (Float)n;
		} else if (n instanceof Number) {
			return ((Number)n).floatValue();
		}
		// 支持 String 类型转换
		return n != null ? Float.parseFloat(n.toString()) : null;
	}
	
	public static Short toShort(Object n) {
		if (n instanceof Short) {
			return (Short)n;
		} else if (n instanceof Number) {
			return ((Number)n).shortValue();
		}
		// 支持 String 类型转换
		return n != null ? Short.parseShort(n.toString()) : null;
	}
	
	public static Byte toByte(Object n) {
		if (n instanceof Byte) {
			return (Byte)n;
		} else if (n instanceof Number) {
			return ((Number)n).byteValue();
		}
		// 支持 String 类型转换
		return n != null ? Byte.parseByte(n.toString()) : null;
	}
	
	public static Boolean toBoolean(Object b) {
		if (b instanceof Boolean) {
			return (Boolean)b;
		} else if (b == null) {
			return null;
		}
		
		// 支持 String 类型转换，并且支持数字 1/0 与字符串 "1"/"0" 转换
		// 此处不能在判断 instanceof String 后强转 String，要支持 int、long 型的 1/0 值
		String s = b.toString();
		if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
			return Boolean.TRUE;
		} else if ("false".equalsIgnoreCase(s) || "0".equals(s)) {
			return Boolean.FALSE;
		}
		
		return (Boolean)b;
	}
	
	public static Number toNumber(Object n) {
		if (n instanceof Number) {
			return (Number)n;
		} else if (n == null) {
			return null;
		}
		
		// 支持 String 类型转换
		String s = n.toString();
		return s.indexOf('.') != -1 ? Double.parseDouble(s) : Long.parseLong(s);
	}
	
	public static java.util.Date toDate(Object d) {
		if (d instanceof java.util.Date) {
			return (java.util.Date)d;
		}
		
		if (d instanceof Temporal) {
			if (d instanceof LocalDateTime) {
				return TimeKit.toDate((LocalDateTime)d);
			}
			if (d instanceof LocalDate) {
				return TimeKit.toDate((LocalDate)d);
			}
			if (d instanceof LocalTime) {
				return TimeKit.toDate((LocalTime)d);
			}
		}
		
		if (d instanceof String) {
			String s = (String)d;
			if (s.length() <= dateLen) {
				return TimeKit.parse(s, datePattern);
			} else if (s.length() > dateTimeWithoutSecondLen) {
				return TimeKit.parse(s, dateTimePattern);
			} else {
				// 判断冒号字符是否出现两次，月、日、小时、分、秒都允许是一位数，例如：2022-1-2 3:4:5
				int index = s.indexOf(':');
				if (index != -1) {
					if (index != s.lastIndexOf(':')) {
						return TimeKit.parse(s, dateTimePattern);	
					} else {
						return TimeKit.parse(s, dateTimeWithoutSecondPattern);
					}
				}
			}
		}
		
		return (java.util.Date)d;
	}
	
	public static LocalDateTime toLocalDateTime(Object ldt) {
		if (ldt instanceof LocalDateTime) {
			return (LocalDateTime)ldt;
		}
		
		if (ldt instanceof LocalDate) {
			return ((LocalDate)ldt).atStartOfDay();
		}
		if (ldt instanceof LocalTime) {
			return LocalDateTime.of(LocalDate.now(), (LocalTime)ldt);
		}
		if (ldt instanceof java.util.Date) {
			return TimeKit.toLocalDateTime((java.util.Date)ldt);
		}
		
		if (ldt instanceof String) {
			String s = (String)ldt;
			if (s.length() <= dateLen) {
				return TimeKit.parseLocalDateTime(s, datePattern);
			} else if (s.length() > dateTimeWithoutSecondLen) {
				return TimeKit.parseLocalDateTime(s, dateTimePattern);
			} else {
				// 判断冒号字符是否出现两次，月、日、小时、分、秒都允许是一位数，例如：2022-1-2 3:4:5
				int index = s.indexOf(':');
				if (index != -1) {
					if (index != s.lastIndexOf(':')) {
						return TimeKit.parseLocalDateTime(s, dateTimePattern);
					} else {
						return TimeKit.parseLocalDateTime(s, dateTimeWithoutSecondPattern);
					}
				}
			}
		}
		
		return (LocalDateTime)ldt;
	}
}





