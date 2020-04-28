/**
 * Copyright (c) 2011-2020, James Zhan 詹波 (jfinal@126.com).
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
 * 对 Text 节点进行压缩
 * 
 * 1：为追求性能极致，只压缩 Text 节点，所以压缩结果会存在一部分空白字符
 * 2：每次读取一行，按行进行压缩
 * 3：第一行左侧空白不压缩
 * 4：最后一行右侧空白不压缩（注意：最后一行以字符 '\n' 结尾时不算最后一行）
 * 5：第一行、最后一行以外的其它行左右侧都压缩
 * 6：文本之内的空白不压缩，例如字符串 "abc  def" 中的 "abc" 与 "def" 之间的空格不压缩
 * 7：压缩后内容的默认分隔字符为 '\n'，对 js 语句缺少分号的支持更友好。还可配置为空格 ' ' 等分隔字符
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
		StringBuilder result = new StringBuilder(len);
		
		int begin = 0;
		int forward = 0;
		int compressMode = 1;		// 1 表示第一行
		while (forward < len) {
			if (content.charAt(forward) == '\n') {
				compressLine(content, begin, forward - 1, compressMode, result);
				
				begin = forward + 1;
				forward = begin;
				compressMode = 2;	// 2 表示中间行
			} else {
				forward++;
			}
		}
		
		compressMode = 3;			// 3 表示最后一行
		compressLine(content, begin, forward - 1, compressMode, result);
		
		return result;
	}
	
	/**
	 * 按行压缩。只压缩文本前后的空白字符，文本内部的空白字符不压缩
	 * @param content 被处理行文本所在的 StringBuilder 对象
	 * @param start 被处理行文本的开始下标
	 * @param end 被处理行文本的结束下标（注意 end 下标所指向的字符被包含在处理的范围之内）
	 * @param compressMode 1 表示第一行，2 表示中间行，3 表示最后一行
	 * @param result 存放压缩结果
	 */
	protected void compressLine(StringBuilder content, int start, int end, int compressMode, StringBuilder result) {
		// 第一行不压缩左侧空白
		if (compressMode != 1) {
			while (start < end && content.charAt(start) <= ' ') {
				start++;
			}
		}
		
		// 最后一行不压缩右侧空白
		if (compressMode != 3) {
			while (end >= start && content.charAt(end) <= ' ') {
				end--;
			}
		}
		
		if (start <= end) {
			for (int i = start; i <= end; i++) {
				result.append(content.charAt(i));
			}
			
			// 最后一行右侧未压缩，不能添加分隔字符。最后一行以 '\n' 结尾时 compressMode 一定不为 3
			if (compressMode != 3) {
				result.append(separator);
			}
		}
	}
}




