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

package com.jfinal.template.stat;

/**
 * Compressor
 * 
 * 压缩规则：
 * 1：为追求性能极致只压缩模板中的静态文本内容，指令输出的内容不压缩，例如 #(blog.content) 输出的内容不会被压缩
 *   由于模板静态内容会被缓存，所以只需压缩一次，性能被最大化
 *    
 * 2：多个连续空格字符压缩为一个空格字符
 * 
 * 3：空格字符与 '\n' 的组合，压缩为一个 separator 字符。separator 为 Compressor 类中的属性值
 *    组合是指 0 个或多个空格字符与 1 个或多个 '\n' 字符组成的连续字符串
 *    组合不区分字符出现的次序，例如下面的四个字符串都满足该组合条件：
 *        "\n"    " \n "    "\n \n"    "\n \n  "
 * 
 * 注意事项：
 * 1：html 模板中存在 javascript 时分隔字符要配置为 '\n'，分符字符 ' ' 不支持 js 压缩
 * 
 * 2：由于多个连续的空格字符会被压缩为一个空格字符，所以当模板静态文本内容本身需要保持其多空格字符
 *    不被压缩为一个时，不能使用该压缩功能，例如：
 *      <input value="hello  ">
 *    上例中的字符串 "hello  " 中的两个空格会被压缩为一个空格
 */
public class Compressor {
	
	protected char separator = '\n';
	
	public Compressor() {}
	
	public Compressor(char separator) {
		if (separator > ' ') {
			throw new IllegalArgumentException("The parameter separator must be a separator character");
		}
		this.separator = separator;
	}
	
	public StringBuilder compress(StringBuilder content) {
		int len = content.length();
		StringBuilder ret = new StringBuilder(len);
		
		char ch;
		boolean hasLineFeed;
		int begin = 0;
		int forward = 0;
		
		while (forward < len) {
			// 扫描空白字符
			hasLineFeed = false;
			while (forward < len) {
				ch = content.charAt(forward);
				if (ch <= ' ') {			// 包含换行字符在内的空白字符
					if (ch == '\n') {		// 包含换行字符
						hasLineFeed = true;
					}
					forward++;
				} else {					// 非空白字符
					break ;
				}
			}
			
			// 压缩空白字符
			if (begin != forward) {
				if (hasLineFeed) {
					ret.append(separator);
				} else {
					ret.append(' ');
				}
			}
			
			// 复制非空白字符
			while (forward < len) {
				ch = content.charAt(forward);
				if (ch > ' ') {
					ret.append(ch);
					forward++;
				} else {
					break ;
				}
			}
			
			begin = forward;
		}
		
		return ret;
	}
}




