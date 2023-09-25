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

package com.jfinal.plugin.activerecord.generator;

import java.util.HashMap;
import java.util.Map;

/**
 * TypeMapping 建立起 ResultSetMetaData.getColumnClassName(i)到 java类型的映射关系
 * 特别注意时间型类型映射为了 java.util.Date（java.sql.Time 除外），可通过继承扩展该类来调整映射满足特殊需求
 * 
 * 与 com.jfinal.plugin.activerecord.JavaType.java 类型映射不同之处在于
 * 将时间型类型对应到 java.util.Date（java.sql.Time 除外）
 */
public class TypeMapping {
	
	@SuppressWarnings("serial")
	protected Map<String, String> map = new HashMap<String, String>(32) {{
		// java.util.Data can not be returned
		// java.sql.Date, java.sql.Time, java.sql.Timestamp all extends java.util.Data so getDate can return the three types data
		put("java.util.Date", "java.util.Date");
		
		// date, year
		put("java.sql.Date", "java.util.Date");
		
		// time
		// put("java.sql.Time", "java.util.Date");
		// 生成器需要生成 java.sql.Time 类型的 getter/setter 方法，以便 getBean 能正常工作
		put("java.sql.Time", "java.sql.Time");
		
		// timestamp, datetime
		put("java.sql.Timestamp", "java.util.Date");
		
		// binary, varbinary, tinyblob, blob, mediumblob, longblob
		// qjd project: print_info.content varbinary(61800);
		put("[B", "byte[]");
		
		// ---------
		
		// varchar, char, enum, set, text, tinytext, mediumtext, longtext
		put("java.lang.String", "java.lang.String");
		
		// int, integer, tinyint, smallint, mediumint
		put("java.lang.Integer", "java.lang.Integer");
		
		// bigint
		put("java.lang.Long", "java.lang.Long");
		
		// real, double
		put("java.lang.Double", "java.lang.Double");
		
		// float
		put("java.lang.Float", "java.lang.Float");
		
		// bit
		put("java.lang.Boolean", "java.lang.Boolean");
		
		// decimal, numeric
		put("java.math.BigDecimal", "java.math.BigDecimal");
		
		// unsigned bigint
		put("java.math.BigInteger", "java.math.BigInteger");
		
		// short
		put("java.lang.Short", "java.lang.Short");
		
		// byte
		put("java.lang.Byte", "java.lang.Byte");
		
		// 新增 java 8 的三种时间类型
		// put("java.time.LocalDateTime", "java.time.LocalDateTime");
		// put("java.time.LocalDate", "java.time.LocalDate");
		// put("java.time.LocalTime", "java.time.LocalTime");

		/**
		 * 部分同学反馈使用原始的 Date 更常用，故默认使用原始 Date
		 * 需要调整的通过可通过 Generator.addTypeMapping(...) 来覆盖默认映射
		 * 
		 * 也可以通过 removeMapping(...) 来清除默认映射，让 JDBC 自动处理映射关系
		 * 
		 * 注意：mysql 8 版本会将 datetime 字段类型映射为 LocalDateTime
		 */
		put("java.time.LocalDateTime", "java.util.Date");
		put("java.time.LocalDate", "java.util.Date");
		put("java.time.LocalTime", "java.sql.Time");
	}};
	
	public void addMapping(Class<?> from, Class<?> to) {
		map.put(from.getName(), to.getName());
	}
	
	public void addMapping(String from, String to) {
		map.put(from, to);
	}
	
	public void removeMapping(Class<?> from) {
		map.remove(from.getName());
	}
	
	public void removeMapping(String from) {
		map.remove(from);
	}
	
	public String getType(String typeString) {
		return map.get(typeString);
	}
}
