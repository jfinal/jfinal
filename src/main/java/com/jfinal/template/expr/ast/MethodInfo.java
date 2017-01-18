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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * MethodInfo
 */
public class MethodInfo {
	
	protected final String key;
	protected final Class<?> clazz;
	protected final Method method;
	
	protected final boolean isVarArgs;
	protected final Class<?>[] paraTypes;
	
	public MethodInfo(String key, Class<?> clazz, Method method) {
		this.key = key;
		this.clazz = clazz;
		this.method = method;
		this.isVarArgs = method.isVarArgs();
		this.paraTypes = method.getParameterTypes();
	}
	
	public Object invoke(Object target, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (isVarArgs) {
			return invokeVarArgsMethod(target, args);
		} else {
			return method.invoke(target, args);
		}
	}
	
	private Object invokeVarArgsMethod(Object target, Object[] argValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object[] finalArgValues = new Object[paraTypes.length];
		
		int fixedParaLength = paraTypes.length - 1;
		System.arraycopy(argValues, 0, finalArgValues, 0, fixedParaLength);
		Class<?> varParaComponentType = paraTypes[paraTypes.length - 1].getComponentType();
		Object varParaValues = Array.newInstance(varParaComponentType, argValues.length - fixedParaLength);
		int p = 0;
		for (int i=fixedParaLength; i<argValues.length; i++) {
			Array.set(varParaValues, p++, argValues[i]);
		}
		finalArgValues[paraTypes.length - 1] = varParaValues;
		return method.invoke(target, finalArgValues);
	}
	
	public String getKey() {
		return key;
	}
	
	public String getName() {
		return method.getName();
	}
	
	public boolean isStatic() {
		return Modifier.isStatic(method.getModifiers());
	}
	
	public boolean isVarArgs() {
		return isVarArgs;
	}
	
	protected Class<?>[] getParameterTypes() {
		return paraTypes;
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder(clazz.getName()).append(".").append(method.getName()).append("(");
		for (int i=0; i<paraTypes.length; i++) {
			if (i > 0) {
				ret.append(", ");
			}
			ret.append(paraTypes[i].getName());
		}
		return ret.append(")").toString();
	}
}


