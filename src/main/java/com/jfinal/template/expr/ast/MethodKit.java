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

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.jfinal.kit.HashKit;

/**
 * MethodKit
 */
public class MethodKit {
	
	private static final Class<?>[] NULL_ARG_TYPES = new Class<?>[0];
	private static final Set<String> forbiddenMethods = new HashSet<String>();
	private static final Set<Class<?>> forbiddenClasses = new HashSet<Class<?>>();
	private static final Map<Class<?>, Class<?>> primitiveMap = new HashMap<Class<?>, Class<?>>();
	private static final ConcurrentHashMap<String, Object> methodCache = new ConcurrentHashMap<String, Object>();
	
	static {
		Class<?>[] cs = {
			System.class, Runtime.class, Thread.class, Class.class, ClassLoader.class, File.class
		};
		for (Class<?> c : cs) {
			forbiddenClasses.add(c);
		}
		
		String[] ms = {
			"getClass", "getDeclaringClass", "forName", "newInstance", "getClassLoader",
			"getMethod", "getMethods", "getField", "getFields",
			"notify", "notifyAll", "wait",
			"load", "exit", "loadLibrary", "halt",
			"stop", "suspend", "resume", "setDaemon", "setPriority",
		};
		for (String m : ms) {
			forbiddenMethods.add(m);
		}
		
		primitiveMap.put(byte.class, Byte.class);
		primitiveMap.put(short.class, Short.class);
		primitiveMap.put(int.class, Integer.class);
		primitiveMap.put(long.class, Long.class);
		primitiveMap.put(float.class, Float.class);
		primitiveMap.put(double.class, Double.class);
		primitiveMap.put(char.class, Character.class);
		primitiveMap.put(boolean.class, Boolean.class);
		
		primitiveMap.put(Byte.class, byte.class);
		primitiveMap.put(Short.class, short.class);
		primitiveMap.put(Integer.class, int.class);
		primitiveMap.put(Long.class, long.class);
		primitiveMap.put(Float.class, float.class);
		primitiveMap.put(Double.class, double.class);
		primitiveMap.put(Character.class, char.class);
		primitiveMap.put(Boolean.class, boolean.class);
	}
	
	public static boolean isForbiddenClass(Class<?> clazz) {
		return forbiddenClasses.contains(clazz);
	}
	
	public static boolean isForbiddenMethod(String methodName) {
		return forbiddenMethods.contains(methodName);
	}
	
	public static void addForbiddenMethod(String methodName) {
		forbiddenMethods.add(methodName);
	}
	
	public static MethodInfo getMethod(Class<?> targetClass, String methodName, Object[] argValues) {
		Class<?>[] argTypes = getArgTypes(argValues);
		String key = getMethodKey(targetClass, methodName, argTypes);
		Object method = methodCache.get(key);
		if (method == null) {
			method = doGetMethod(key, targetClass, methodName, argTypes);
			if (method != null) {
				methodCache.putIfAbsent(key, method);
			} else {
				// 对于不存在的 Method，只进行一次获取操作，主要为了支持 null safe，未来需要考虑内存泄漏风险
				methodCache.put(key, Boolean.FALSE);
			}
		}
		return method instanceof MethodInfo ? (MethodInfo)method : null;
	}
	
	/**
	 * 获取 getter 方法
	 * 使用与 Field 相同的 key，避免生成两次 key值
	 */
	public static MethodInfo getGetterMethod(String key, Class<?> targetClass, String methodName) {
		Object getterMethod = methodCache.get(key);
		if (getterMethod == null) {
			getterMethod = doGetMethod(key, targetClass, methodName, NULL_ARG_TYPES);
			if (getterMethod != null) {
				methodCache.putIfAbsent(key, getterMethod);
			} else {
				methodCache.put(key, Boolean.FALSE);
			}
		}
		return getterMethod instanceof MethodInfo ? (MethodInfo)getterMethod : null;
	}
	
