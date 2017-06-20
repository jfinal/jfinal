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

import com.jfinal.template.expr.ast.Logic;

/**
 * 针对 java.lang.Integer 的扩展方法
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
 * 
 * 用法：
 * #if(age.toInt() > 18)
 */
public class IntegerExt {
	
	public Boolean toBoolean(Integer self) {
		return Logic.isTrue(self);
	}
	
	public Integer toInt(Integer self) {
		return self;
	}
	
	public Long toLong(Integer self) {
		return self != null ? self.longValue() : null;
	}
	
	public Float toFloat(Integer self) {
		return self != null ? self.floatValue() : null;
	}
	
	public Double toDouble(Integer self) {
		return self != null ? self.doubleValue() : null;
	}
}



