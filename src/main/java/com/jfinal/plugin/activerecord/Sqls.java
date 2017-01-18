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

package com.jfinal.plugin.activerecord;

import java.util.concurrent.ConcurrentHashMap;
import com.jfinal.core.Const;
import com.jfinal.kit.Prop;

/**
 * 外部存取 sql 语句的工具类.<br>
 * 示例：<br>
 * Sqls.load("mySql.txt");<br>
 * String findBlogs = Sqls.get("findBlogs");<br>
 * Blog.dao.find(findBlogs);<br><br>
 * 
 * Sqls.load("otherSql.txt");<br>
 * String findUsers = Sqls.get("othersqls.txt", "findUser");<br>
 * User.dao.find(findUsers);<br>
 */
@Deprecated
public class Sqls {
	
	private static Prop prop = null;
	private static final ConcurrentHashMap<String, Prop> map = new ConcurrentHashMap<String, Prop>();
	
	private Sqls() {}
	
	/**
	 * 加载 sql 文件.
	 * 最先被加载的 sql 文件将成为默认 sql 文件，并能够被 Sqls.get(String) 使用到
	 * 第一次以后 load 后的 sql 文件会被 Sqls.get(String, String) 使用到
	 */
	public static void load(String sqlFileName) {
		use(sqlFileName);
	}
	
	public static String get(String sqlKey) {
		if (prop == null)
			throw new IllegalStateException("Init sql propties file by invoking Sqls.load(String fileName) method first.");
		return prop.get(sqlKey);
	}
	
	public static String get(String slqFileName, String sqlKey) {
		Prop prop = map.get(slqFileName);
		if (prop == null)
			throw new IllegalStateException("Init sql propties file by invoking Sqls.load(String fileName) method first.");
		return prop.get(sqlKey);
	}
	
	private static Prop use(String fileName) {
		return use(fileName, Const.DEFAULT_ENCODING);
	}
	
	private static Prop use(String fileName, String encoding) {
		Prop result = map.get(fileName);
		if (result == null) {
			result = new Prop(fileName, encoding);
			map.put(fileName, result);
			if (Sqls.prop == null)
				Sqls.prop = result;
		}
		return result;
	}
	
	public static Prop useless(String sqlFileName) {
		Prop previous = map.remove(sqlFileName);
		if (Sqls.prop == previous)
			Sqls.prop = null;
		return previous;
	}
	
	public static void clear() {
		prop = null;
		map.clear();
	}
}



