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
 * LineCompressor 按行压缩
 * 
 * 每次压缩一行，对 Text 节点进行压缩
 * 
 * 1：为追求性能极致，只压缩 Text 节点，所以压缩结果会存在一部分空白字符
 * 2：每次读取一行，按行进行压缩
 * 3：第一行左侧空白不压缩
 * 4：最后一行右侧空白不压缩（注意：最后一行以字符 '\n' 结尾时不算最后一行）
 * 5：第一行、最后一行以外的其它行左右侧都压缩
 * 6：文本之内的空白不压缩，例如字符串 "abc  def" 中的 "abc" 与 "def" 之间的空格不压缩
 * 7：压缩分隔符默认配置为 '\n'，还可配置为 ' '。如果模板中含有 javascript 脚本，需配置为 '\n'
 * 8：可通过 Engine.setCompressor(Compressor) 来定制自己的实现类
 *    可使用第三方的压缩框架来定制，例如使用 google 的压缩框架:
 *      压缩 html: com.googlecode.htmlcompressor:htmlcompressor
 *      压缩 javascript: com.google.javascript:closure-compiler
 */
public class LineCompressor extends Compressor {
	
	public LineCompressor() {}
	
	public LineCompressor(char separator) {
		if (separator > ' ') {
			throw new IllegalArgumentException("The parameter separator must be a separator character");
		}
		this.separator = separator;
	}
	
	public StringBuilder compress(StringBuilder content) {
		int len = content.length();
		
		// 仅包含一个字符 '\n'，需单独处理，否则会返回空字符串 ""
		// 测试用例: "#date()\n#date()" "#(1)\n#(2)"
		if (len == 1) {
			// 换行字符 '\n' 替换为 separator。空格除外的空白字符替换为 ' ' 压缩效果更好，例如 '\t'
			if (content.charAt(0) == '\n') {
				content.setCharAt(0, separator);
			} else if (content.charAt(0) < ' ') {
				content.setCharAt(0, ' ');
			}
			return content;
		}
		
		int begin = 0;
		int forward = 0;
		int lineType = 1;		// 1 表示第一行
		StringBuilder result = null;
		while (forward < len) {
			if (content.charAt(forward) == '\n') {
				if (result == null) {
					result = new StringBuilder(len);		// 延迟创建
				}
				compressLine(content, begin, forward - 1, lineType, result);
				
				begin = forward + 1;
				forward = begin;
				lineType = 2;	// 2 表示中间行
			} else {
				forward++;
			}
		}
		
		if (lineType == 1) {	// 此时为 1，表示既是第一行也是最后一行
			return content;
		}
		
		lineType = 3;			// 3 表示最后一行
		compressLine(content, begin, forward - 1, lineType, result);
		
		return result;
	}
	
	/**
	 * 按行压缩
	 * 
	 * 只压缩文本前后的空白字符，文本内部的空白字符不压缩，极大简化压缩算法、提升压缩效率，并且压缩结果也不错
	 * 
	 * @param content 被处理行文本所在的 StringBuilder 对象
	 * @param start 被处理行文本的开始下标
	 * @param end 被处理行文本的结束下标（注意 end 下标所指向的字符被包含在处理的范围之内）
	 * @param lineType 1 表示第一行，2 表示中间行，3 表示最后一行
	 * @param result 存放压缩结果
	 */
	protected void compressLine(StringBuilder content, int start, int end, int lineType, StringBuilder result) {
		// 第一行不压缩左侧空白
		if (lineType != 1) {
			while (start <= end && content.charAt(start) <= ' ') {
				start++;
			}
		}
		
		// 最后一行不压缩右侧空白
		if (lineType != 3) {
			while (end >= start && content.charAt(end) <= ' ') {
				end--;
			}
		}
		
		// 空白行可出现 start 大于 end 的情况
		if (start <= end) {
			for (int i = start; i <= end; i++) {
				result.append(content.charAt(i));
			}
			
			// 最后一行右侧未压缩，不能添加分隔字符。最后一行以 '\n' 结尾时 lineType 一定不为 3
			if (lineType != 3) {
				result.append(separator);
			}
		}
		// 空白行，且是第一行，需要添加分隔字符，否则会被压缩去除掉该空行
		// 测试用例："id=#(123)\nand"    "id=#(123)   \nand"
		else {
			if (lineType == 1) {
				// 第一行不压缩左侧空白的规则，是针对其为 "非空白行"，所以此处没有原样保留空白字符
				// 最后一行不压缩右侧空白的规则，也是针对其为 "非空白行"
				result.append(separator);
			}
		}
	}
}




