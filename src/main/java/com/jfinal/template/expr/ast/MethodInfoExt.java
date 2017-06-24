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

package com.jfinal.template.expr.ast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * MethodInfoExt 辅助实现 extension method 功能
 */
public class MethodInfoExt extends MethodInfo {
	
	protected Object objectOfExtensionClass;
	
	public MethodInfoExt(Object objectOfExtensionClass, String key, Class<?> clazz, Method method) {
		super(key, clazz, method);
		this.objectOfExtensionClass = objectOfExtensionClass;
		
		// 将被 mixed 的类自身添加入参数类型数组的第一个位置
		// Class<?>[] newParaTypes = new Class<?>[paraTypes.length + 1];
		// newParaTypes[0] = clazz;	// 第一个参数就是被 mixed 的类它自己
		// System.arraycopy(paraTypes, 0, newParaTypes, 1, paraTypes.length);
		// this.paraTypes = newParaTypes;
	}
	
	public Object invoke(Object target, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object[] finalArgs = new Object[args.length + 1];
		finalArgs[0] = target;
		
		if (args.length > 0) {
			System.arraycopy(args, 0, finalArgs, 1, args.length);
		}
		
		if (isVarArgs) {
			return invokeVarArgsMethod(objectOfExtensionClass, finalArgs);
		} else {
			return method.invoke(objectOfExtensionClass, finalArgs);
		}
	}
}







