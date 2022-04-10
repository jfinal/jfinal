/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.json;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import com.jfinal.kit.StrKit;
import com.jfinal.kit.SyncWriteMap;
import com.jfinal.kit.TimeKit;
import com.jfinal.plugin.activerecord.CPI;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * JFinalJsonKit
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JFinalJsonKit {
	
	public static final JFinalJsonKit me = new JFinalJsonKit();
	
	// 缓存 ToJson 对象
	protected static SyncWriteMap<Class<?>, ToJson<?>> cache = new SyncWriteMap<>(512, 0.25F);
	
	// StringBuilder 最大缓冲区大小
	protected static int maxBufferSize = 1024 * 512;
	
	// 将 Model 当成 Bean 只对 getter 方法进行转换
	protected static boolean treatModelAsBean = false;
	
	// 是否跳过 null 值的字段，不对其进行转换
	protected static boolean skipNullValueField = false;
	
	// 对 Model 和 Record 的字段名进行转换的函数。例如转成驼峰形式对 oracle 支持更友好
	protected static Function<String, String> modelAndRecordFieldNameConverter = null;
	
	protected static Function<Object, ToJson<?>> toJsonFactory = null;
	
	public interface ToJson<T> {
		void toJson(T value, int depth, JsonResult ret);
	}
	
	public ToJson<?> getToJson(Object object) {
		ToJson<?> ret = cache.get(object.getClass());
		if (ret == null) {
			ret = createToJson(object);
			cache.putIfAbsent(object.getClass(), ret);
		}
		return ret;
	}
	
	/**
	 * 添加 ToJson 转换接口实现类，自由定制任意类型数据的转换规则
	 * <pre>
	 * 例子：
	 *     ToJson<Timestamp> toJson = (value, depth, ret) -> {
	 *       ret.addLong(value.getTime());
	 *     };
	 *     
	 *     JFinalJson.addToJson(Timestamp.class, toJson);
	 *     
	 *     以上代码为 Timestamp 类型的 json 转换定制了转换规则
	 *     将其转换成了 long 型数据
	 * </pre>
	 */
	public static void addToJson(Class<?> type, ToJson<?> toJson) {
		Objects.requireNonNull(type, "type can not be null");
		Objects.requireNonNull(toJson, "toJson can not be null");
		cache.put(type, toJson);
	}
	
	protected ToJson<?> createToJson(Object value) {
		// 优先使用 toJsonFactory 创建 ToJson 实例，方便用户优先接管 ToJson 转换器的创建
		if (toJsonFactory != null) {
			ToJson<?> tj = toJsonFactory.apply(value);
			if (tj != null) {
				return tj;
			}
		}
		
		// 基础类型 -----------------------------------------
		if (value instanceof String) {
			return new StrToJson();
		}
		
		if (value instanceof Number) {
			if (value instanceof Integer) {
				return new IntToJson();
			}
			if (value instanceof Long) {
				return new LongToJson();
			}
			if (value instanceof Double) {
				return new DoubleToJson();
			}
			if (value instanceof Float) {
				return new FloatToJson();
			}
			return new NumberToJson();
		}
		
		if (value instanceof Boolean) {
			return new BooleanToJson();
		}
		
		if (value instanceof Character) {
			return new CharacterToJson();
		}
		
		if (value instanceof Enum) {
			return new EnumToJson();
		}
		
		if (value instanceof java.util.Date) {
			if (value instanceof Timestamp) {
				return new TimestampToJson();
			}
			if (value instanceof Time) {
				return new TimeToJson();
			}
			return new DateToJson();
		}
		
		if (value instanceof Temporal) {
			if (value instanceof LocalDateTime) {
				return new LocalDateTimeToJson();
			}
			if (value instanceof LocalDate) {
				return new LocalDateToJson();
			}
			if (value instanceof LocalTime) {
				return new LocalTimeToJson();
			}
		}
		
		// 集合、Bean 类型，需要检测 depth ---------------------------------
		if (! treatModelAsBean) {
			if (value instanceof Model) {
				return new ModelToJson();
			}
		}
		
		if (value instanceof Record) {
			return new RecordToJson();
		}
		
		if (value instanceof Map) {
			return new MapToJson();
		}
		
		if (value instanceof Collection) {
			return new CollectionToJson();
		}
		
		if (value.getClass().isArray()) {
			return new ArrayToJson();
		}
		
		if (value instanceof Enumeration) {
			return new EnumerationToJson();
		}
		
		if (value instanceof Iterator) {
			return new IteratorToJson();
		}
		
		if (value instanceof Iterable) {
			return new IterableToJson();
		}
		
		BeanToJson beanToJson = buildBeanToJson(value);
		if (beanToJson != null) {
			return beanToJson;
		}
		
		return new UnknownToJson();
	}
	
	public static boolean checkDepth(int depth, JsonResult ret) {
		if (depth < 0) {
			ret.addNull();
			return true;
		} else {
			return false;
		}
	}
	
	static class StrToJson implements ToJson<String> {
		public void toJson(String str, int depth, JsonResult ret) {
			escape(str, ret.sb);
		}
	}
	
	static class CharacterToJson implements ToJson<Character> {
		public void toJson(Character ch, int depth, JsonResult ret) {
			escape(ch.toString(), ret.sb);
		}
	}
	
	static class IntToJson implements ToJson<Integer> {
		public void toJson(Integer value, int depth, JsonResult ret) {
			ret.addInt(value);
		}
	}
	
	static class LongToJson implements ToJson<Long> {
		public void toJson(Long value, int depth, JsonResult ret) {
			ret.addLong(value);
		}
	}
	
	static class DoubleToJson implements ToJson<Double> {
		public void toJson(Double value, int depth, JsonResult ret) {
			if (value.isInfinite() || value.isNaN()) {
				ret.addNull();
			} else {
				ret.addDouble(value);
			}
		}
	}
	
	static class FloatToJson implements ToJson<Float> {
		public void toJson(Float value, int depth, JsonResult ret) {
			if (value.isInfinite() || value.isNaN()) {
				ret.addNull();
			} else {
				ret.addFloat(value);
			}
		}
	}
	
	// 接管 int、long、double、float 之外的 Number 类型
	static class NumberToJson implements ToJson<Number> {
		public void toJson(Number value, int depth, JsonResult ret) {
			ret.addNumber(value);
		}
	}
	
	static class BooleanToJson implements ToJson<Boolean> {
		public void toJson(Boolean value, int depth, JsonResult ret) {
			ret.addBoolean(value);
		}
	}
	
	static class EnumToJson implements ToJson<Enum> {
		public void toJson(Enum en, int depth, JsonResult ret) {
			ret.addEnum(en);
		}
	}
	
	static class TimestampToJson implements ToJson<Timestamp> {
		public void toJson(Timestamp ts, int depth, JsonResult ret) {
			ret.addTimestamp(ts);
		}
	}
	
	static class TimeToJson implements ToJson<Time> {
		public void toJson(Time t, int depth, JsonResult ret) {
			ret.addTime(t);
		}
	}
	
	static class DateToJson implements ToJson<Date> {
		public void toJson(Date value, int depth, JsonResult ret) {
			ret.addDate(value);
		}
	}
	
	static class LocalDateTimeToJson implements ToJson<LocalDateTime> {
		public void toJson(LocalDateTime value, int depth, JsonResult ret) {
			ret.addLocalDateTime(value);
		}
	}
	
	static class LocalDateToJson implements ToJson<LocalDate> {
		public void toJson(LocalDate value, int depth, JsonResult ret) {
			ret.addLocalDate(value);
		}
	}
	
	static class LocalTimeToJson implements ToJson<LocalTime> {
		public void toJson(LocalTime value, int depth, JsonResult ret) {
			ret.addLocalTime(value);
		}
	}
	
	static class ModelToJson implements ToJson<Model> {
		public void toJson(Model model, int depth, JsonResult ret) {
			if (checkDepth(depth--, ret)) {
				return ;
			}
			
			Map<String, Object> attrs = CPI.getAttrs(model);
			modelAndRecordToJson(attrs, depth, ret);
		}
	}
	
	static class RecordToJson implements ToJson<Record> {
		public void toJson(Record record, int depth, JsonResult ret) {
			if (checkDepth(depth--, ret)) {
				return ;
			}
			
			Map<String, Object> columns = record.getColumns();
			modelAndRecordToJson(columns, depth, ret);
		}
	}
	
	public static void modelAndRecordToJson(Map<String, Object> map, int depth, JsonResult ret) {
		Iterator iter = map.entrySet().iterator();
		boolean first = true;
		ret.addChar('{');
		while (iter.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry)iter.next();
			Object value = entry.getValue();
			
			if (value == null && skipNullValueField) {
				continue ;
			}
			
			if (first) {
				first = false;
			} else {
				ret.addChar(',');
			}
			
			String fieldName = entry.getKey();
			if (modelAndRecordFieldNameConverter != null) {
				fieldName = modelAndRecordFieldNameConverter.apply(fieldName);
			}
			ret.addStrNoEscape(fieldName);
			
			ret.addChar(':');
			
			if (value != null) {
				ToJson tj = me.getToJson(value);
				tj.toJson(value, depth, ret);
			} else {
				ret.addNull();
			}
		}
		ret.addChar('}');
	}
	
	static class MapToJson implements ToJson<Map<?, ?>> {
		public void toJson(Map<?, ?> map, int depth, JsonResult ret) {
			if (checkDepth(depth--, ret)) {
				return ;
			}
			
			mapToJson(map, depth, ret);
		}
	}
	
	public static void mapToJson(Map<?, ?> map, int depth, JsonResult ret) {
		Iterator iter = map.entrySet().iterator();
		boolean first = true;
		ret.addChar('{');
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			Object value = entry.getValue();
			
			if (value == null && skipNullValueField) {
				continue ;
			}
			
			if (first) {
				first = false;
			} else {
				ret.addChar(',');
			}
			
			ret.addMapKey(entry.getKey());
			
			ret.addChar(':');
			
			if (value != null) {
				ToJson tj = me.getToJson(value);
				tj.toJson(value, depth, ret);
			} else {
				ret.addNull();
			}
		}
		ret.addChar('}');
	}
	
	static class CollectionToJson implements ToJson<Collection> {
		public void toJson(Collection c, int depth, JsonResult ret) {
			if (checkDepth(depth--, ret)) {
				return ;
			}
			
			iteratorToJson(c.iterator(), depth, ret);
		}
	}
	
	static class ArrayToJson implements ToJson<Object> {
		public void toJson(Object object, int depth, JsonResult ret) {
			if (checkDepth(depth--, ret)) {
				return ;
			}
			
			iteratorToJson(new ArrayIterator(object), depth, ret);
		}
	}
	
	static class ArrayIterator implements Iterator<Object> {
		private Object array;
		private int size;
		private int index;
		public ArrayIterator(Object array) {
			this.array = array;
			this.size = Array.getLength(array);
			this.index = 0;
		}
		public boolean hasNext() {
			return index < size;
		}
		public Object next() {
			return Array.get(array, index++);
		}
	}
	
	static class EnumerationToJson implements ToJson<Enumeration> {
		public void toJson(Enumeration en, int depth, JsonResult ret) {
			if (checkDepth(depth--, ret)) {
				return ;
			}
			
			ArrayList list = Collections.list(en);
			iteratorToJson(list.iterator(), depth, ret);
		}
	}
	
	static class IteratorToJson implements ToJson<Iterator> {
		public void toJson(Iterator it, int depth, JsonResult ret) {
			if (checkDepth(depth--, ret)) {
				return ;
			}
			
			iteratorToJson(it, depth, ret);
		}
	}
	
	public static void iteratorToJson(Iterator it, int depth, JsonResult ret) {
		boolean first = true;
		ret.addChar('[');
		while (it.hasNext()) {
			if (first) {
				first = false;
			} else {
				ret.addChar(',');
			}
			
			Object value = it.next();
			if (value != null) {
				ToJson tj = me.getToJson(value);
				tj.toJson(value, depth, ret);
			} else {
				ret.addNull();
			}
		}
		ret.addChar(']');
	}
	
	static class IterableToJson implements ToJson<Iterable> {
		public void toJson(Iterable iterable, int depth, JsonResult ret) {
			if (checkDepth(depth--, ret)) {
				return ;
			}
			
			iteratorToJson(iterable.iterator(), depth, ret);
		}
	}
	
	static class BeanToJson implements ToJson<Object> {
		private static final Object[] NULL_ARGS = new Object[0];
		private String[] fields;
		private Method[] methods;
		
		public BeanToJson(String[] fields, Method[] methods) {
			if (fields.length != methods.length) {
				throw new IllegalArgumentException("fields 与 methods 长度必须相同");
			}
			
			this.fields = fields;
			this.methods = methods;
		}
		
		public void toJson(Object bean, int depth, JsonResult ret) {
			if (checkDepth(depth--, ret)) {
				return ;
			}
			
			try {
				ret.addChar('{');
				boolean first = true;
				for (int i = 0; i < fields.length; i++) {
					Object value = methods[i].invoke(bean, NULL_ARGS);
					
					if (value == null && skipNullValueField) {
						continue ;
					}
					
					if (first) {
						first = false;
					} else {
						ret.addChar(',');
					}
					
					ret.addStrNoEscape(fields[i]);
					
					ret.addChar(':');
					
					if (value != null) {
						ToJson tj = me.getToJson(value);
						tj.toJson(value, depth, ret);
					} else {
						ret.addNull();
					}
				}
				ret.addChar('}');
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * 存在 getter/is 方法返回 BeanToJson，否则返回 null
	 */
	public static BeanToJson buildBeanToJson(Object bean) {
		List<String> fields = new ArrayList<>();
		List<Method> methods = new ArrayList<>();
		
		Method[] methodArray = bean.getClass().getMethods();
		for (Method m : methodArray) {
			if (m.getParameterCount() != 0 || m.getReturnType() == void.class) {
				continue ;
			}
			
			String methodName = m.getName();
			int indexOfGet = methodName.indexOf("get");
			if (indexOfGet == 0 && methodName.length() > 3) {	// Only getter
				String attrName = methodName.substring(3);
				if (!attrName.equals("Class")) {				// Ignore Object.getClass()
					fields.add(StrKit.firstCharToLowerCase(attrName));
					methods.add(m);
				}
			}
			else {
				int indexOfIs = methodName.indexOf("is");
				if (indexOfIs == 0 && methodName.length() > 2) {
					String attrName = methodName.substring(2);
					fields.add(StrKit.firstCharToLowerCase(attrName));
					methods.add(m);
				}
			}
		}
		
		int size = fields.size();
		if (size > 0) {
			return new BeanToJson(fields.toArray(new String[size]), methods.toArray(new Method[size]));
		} else {
			return null;
		}
	}
	
	static class UnknownToJson implements ToJson<Object> {
		public void toJson(Object object, int depth, JsonResult ret) {
			// 未知类型无法处理时当作字符串处理，否则 ajax 调用返回时 js 无法解析
			ret.addUnknown(object);
		}
	}
	
	/**
	 * JsonResult 用于存放 json 生成结果，结合 ThreadLocal 进行资源重用
	 */
	public static class JsonResult {
		
		/**
		 * 缓存 SimpleDateFormat
		 * 
		 * 备忘：请勿使用 TimeKit.getSimpleDateFormat(String) 优化这里，可减少一次
		 *      ThreadLocal.get() 调用
		 */
		Map<String, SimpleDateFormat> formats = new HashMap<>();
		
		// StringBuilder 内部对 int、long、double、float 数据写入有优化
		StringBuilder sb = new StringBuilder();
		
		String datePattern;
		String timestampPattern;
		boolean inUse = false;
		
		public void init(String datePattern, String timestampPattern) {
			this.datePattern = datePattern;
			this.timestampPattern = timestampPattern;
			inUse = true;
		}
		
		// 用来判断当前是否处于重入型转换状态，如果为 true，则要使用 new JsonResult()
		public boolean isInUse() {
			return inUse;
		}
		
		public void clear() {
			inUse = false;
			
			// 释放空间占用过大的缓存
			if (sb.length() > maxBufferSize) {
				sb = new StringBuilder(Math.max(1024, maxBufferSize / 2));
			} else {
				sb.setLength(0);
			}
		}
		
		public String toString() {
			return sb.toString();
		}
		
		public int length() {
			return sb.length();
		}
		
		public void addChar(char ch) {
			sb.append(ch);
		}
		
		public void addNull() {
			// sb.append((String)null);
			sb.append("null");
		}
		
		public void addStr(String str) {
			escape(str, sb);
		}
		
		public void addStrNoEscape(String str) {
			sb.append('\"').append(str).append('\"');
		}
		
		public void addInt(int i) {
			sb.append(i);
		}
		
		public void addLong(long l) {
			sb.append(l);
		}
		
		public void addDouble(double d) {
			sb.append(d);
		}
		
		public void addFloat(float f) {
			sb.append(f);
		}
		
		public void addNumber(Number n) {
			sb.append(n.toString());
		}
		
		public void addBoolean(boolean b) {
			sb.append(b);
		}
		
		public void addEnum(Enum en) {
			sb.append('\"').append(en.toString()).append('\"');
		}
		
		public String getDatePattern() {
			return datePattern;
		}
		
		public String getTimestampPattern() {
			return timestampPattern;
		}
		
		public SimpleDateFormat getFormat(String pattern) {
			SimpleDateFormat ret = formats.get(pattern);
			if (ret == null) {
				ret = new SimpleDateFormat(pattern);
				formats.put(pattern, ret);
			}
			return ret;
		}
		
		public void addTime(Time t) {
			sb.append('\"').append(t.toString()).append('\"');
		}
		
		public void addTimestamp(Timestamp ts) {
			if (timestampPattern != null) {
				sb.append('\"').append(getFormat(timestampPattern).format(ts)).append('\"');
			} else {
				sb.append(ts.getTime());
			}
		}
		
		public void addDate(Date d) {
			if (datePattern != null) {
				sb.append('\"').append(getFormat(datePattern).format(d)).append('\"');
			} else {
				sb.append(d.getTime());
			}
		}
		
		public void addLocalDateTime(LocalDateTime ldt) {
			if (datePattern != null) {
				sb.append('\"').append(TimeKit.format(ldt, datePattern)).append('\"');
			} else {
				sb.append(TimeKit.toDate(ldt).getTime());
			}
		}
		
		public void addLocalDate(LocalDate ld) {
			// LocalDate 的 pattern 不支持时分秒
			// 可通过 JFinalJson.addToJson(LocalDate.class, ...) 定制自己的转换 pattern
			String dp = "yyyy-MM-dd";
			sb.append('\"').append(TimeKit.format(ld, dp)).append('\"');
		}
		
		public void addLocalTime(LocalTime lt) {
			// LocalTime 的 pattern 不支持年月日，并且 LocalTime.toString() 的结果与 Time.toString() 格式不同
			// 可通过 JFinalJson.addToJson(LocalTime.class, ...) 定制自己的转换 pattern
			String tp = "HH:mm:ss";
			sb.append('\"').append(TimeKit.format(lt, tp)).append('\"');
		}
		
		public void addMapKey(Object value) {
			escape(String.valueOf(value), sb);
		}
		
		public void addUnknown(Object obj) {
			escape(obj.toString(), sb);
		}
	}
	
	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 */
	public static void escape(String s, StringBuilder sb) {
		sb.append('\"');
		
		for (int i = 0, len = s.length(); i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			//case '/':
			//	sb.append("\\/");
			//	break;
			default:
				if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
					String str = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < 4 - str.length(); k++) {
						sb.append('0');
					}
					sb.append(str.toUpperCase());
				}
				else {
					sb.append(ch);
				}
			}
		}
		
		sb.append('\"');
	}
	
	public static void setMaxBufferSize(int maxBufferSize) {
		int size = 1024 * 1;
		if (maxBufferSize < size) {
			throw new IllegalArgumentException("maxBufferSize can not less than " + size);
		}
		JFinalJsonKit.maxBufferSize = maxBufferSize;
	}
	
	/**
	 * 将 Model 当成 Bean 只对 getter 方法进行转换
	 * 
	 * 默认值为 false，将使用 Model 内的 Map attrs 属性进行转换，不对 getter 方法进行转换
	 * 优点是可以转换 sql 关联查询产生的动态字段，还可以转换 Model.put(...) 进来的数据
	 * 
	 * 配置为 true 时，将 Model 当成是传统的 java bean 对其 getter 方法进行转换，
	 * 使用生成器生成过 base model 的情况下才可以使用此配置
	 */
	public static void setTreatModelAsBean(boolean treatModelAsBean) {
		JFinalJsonKit.treatModelAsBean = treatModelAsBean;
	}
	
	/**
	 * 配置 Model、Record 字段名的转换函数
	 * 
	 * <pre>
	 * 例子：
	 *    JFinalJson.setModelAndRecordFieldNameConverter(fieldName -> {
	 *		   return StrKit.toCamelCase(fieldName, true);
	 *	  });
	 *  
	 *  以上例子中的方法 StrKit.toCamelCase(...) 的第二个参数可以控制大小写转化的细节
	 *  可以查看其方法上方注释中的说明了解详情
	 * </pre>
	 */
	public static void setModelAndRecordFieldNameConverter(Function<String, String>converter) {
		JFinalJsonKit.modelAndRecordFieldNameConverter = converter;
	}
	
	/**
	 * 配置将 Model、Record 字段名转换为驼峰格式
	 * 
	 * <pre>
	 * toLowerCaseAnyway 参数的含义：
	 * 1：true 值无条件将字段先转换成小写字母。适用于 oracle 这类字段名是大写字母的数据库
	 * 2：false 值只在出现下划线时将字段转换成小写字母。适用于 mysql 这类字段名是小写字母的数据库
	 * </pre>
	 */
	public static void setModelAndRecordFieldNameToCamelCase(boolean toLowerCaseAnyway) {
		modelAndRecordFieldNameConverter = (fieldName) -> {
			return StrKit.toCamelCase(fieldName, toLowerCaseAnyway);
		};
	}
	
	/**
	 * 配置将 Model、Record 字段名转换为驼峰格式
	 * 
	 * 先将字段名无条件转换成小写字母，然后再转成驼峰格式，适用于 oracle 这类字段名是大写字母的数据库
	 * 
	 * 如果是 mysql 数据库，建议使用: setModelAndRecordFieldNameToCamelCase(false);
	 */
	public static void setModelAndRecordFieldNameToCamelCase() {
		setModelAndRecordFieldNameToCamelCase(true);
	}
	
	public static void setToJsonFactory(Function<Object, ToJson<?>> toJsonFactory) {
		JFinalJsonKit.toJsonFactory = toJsonFactory;
	}
	
	public static void setSkipNullValueField(boolean skipNullValueField) {
		JFinalJsonKit.skipNullValueField = skipNullValueField;
	}
}




