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

package com.jfinal.kit;

/**
 * StrKit.
 */
public class StrKit {
	
	/**
	 * 首字母变小写
	 */
	public static String firstCharToLowerCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'A' && firstChar <= 'Z') {
			char[] arr = str.toCharArray();
			arr[0] += ('a' - 'A');
			return new String(arr);
		}
		return str;
	}
	
	/**
	 * 首字母变大写
	 */
	public static String firstCharToUpperCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'a' && firstChar <= 'z') {
			char[] arr = str.toCharArray();
			arr[0] -= ('a' - 'A');
			return new String(arr);
		}
		return str;
	}
	
	/**
	 * 字符串为 null 或者内部字符全部为 ' ' '\t' '\n' '\r' 这四类字符时返回 true
	 */
	public static boolean isBlank(String str) {
		if (str == null) {
			return true;
		}
		
		for (int i = 0, len = str.length(); i < len; i++) {
			if (str.charAt(i) > ' ') {
				return false;
			}
		}
		return true;
	}
	
	public static boolean notBlank(String str) {
		return !isBlank(str);
	}
	
	public static boolean notBlank(String... strings) {
		if (strings == null || strings.length == 0) {
			return false;
		}
		for (String str : strings) {
			if (isBlank(str)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean hasBlank(String... strings) {
		if (strings == null || strings.length == 0) {
			return true;
		}
		
		for (String str : strings) {
			if (isBlank(str)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean notNull(Object... paras) {
		if (paras == null) {
			return false;
		}
		for (Object obj : paras) {
			if (obj == null) {
				return false;
			}
		}
		return true;
	}
	
	public static String defaultIfBlank(String str, String defaultValue) {
		return isBlank(str) ? defaultValue : str;
	}
	
	/**
	 * 将包含下划线字符 '_' 的字符串转换成驼峰格式，不包含下划线则原样返回
	 */
	public static String toCamelCase(String str) {
		return toCamelCase(str, false);
	}
	
	/**
	 * 字符串转换成驼峰格式
	 * 
	 * <pre>
	 * toLowerCaseAnyway 参数的作用如下：
	 * 
	 * 1：当待转换字符串中包含下划线字符 '_' 时，无需关心 toLowerCaseAnyway 参数的值，转换结果始终一样
	 * 
	 * 2：当待转换字符串中不包含下划线字符 '_' 时，toLowerCaseAnyway 参数规则如下：
	 *    true 值:  将待转换字符串全部转换成小与字母，适用于 oralce 数据库字段转换的场景
	 *              因为 oracle 字段全是大写字母
	 *                 
	 *    false 值: 则原样返回待转换字符串，适用于待转换字符串可能原本就是驼峰格式的场景
	 *              如果原本就是驼峰，全部转成小写字母显然不合理
	 * </pre>
	 */
	public static String toCamelCase(String str, boolean toLowerCaseAnyway) {
		int len = str.length();
		if (len <= 1) {
			return str;
		}
		
		char ch;
		int index = 0;
		char[] buf = new char[len];
		
		int i = 0;
		for (; i < len; i++) {
			ch = str.charAt(i);
			if (ch == '_') {
				// 当前字符为下划线时，将指针后移一位，将紧随下划线后面一个字符转成大写并存放
				i++;
				if (i < len) {
					ch = str.charAt(i);
					buf[index] = (
							index == 0 ?	// 首字母无条件变小写
							Character.toLowerCase(ch) :
							Character.toUpperCase(ch)
						);
					index++;
				}
			}
			else {
				buf[index++] = Character.toLowerCase(ch);
			}
		}
		
		if (toLowerCaseAnyway) {
			return new String(buf, 0, index);
		}
		
		// i == index 时，表明字符串中不存在字符 '_'
		// 无下划线的字符串原本可能就是驼峰形式，所以原样返回
		return i == index ? str : new String(buf, 0, index);
	}
	
	public static String join(String[] stringArray) {
		StringBuilder sb = new StringBuilder();
		for (String s : stringArray) {
			sb.append(s);
		}
		return sb.toString();
	}
	
	public static String join(String[] stringArray, String separator) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<stringArray.length; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(stringArray[i]);
		}
		return sb.toString();
	}
	
	public static String join(java.util.List<String> list, String separator) {
		StringBuilder sb = new StringBuilder();
		for (int i=0, len=list.size(); i<len; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(list.get(i));
		}
		return sb.toString();
	}
	
	public static boolean slowEquals(String a, String b) {
		byte[] aBytes = (a != null ? a.getBytes() : null);
		byte[] bBytes = (b != null ? b.getBytes() : null);
		return HashKit.slowEquals(aBytes, bBytes);
	}
	
	public static boolean equals(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}
	
	public static String getRandomUUID() {
		return java.util.UUID.randomUUID().toString().replace("-", "");
	}
}




