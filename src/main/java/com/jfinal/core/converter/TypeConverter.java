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
import java.util.HashMap;
import java.util.Map;
import com.jfinal.core.JFinal;
import com.jfinal.core.converter.Converters.BigDecimalConverter;
import com.jfinal.core.converter.Converters.BigIntegerConverter;
import com.jfinal.core.converter.Converters.BooleanConverter;
import com.jfinal.core.converter.Converters.ByteConverter;
import com.jfinal.core.converter.Converters.DateConverter;
import com.jfinal.core.converter.Converters.DoubleConverter;
import com.jfinal.core.converter.Converters.FloatConverter;
import com.jfinal.core.converter.Converters.IntegerConverter;
import com.jfinal.core.converter.Converters.LongConverter;
import com.jfinal.core.converter.Converters.SqlDateConverter;
import com.jfinal.core.converter.Converters.TimeConverter;
import com.jfinal.core.converter.Converters.TimestampConverter;

/**
 * TypeConverter 用于将客户端请求的 String 类型数据转换成指定的数据类型
 * 
 * 可以通过实现 IConverter 定制自己的类型转换规则:
 * 1：实现 IConverter 接口，如 MyDateConverter
 * 2：注册：TypeConverter.me().regist(Date.class, new MyDateConverter());
 * 
 * 注意：后注册的 Converter 会直接覆盖掉先注册的，因此可以通过覆盖的方式取代 jfinal
 *      官方的 Converter 实现类，达成定制化的需求
 * 
 * 
 * test for all types of mysql
 * 
 * 表单提交测试结果:
 * 1: 表单中的域,就算不输入任何内容,也会传过来 "", 也即永远不可能为 null.
 * 2: 如果输入空格也会提交上来
 * 3: 需要考 model中的 string属性,在传过来 "" 时是该转成 null还是不该转换,
 *    因为用户没有输入那么肯定是 null, 而不该是 ""
 * 4： 返回 null 还可以在数据表字段约束为不允许 null 值时，避免失误清除数据，
 *    "" 值对于数据表字段来说是合法的，容易失误清掉原有内容
 * 
 * 注意: 1:当type参数不为String.class, 且参数s为空串blank的情况,
 *       此情况下转换结果为 null, 而不应该抛出异常
 *      2:调用者需要对被转换数据做 null 判断，参见 Injector 的两处调用
 */
public class TypeConverter {
	
	private final Map<Class<?>, IConverter<?>> converterMap = new HashMap<Class<?>, IConverter<?>>();
	private static TypeConverter me = new TypeConverter();
	
	private TypeConverter() {
		regist(Integer.class, new IntegerConverter());
		regist(int.class, new IntegerConverter());
		regist(Long.class, new LongConverter());
		regist(long.class, new LongConverter());
		regist(Double.class, new DoubleConverter());
		regist(double.class, new DoubleConverter());
		regist(Float.class, new FloatConverter());
		regist(float.class, new FloatConverter());
		regist(Boolean.class, new BooleanConverter());
		regist(boolean.class, new BooleanConverter());
		regist(java.util.Date.class, new DateConverter());
		regist(java.sql.Date.class, new SqlDateConverter());
		regist(java.sql.Time.class, new TimeConverter());
		regist(java.sql.Timestamp.class, new TimestampConverter());
		regist(java.math.BigDecimal.class, new BigDecimalConverter());
		regist(java.math.BigInteger.class, new BigIntegerConverter());
		regist(byte[].class, new ByteConverter());
	}
	
	public static TypeConverter me() {
		return me;
	}
	
	public <T> void regist(Class<T> type, IConverter<T> converter) {
		converterMap.put(type, converter);
	}
	
	/**
	 * 将 String 数据转换为指定的类型
	 * @param type 需要转换成为的数据类型
	 * @param s 被转换的 String 类型数据，注意： s 参数不接受 null 值，否则会抛出异常
	 * @return 转换成功的数据
	 */
	public final Object convert(Class<?> type, String s) throws ParseException {
		// mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
		if (type == String.class) {
			return ("".equals(s) ? null : s);	// 用户在表单域中没有输入内容时将提交过来 "", 因为没有输入,所以要转成 null.
		}
		s = s.trim();
		if ("".equals(s)) {	// 前面的 String跳过以后,所有的空字符串全都转成 null,  这是合理的
			return null;
		}
		// 以上两种情况无需转换,直接返回, 注意, 本方法不接受null为 s 参数(经测试永远不可能传来null, 因为无输入传来的也是"")
		//String.class提前处理
		
		// --------
		IConverter<?> converter = converterMap.get(type);
		if (converter != null) {
			return converter.convert(s);
		}
		if (JFinal.me().getConstants().getDevMode()) {
			throw new RuntimeException("Please add code in " + TypeConverter.class  + ". The type can't be converted: " + type.getName());
		} else {
			throw new RuntimeException(type.getName() + " can not be converted, please use other type of attributes in your model!");
		}
	}
}







