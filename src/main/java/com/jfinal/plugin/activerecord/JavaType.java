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

package com.jfinal.plugin.activerecord;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaType.
 * 
 * Java, JDBC and MySQL Types:
 * http://dev.mysql.com/doc/connector-j/en/connector-j-reference-type-conversions.html
 */
public class JavaType {
	
	@SuppressWarnings("serial")
	private Map<String, Class<?>> strToType = new HashMap<String, Class<?>>(32) {{
		
		// varchar, char, enum, set, text, tinytext, mediumtext, longtext
		put("java.lang.String", java.lang.String.class);
		
		// int, integer, tinyint, smallint, mediumint
		put("java.lang.Integer", java.lang.Integer.class);
		
		// bigint
		put("java.lang.Long", java.lang.Long.class);
		
		// java.util.Date can not be returned
		// java.sql.Date, java.sql.Time, java.sql.Timestamp all extends java.util.Date so getDate can return the three types data
		// put("java.util.Date", java.util.Date.class);
		
		// date, year
		put("java.sql.Date", java.sql.Date.class);
		
		// real, double
		put("java.lang.Double", java.lang.Double.class);
		
		// float
		put("java.lang.Float", java.lang.Float.class);
		
		// bit
		put("java.lang.Boolean", java.lang.Boolean.class);
		
		// time
		put("java.sql.Time", java.sql.Time.class);
		
		// timestamp, datetime
		put("java.sql.Timestamp", java.sql.Timestamp.class);
		
		// decimal, numeric
		put("java.math.BigDecimal", java.math.BigDecimal.class);
		
		// unsigned bigint
		put("java.math.BigInteger", java.math.BigInteger.class);
		
		// binary, varbinary, tinyblob, blob, mediumblob, longblob
		// qjd project: print_info.content varbinary(61800);
		put("[B", byte[].class);
		
		// 支持需要保持 short 与 byte 而非转成 int 的场景
		// 目前作用于Controller.getModel()/getBean()
		put("java.lang.Short", java.lang.Short.class);
		put("java.lang.Byte", java.lang.Byte.class);
	}};
	
	public Class<?> getType(String typeString) {
		return strToType.get(typeString);
	}
	
	public void addType(Class<?> type) {
		strToType.put(type.getName(), type);
	}

	public void removeType(Class<?> type) {
		strToType.remove(type.getName());
	}
	
	public void addTypeMapping(Class<?> from, Class<?> to) {
		strToType.put(from.getName(), to);
	}
	
	public void addTypeMapping(String from, Class<?> to) {
		strToType.put(from, to);
	}
}


