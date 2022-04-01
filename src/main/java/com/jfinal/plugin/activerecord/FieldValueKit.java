package com.jfinal.plugin.activerecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import com.jfinal.kit.TimeKit;

/**
 * FieldTypeKit
 */
class FieldValueKit {
	
	static Integer toInt(Object n) {
		if (n instanceof Integer) {
			return (Integer)n;
		} else if (n instanceof Number) {
			return ((Number)n).intValue();
		}
		// 支持 String 类型转换
		return n != null ? Integer.parseInt(n.toString()) : null;
	}
	
	static Long toLong(Object n) {
		if (n instanceof Long) {
			return (Long)n;
		} else if (n instanceof Number) {
			return ((Number)n).longValue();
		}
		// 支持 String 类型转换
		return n != null ? Long.parseLong(n.toString()) : null;
	}
	
	static Double toDouble(Object n) {
		if (n instanceof Double) {
			return (Double)n;
		} else if (n instanceof Number) {
			return ((Number)n).doubleValue();
		}
		// 支持 String 类型转换
		return n != null ? Double.parseDouble(n.toString()) : null;
	}
	
	static BigDecimal toBigDecimal(Object n) {
		if (n instanceof BigDecimal) {
			return (BigDecimal)n;
		} else if (n != null) {
			return new BigDecimal(n.toString());
		} else {
			return null;
		}
	}
	
	static Float toFloat(Object n) {
		if (n instanceof Float) {
			return (Float)n;
		} else if (n instanceof Number) {
			return ((Number)n).floatValue();
		}
		// 支持 String 类型转换
		return n != null ? Float.parseFloat(n.toString()) : null;
	}
	
	static Short toShort(Object n) {
		if (n instanceof Short) {
			return (Short)n;
		} else if (n instanceof Number) {
			return ((Number)n).shortValue();
		}
		// 支持 String 类型转换
		return n != null ? Short.parseShort(n.toString()) : null;
	}
	
	static Byte toByte(Object n) {
		if (n instanceof Byte) {
			return (Byte)n;
		} else if (n instanceof Number) {
			return ((Number)n).byteValue();
		}
		// 支持 String 类型转换
		return n != null ? Byte.parseByte(n.toString()) : null;
	}
	
	static Boolean toBoolean(Object b) {
		if (b instanceof Boolean) {
			return (Boolean)b;
		} else if (b == null) {
			return null;
		}
		
		// 支持 String 类型转换，并且支持数字 1/0 与字符串 "1"/"0" 转换
		String s = b.toString();
		if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
			return Boolean.TRUE;
		} else if ("false".equalsIgnoreCase(s) || "0".equals(s)) {
			return Boolean.FALSE;
		}
		
		throw new ClassCastException("无法转换为 Boolean 值，类型 : " + b.getClass() + " 值 : " + b);
	}
	
	static Number toNumber(Object n) {
		if (n instanceof Number) {
			return (Number)n;
		} else if (n == null) {
			return null;
		}
		
		// 支持 String 类型转换
		String s = n.toString();
		return s.indexOf('.') != -1 ? Double.parseDouble(s) : Long.parseLong(s);
	}
	
	static java.util.Date toDate(Object d) {
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
		
		return (java.util.Date)d;
	}
	
	static LocalDateTime toLocalDateTime(Object ldt) {
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
		
		return (LocalDateTime)ldt;
	}
}