	static Class<?>[] getArgTypes(Object[] argValues) {
		if (argValues == null || argValues.length == 0) {
			return NULL_ARG_TYPES;
		}
		Class<?>[] argTypes = new Class<?>[argValues.length];
		for (int i=0; i<argValues.length; i++) {
			argTypes[i] = argValues[i] != null ? argValues[i].getClass() : null;
		}
		return argTypes;
	}
	
	private static MethodInfo doGetMethod(String key, Class<?> targetClass, String methodName, Class<?>[] argTypes) {
		if (forbiddenClasses.contains(targetClass)) {
			throw new RuntimeException("Forbidden class: " + targetClass.getName());
		}
		if (forbiddenMethods.contains(methodName)) {
			throw new RuntimeException("Forbidden method: " + methodName);
		}
		
		Method[] methodArray = targetClass.getMethods();
		for (Method method : methodArray) {
			if (method.getName().equals(methodName)) {
				Class<?>[] paraTypes = method.getParameterTypes();
				if (matchFixedArgTypes(paraTypes, argTypes)) {	// 无条件优先匹配固定参数方法
					return new MethodInfo(key, targetClass, method);
				}
				if (method.isVarArgs() && matchVarArgTypes(paraTypes, argTypes)) {
					return new MethodInfo(key, targetClass, method);
				}
			}
		}
		return null;
	}
	
	static boolean matchFixedArgTypes(Class<?>[] paraTypes, Class<?>[] argTypes) {
		if (paraTypes.length != argTypes.length) {
			return false;
		}
		return matchRangeTypes(paraTypes, argTypes, paraTypes.length);
	}
	
	private static boolean matchRangeTypes(Class<?>[] paraTypes, Class<?>[] argTypes, int matchLength) {
		for (int i=0; i<matchLength; i++) {
			if (argTypes[i] == null) {
				if (paraTypes[i].isPrimitive()) {
					return false;
				}
				continue ;
			}
			if (paraTypes[i].isAssignableFrom(argTypes[i])) {
				continue ;
			}
			// object instanceof Xxx、Class.isAssignableFrom(Class)、Class.isInstance(Object) not works for primitive type
			if (paraTypes[i] == argTypes[i] || primitiveMap.get(paraTypes[i]) == argTypes[i]) {
				continue ;
			}
			return false;
		}
		return true;
	}
	
	static boolean matchVarArgTypes(Class<?>[] paraTypes, Class<?>[] argTypes) {
		int fixedParaLength = paraTypes.length - 1;
		if (argTypes.length < fixedParaLength) {
			return false;
		}
		if (!matchRangeTypes(paraTypes, argTypes, fixedParaLength)) {
			return false;
		}
		
		Class<?> varArgType = paraTypes[paraTypes.length - 1].getComponentType();
		for (int i=fixedParaLength; i<argTypes.length; i++) {
			if (argTypes[i] == null) {
				if (varArgType.isPrimitive()) {
					return false;
				}
				continue ;
			}
			if (varArgType.isAssignableFrom(argTypes[i])) {
				continue ;
			}
			if (varArgType == argTypes[i] || primitiveMap.get(varArgType) == argTypes[i]) {
				continue ;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * 获取方法用于缓存的 key
	 */
	private static String getMethodKey(Class<?> targetClass, String methodName, Class<?>[] argTypes) {
        StringBuilder key = new StringBuilder(96);
        key.append(targetClass.getName());
        key.append('.').append(methodName);
        if (argTypes != null && argTypes.length > 0) {
        	createArgTypesDigest(argTypes, key);
		}
        return key.toString();
    }
	
	static void createArgTypesDigest(Class<?>[] argTypes, StringBuilder key) {
		StringBuilder argTypesDigest = new StringBuilder(64);
		for (int i=0; i<argTypes.length; i++) {
            Class<?> type = argTypes[i];
            argTypesDigest.append(type != null ? type.getName() : "null");
        }
		key.append(HashKit.md5(argTypesDigest.toString()));
	}
}










