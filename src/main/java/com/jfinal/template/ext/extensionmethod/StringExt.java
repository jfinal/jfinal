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

/**
 * 最新认知，在对 keepPara() 这个场景进行处理时，由于表达式类型在
 * keepPara() 之前是 Integer 型，在 keepPara() 之后变成了 String 型
 * 所以表达式中的变量一会是 Integer 一会是 String，因此在 addMixedMethod
 * 时要针对 String Integer Long Float Double 等类型同时进行处理
 * 
 * 
 * ----------------------------------------------------------
 * 
 * 用法：
 * engine.addMixedMethod(Class targetClass, Class mixedClass)
 * 
 * 最终是调用的 engineConfig.methodKit.addMixedMethod(...)
 * 
 * 
 * 在 methodKit.addMixed(...) 时，要对里头所有方法的第一个参数进行类型判断
 * 
 * 例如在对 String 类进行 mixed 时，要判断第一个参数是否为 String
 * 
 * if (targetValue.getClass() instanceof mixedClass)
 *    // 判断成功
 * 以上的 mixedClass 是被 mixed 的 class，在本例中值为： String.class
 * 
 */
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
 *   以上用法，必须针对 String 与 Integer 同时扩展一个 toInt() 方法，模型表达式中
 * 的变在为 String 或为 Integer 时都存在 toInt() 方法可供调用
 * 
 * 用法：
 * #if(age.toInt() > 18)
 */
public class StringExt {
	
	/**
	 * toBoolean(...) 与 Logic.isTrue(...) 中的逻辑不同
	 * 后者针对 String，只要 length() > 0 即返回 true 值
	 */
	public Boolean toBoolean(String self) {
		if (self == null) {
			return Boolean.FALSE;
		}
		self = self.trim().toLowerCase();
		return self.equals("true") || self.equals("1");	// 未来考虑 "t"、"on"
	}
	
	// TODO 这里要测试当参数为被扩展的子类时的情况
	// 还要测试方法已经在 targetClass 已经存在的情况
	public Integer toInt(String self) {
		return self != null ? Integer.parseInt(self) : null;
	}
	
	public Long toLong(String self) {
		return self != null ? Long.parseLong(self) : null;
	}
	
	public Float toFloat(String self) {
		return self != null ? Float.parseFloat(self) : null;
	}
	
	public Double toDouble(String self) {
		return self != null ? Double.parseDouble(self) : null;
	}
}




