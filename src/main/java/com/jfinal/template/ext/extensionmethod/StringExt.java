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

package com.jfinal.template.ext.extensionmethod;

import com.jfinal.kit.StrKit;

/**
 * 针对 java.lang.String 的扩展方法
 * 
 * 重要用途：
 *     Controller.keepPara() 方法会将所有类型的数据当成 String 并传回到
 *     到模板中，所以模板中的如下代码将无法工作：
 *    	 #if(age > 18)
 *      	 ....
 *    	 #end
 *    
 *     以上代码，第一次渲染模板时，由于 age 为 int 类型，那么 if 语句中是正确的表达式，
 *     当提交表单后在后端调用 keepPara() 以后 age 变成了 String 类型，表达式错误，
 *     在有了扩展方法以后，解决办法如下：
 *       #if(age.toInt() > 18)
 *       	...
 *       #end
 *     如上所示，无论 age 是 String 还是 int 型，调用其 toInt() 方法将一直确保
 *     age 为 int 类型
 * 
 *   以上用法，必须针对 String 与 Integer 同时扩展一个 toInt() 方法，模板表达式中的
 *   变量为 String 或为 Integer 时都存在 toInt() 方法可供调用
 * 
 * 用法：
 * #if(age.toInt() > 18)
 */
public class StringExt {
	
	/**
	 * StringExt.toBoolean() 是数据类型转换，所以与 Logic.isTrue(String)
	 * 中的逻辑不同，后者只要 String 值非 null 并且 length() > 0 即返回 true
	 */
	public Boolean toBoolean(String self) {
		if (StrKit.isBlank(self)) {
			return null;	// return Boolean.FALSE;
		}
		
		String value = self.trim().toLowerCase();
		if ("true".equals(value) || "1".equals(value)) {	// 未来考虑 "yes"、"on"
			return Boolean.TRUE;
		} else if ("false".equals(value) || "0".equals(value)) {
			return Boolean.FALSE;
		} else {
			throw new RuntimeException("Can not parse to boolean type of value: \"" + self + "\"");
		}
	}
	
	public Integer toInt(String self) {
		return StrKit.isBlank(self) ? null : Integer.parseInt(self);
	}
	
	public Long toLong(String self) {
		return StrKit.isBlank(self) ? null : Long.parseLong(self);
	}
	
	public Float toFloat(String self) {
		return StrKit.isBlank(self) ? null : Float.parseFloat(self);
	}
	
	public Double toDouble(String self) {
		return StrKit.isBlank(self) ? null : Double.parseDouble(self);
	}
	
	public Short toShort(String self) {
		return StrKit.isBlank(self) ? null : Short.parseShort(self);
	}
	
	public Byte toByte(String self) {
		return StrKit.isBlank(self) ? null : Byte.parseByte(self);
	}
}




