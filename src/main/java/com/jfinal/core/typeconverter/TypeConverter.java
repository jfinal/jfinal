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
package com.jfinal.core.typeconverter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.jfinal.core.JFinal;

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
 *  用户可以实现自己的类型转换器，并进行注册。
 *  TypeConverter.me().regist(Integer.class, new IntegerConverter());
 */
public class TypeConverter {
	private final Map<Class<?>, IConverter<?>> typeMap = new HashMap<Class<?>, IConverter<?>>();
	private static TypeConverter me = new TypeConverter();
	private TypeConverter(){
		this.regist(Integer.class, new IntegerConverter());
		this.regist(int.class, new IntegerConverter());
		this.regist(Long.class, new LongConverter());
		this.regist(long.class, new LongConverter());
		this.regist(Double.class, new DoubleConverter());
		this.regist(double.class, new DoubleConverter());
		this.regist(Float.class, new FloatConverter());
		this.regist(float.class, new FloatConverter());
		this.regist(Boolean.class, new BooleanConverter());
		this.regist(boolean.class, new BooleanConverter());
		this.regist(java.util.Date.class, new DateConverter());
		this.regist(java.sql.Date.class, new SqlDateConverter());
		this.regist(java.sql.Time.class, new TimeConverter());
		this.regist(java.sql.Timestamp.class, new TimestampConverter());
		this.regist(java.math.BigDecimal.class, new BigDecimalConverter());
		this.regist(java.math.BigInteger.class, new BigIntegerConverter());
		this.regist(byte[].class, new ByteConverter());
	}
	public static TypeConverter me(){
		return TypeConverter.me;
	}
	
	public <T> void regist(Class<T> type, IConverter<T> converter){
		this.typeMap.put(type, converter);
	}
	
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
		//String.class提前处理。
		IConverter<?> converter = typeMap.get(type);
		if(converter != null){
			return converter.convert(s);
		}
		if (JFinal.me().getConstants().getDevMode()) {
			throw new RuntimeException("Please add code in " + TypeConverter.class  + ". The type can't be converted: " + type.getName());
		} else {
			throw new RuntimeException(type.getName() + " can not be converted, please use other type of attributes in your model!");
		}
	}
}
