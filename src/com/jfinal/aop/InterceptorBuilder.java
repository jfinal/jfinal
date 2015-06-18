/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * InterceptorBuilder
 */
public class InterceptorBuilder {
	
	public static final Interceptor[] NULL_INTERS = new Interceptor[0];
	
	private static Interceptor[] globalInters = NULL_INTERS;
	private static Map<Class<? extends Interceptor>, Interceptor> intersMap = new HashMap<Class<? extends Interceptor>, Interceptor>();
	
	/**
	 * Build Interceptors.
	 * <pre>
	 * Interceptors of action:  finalInters = globalInters + classInters + methodInters
	 * Interceptors of service: finalInters = globalInters + injectInters + classInters + methodInters
	 * </pre>
	 */
	public static Interceptor[] build(Interceptor[] injectInters, Class<?> targetClass, Method method) {
		Interceptor[] methodInters = createInterceptors(method.getAnnotation(Before.class));
		
		// no Clear annotation
		Clear clear = method.getAnnotation(Clear.class);
		if (clear == null) {
			Interceptor[] classInters = createInterceptors(targetClass.getAnnotation(Before.class));
			Interceptor[] result = new Interceptor[globalInters.length + injectInters.length + classInters.length + methodInters.length];
			int index = 0;
			for (Interceptor inter : globalInters)
				result[index++] = inter;
			for (Interceptor inter : injectInters)
				result[index++] = inter;
			for (Interceptor inter : classInters)
				result[index++] = inter;
			for (Interceptor inter : methodInters)
				result[index++] = inter;
			return result;
		}
		
		// Clear annotation without parameter
		Class<? extends Interceptor>[] clearInters = clear.value();
		if (clearInters.length == 0)
			return methodInters;
		
		// Clear annotation with parameter
		Interceptor[] classInters = createInterceptors(targetClass.getAnnotation(Before.class));
		Interceptor[] temp = new Interceptor[globalInters.length + injectInters.length + classInters.length];
		int index = 0;
		for (Interceptor inter : globalInters)
			temp[index++] = inter;
		for (Interceptor inter : injectInters)
			temp[index++] = inter;
		for (Interceptor inter : classInters)
			temp[index++] = inter;
		
		int removeCount = 0;
		for (int i=0; i<temp.length; i++) {
			for (Class<? extends Interceptor> ci : clearInters) {
				if (temp[i].getClass() == ci) {
					temp[i] = null;
					removeCount++;
					break;
				}
			}
		}
		
		Interceptor[] result = new Interceptor[temp.length + methodInters.length - removeCount];
		index = 0;
		for (Interceptor inter : temp)
			if (inter != null)
				result[index++] = inter;
		for (Interceptor inter : methodInters)
			result[index++] = inter;
		return result;
	}
	
	private static Interceptor[] createInterceptors(Before beforeAnnotation) {
		if (beforeAnnotation == null)
			return NULL_INTERS;
		
		Class<? extends Interceptor>[] interceptorClasses = beforeAnnotation.value();
		if (interceptorClasses.length == 0)
			return NULL_INTERS;
		
		Interceptor[] result = new Interceptor[interceptorClasses.length];
		try {
			for (int i=0; i<result.length; i++) {
				result[i] = intersMap.get(interceptorClasses[i]);
				if (result[i] == null) {
					result[i] = (Interceptor)interceptorClasses[i].newInstance();
					intersMap.put(interceptorClasses[i], result[i]);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	public static synchronized void addGlobalServiceInterceptor(Interceptor... inters) {
		if (inters == null)
			throw new IllegalArgumentException("interceptors can not be null.");
		
		for (Interceptor inter : inters)
			if (intersMap.containsKey(inter.getClass()))
				throw new IllegalArgumentException("interceptor already exists, interceptor must be singlton, do not create more then one instance of the same Interceptor Class.");
		for (Interceptor inter : inters)
			intersMap.put(inter.getClass(), inter);
		
		Interceptor[] temp = new Interceptor[globalInters.length + inters.length];
		System.arraycopy(globalInters, 0, temp, 0, globalInters.length);
		System.arraycopy(inters, 0, temp, globalInters.length, inters.length);
		globalInters = temp;
	}
}
