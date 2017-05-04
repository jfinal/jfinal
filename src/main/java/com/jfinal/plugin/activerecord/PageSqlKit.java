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

import java.util.LinkedList;

/**
 * PageSqlKit
 */
public class PageSqlKit {
	
	private static final int start = "select ".length();
	
	private static final char NULL = 0;
	private static final char SIZE = 128;
	private static char[] charTable = buildCharTable();
	
	private static char[] buildCharTable() {
		char[] ret = new char[SIZE];
		for (char i=0; i<SIZE; i++) {
			ret[i] = NULL;
		}
		
		ret['('] = '(';
		ret[')'] = ')';
		
		ret['f'] = 'f';
		ret['F'] = 'f';
		ret['r'] = 'r';
		ret['R'] = 'r';
		ret['o'] = 'o';
		ret['O'] = 'o';
		ret['m'] = 'm';
		ret['M'] = 'm';
		
		ret[' '] = ' ';
		ret['\r'] = ' ';
		ret['\n'] =  ' ';
		ret['\t'] =  ' ';
		return ret;
	}
	
	/**
	 * 未来考虑处理字符串常量中的字符：
	 * 1：select * from article where title = 'select * from'
	 *    此例可以正常处理，因为在第一个 from 之处就会正确返回
	 *  
	 * 2：select (select x from t where y = 'select * from ...') as a from article
	 *   此例无法正常处理，暂时交由 paginateByFullSql(...)
	 * 
	 * 3：如果一定要处理上例中的问题，还要了解不同数据库有关字符串常量的定界符细节
	 */
	private static int getIndexOfFrom(String sql) {
		LinkedList<String> stack = null;
		char c;
		for (int i = start, end = sql.length() - 5; i < end; i++) {
			c = charTable[sql.charAt(i)];
			if (c == NULL) {
				continue ;
			}
			
			if (c == '(') {
				if (stack == null) {
					stack = new LinkedList<String>();
				}
				stack.push("(");
				continue ;
			}
			
			if (c == ')') {
				if (stack == null) {
					throw new RuntimeException("Can not match left paren '(' for right paren ')': " + sql);
				}
				stack.pop();
				continue ;
			}
			if (stack != null && !stack.isEmpty()) {
				continue ;
			}
			
			if (c == 'f'
				&& charTable[sql.charAt(i + 1)] == 'r'
				&& charTable[sql.charAt(i + 2)] == 'o'
				&& charTable[sql.charAt(i + 3)] == 'm') {
				c = sql.charAt(i + 4);
				// 测试用例： "select count(*)from(select * from account limit 3) as t"
				if (charTable[c] == ' ' || c == '(') {		// 判断 from 后方字符
					c = sql.charAt(i - 1);
					if (charTable[c] == ' ' || c == ')') {	// 判断 from 前方字符
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	public static String[] parsePageSql(String sql) {
		int index = getIndexOfFrom(sql);
		if (index == -1) {
			return null;
		}
		
		String[] ret = new String[2];
		ret[0] = sql.substring(0, index);
		ret[1] = sql.substring(index);
		return ret;
	}
}



