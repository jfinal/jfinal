/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.kit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.jfinal.core.Const;

/**
 * PropKit. PropKit can load properties file from CLASSPATH or File object.
 */
public class PropKit {
	
	private static Prop prop = null;
	private static final Map<String, Prop> map = new HashMap<String, Prop>();
	
	private PropKit() {}
	
	/**
	 * Using the properties file. It will loading the properties file if not loading.
	 * @see #use(String, String)
	 */
	public static Prop use(String fileName) {
		return use(fileName, Const.DEFAULT_ENCODING);
	}
	
	/**
	 * Using the properties file. It will loading the properties file if not loading.
	 * <p>
	 * Example:<br>
	 * PropKit.use("config.txt", "UTF-8");<br>
	 * PropKit.use("other_config.txt", "UTF-8");<br><br>
	 * String userName = PropKit.get("userName");<br>
	 * String password = PropKit.get("password");<br><br>
	 * 
	 * userName = PropKit.use("other_config.txt").get("userName");<br>
	 * password = PropKit.use("other_config.txt").get("password");<br><br>
	 * 
	 * PropKit.use("com/jfinal/config_in_sub_directory_of_classpath.txt");
	 * 
	 * @param fileName the properties file's name in classpath or the sub directory of classpath
	 * @param encoding the encoding
	 */
	public static Prop use(String fileName, String encoding) {
		Prop result = map.get(fileName);
		if (result == null) {
			synchronized (map) {
				result = map.get(fileName);
				if (result == null) {
					result = new Prop(fileName, encoding);
					map.put(fileName, result);
					if (PropKit.prop == null)
						PropKit.prop = result;
				}
			}
		}
		return result;
	}
	
	/**
	 * Using the properties file bye File object. It will loading the properties file if not loading.
	 * @see #use(File, String)
	 */
	public static Prop use(File file) {
		return use(file, Const.DEFAULT_ENCODING);
	}
	
	/**
	 * Using the properties file bye File object. It will loading the properties file if not loading.
	 * <p>
	 * Example:<br>
	 * PropKit.use(new File("/var/config/my_config.txt"), "UTF-8");<br>
	 * Strig userName = PropKit.use("my_config.txt").get("userName");
	 * 
	 * @param file the properties File object
	 * @param encoding the encoding
	 */
	public static Prop use(File file, String encoding) {
		Prop result = map.get(file.getName());
		if (result == null) {
			synchronized (map) {
				result = map.get(file.getName());
				if (result == null) {
					result = new Prop(file, encoding);
					map.put(file.getName(), result);
					if (PropKit.prop == null)
						PropKit.prop = result;
				}
			}
		}
		return result;
	}
	
	public static Prop useless(String fileName) {
		Prop previous = map.remove(fileName);
		if (PropKit.prop == previous)
			PropKit.prop = null;
		return previous;
	}
	
	public static void clear() {
		prop = null;
		map.clear();
	}
	
	public static Prop getProp() {
		if (prop == null)
			throw new IllegalStateException("Load propties file by invoking PropKit.use(String fileName) method first.");
		return prop;
	}
	
	public static Prop getProp(String fileName) {
		return map.get(fileName);
	}
	
	public static String get(String key) {
		return getProp().get(key);
	}
	
	public static String get(String key, String defaultValue) {
		return getProp().get(key, defaultValue);
	}
	
	public static Integer getInt(String key) {
		return getProp().getInt(key);
	}
	
	public static Integer getInt(String key, Integer defaultValue) {
		return getProp().getInt(key, defaultValue);
	}
	
	public static Long getLong(String key) {
		return getProp().getLong(key);
	}
	
	public static Long getLong(String key, Long defaultValue) {
		return getProp().getLong(key, defaultValue);
	}
	
	public static Boolean getBoolean(String key) {
		return getProp().getBoolean(key);
	}
	
	public static Boolean getBoolean(String key, Boolean defaultValue) {
		return getProp().getBoolean(key, defaultValue);
	}
	
	public static boolean containsKey(String key) {
		return getProp().containsKey(key);
	}
}


