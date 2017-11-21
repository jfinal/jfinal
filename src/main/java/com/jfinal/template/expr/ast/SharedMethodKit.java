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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.ReflectKit;

/**
 * SharedMethodKit
 */
public class SharedMethodKit {
	
	private static final Set<Long> excludedMethodKey = new HashSet<Long>();
	
	static {
		Method[] methods = Object.class.getMethods();
		for (Method method : methods) {
			Long key = getSharedMethodKey(method.getName(), method.getParameterTypes());
			excludedMethodKey.add(key);
		}
	}
	
	private final List<SharedMethodInfo> sharedMethodList = new ArrayList<SharedMethodInfo>();
	private final HashMap<Long, SharedMethodInfo> methodCache = new HashMap<Long, SharedMethodInfo>();
	
	public SharedMethodInfo getSharedMethodInfo(String methodName, Object[] argValues) {
		Class<?>[] argTypes = MethodKit.getArgTypes(argValues);
		Long key = getSharedMethodKey(methodName, argTypes);
		SharedMethodInfo method = methodCache.get(key);
		if (method == null) {
			method = doGetSharedMethodInfo(methodName, argTypes);
			if (method != null) {
				methodCache.put(key, method);
			}
			// shared method 不支持 null safe，不缓存: methodCache.put(key, Boolean.FALSE)
		}
		return method;
	}
	
	private SharedMethodInfo doGetSharedMethodInfo(String methodName, Class<?>[] argTypes) {
		for (SharedMethodInfo smi : sharedMethodList) {
			if (smi.getName().equals(methodName)) {
				Class<?>[] paraTypes = smi.getParameterTypes();
				if (MethodKit.matchFixedArgTypes(paraTypes, argTypes)) {	// 无条件优先匹配固定参数方法
					return smi;
				}
				if (smi.isVarArgs() && MethodKit.matchVarArgTypes(paraTypes, argTypes)) {
					return smi;
				}
			}
		}
		return null;
	}
	
	public void addSharedMethod(Object sharedMethodFromObject) {
		addSharedMethod(sharedMethodFromObject.getClass(), sharedMethodFromObject);
	}
	
	public void addSharedMethod(Class<?> sharedMethodFromClass) {
		addSharedMethod(sharedMethodFromClass, ReflectKit.newInstance(sharedMethodFromClass));
	}
	
	public void addSharedStaticMethod(Class<?> sharedStaticMethodFromClass) {
		addSharedMethod(sharedStaticMethodFromClass, null);
	}
	
	public void removeSharedMethod(String methodName) {
		Iterator<SharedMethodInfo> it = sharedMethodList.iterator();
		while(it.hasNext()) {
			if (it.next().getName().equals(methodName)) {
				it.remove();
			}
		}
	}
	
	public void removeSharedMethod(Class<?> sharedClass) {
		Iterator<SharedMethodInfo> it = sharedMethodList.iterator();
		while(it.hasNext()) {
			if (it.next().getClazz() == sharedClass) {
				it.remove();
			}
		}
	}
	
	public void removeSharedMethod(Method method) {
		Iterator<SharedMethodInfo> it = sharedMethodList.iterator();
		while(it.hasNext()) {
			SharedMethodInfo current = it.next();
			String methodName = method.getName();
			if (current.getName().equals(methodName)) {
				Long key = getSharedMethodKey(methodName, method.getParameterTypes());
				if (current.getKey().equals(key)) {
					it.remove();
				}
			}
		}
	}
	
	private synchronized void addSharedMethod(Class<?> sharedClass, Object target) {
		if (MethodKit.isForbiddenClass(sharedClass)) {
			throw new IllegalArgumentException("Forbidden class: " + sharedClass.getName());
		}
		
		Method[] methods = sharedClass.getMethods();
		for (Method method : methods) {
			Long key = getSharedMethodKey(method.getName(), method.getParameterTypes());
			if (excludedMethodKey.contains(key)) {
				continue ;
			}
			
			for (SharedMethodInfo smi : sharedMethodList) {
				if (smi.getKey().equals(key)) {
					throw new RuntimeException("The shared method is already exists : " + smi.toString());
				}
			}
			
			if (target != null) {
				sharedMethodList.add(new SharedMethodInfo(key, sharedClass, method, target));
			} else if (Modifier.isStatic(method.getModifiers())) { 	// target 为 null 时添加 static method
				sharedMethodList.add(new SharedMethodInfo(key, sharedClass, method, null));
			}
		}
	}
	
	private static Long getSharedMethodKey(String methodName, Class<?>[] argTypes) {
		long hash = HashKit.FNV_OFFSET_BASIS_64;
		hash ^= methodName.hashCode();
		hash *= HashKit.FNV_PRIME_64;
		
		if (argTypes != null) {
			for (int i=0; i<argTypes.length; i++) {
				Class<?> type = argTypes[i];
				if (type != null) {
					hash ^= type.getName().hashCode();
					hash *= HashKit.FNV_PRIME_64;
				}
			}
		}
		return hash;
	}
	
	static class SharedMethodInfo extends MethodInfo {
		final Object target;
		
		private SharedMethodInfo(Long key, Class<?> clazz, Method method, Object target) {
			super(key, clazz, method);
			this.target = target;
		}
		
		public Object invoke(Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			return super.invoke(target, args);
		}
		
		Class<?> getClazz() {
			return clazz;
		}
	}
}

