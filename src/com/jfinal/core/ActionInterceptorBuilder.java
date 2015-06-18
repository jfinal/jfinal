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

package com.jfinal.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import com.jfinal.aop.Before;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Clear;

/**
 * ActionInterceptorBuilder
 */
class ActionInterceptorBuilder {
	
	public static final Interceptor[] NULL_INTERS = new Interceptor[0];
	private Map<Class<? extends Interceptor>, Interceptor> intersMap = new HashMap<Class<? extends Interceptor>, Interceptor>();
	
	void addToInterceptorsMap(Interceptor[] globalInters) {
		for (Interceptor inter : globalInters)
			intersMap.put(inter.getClass(), inter);
	}
	
	/**
	 * Build Interceptors of Controller
	 */
	Interceptor[] buildControllerInterceptors(Class<? extends Controller> controllerClass) {
		return createInterceptors(controllerClass.getAnnotation(Before.class));
	}
	
	/**
	 * Build Interceptors of Method
	 */
	Interceptor[] buildMethodInterceptors(Method method) {
		return createInterceptors(method.getAnnotation(Before.class));
	}
	
	/**
	 * Build Interceptors of Action
	 * <pre>
	 * Interceptors of action : finalInters = globalInters + classInters + methodInters
	 * </pre>
	 */
	Interceptor[] buildActionInterceptors(Interceptor[] globalInters, Interceptor[] controllerInters, Interceptor[] methodInters, Method method) {
		// no Clear annotation
		Clear clear = method.getAnnotation(Clear.class);
		if (clear == null) {
			Interceptor[] result = new Interceptor[globalInters.length + controllerInters.length + methodInters.length];
			int index = 0;
			for (Interceptor inter : globalInters)
				result[index++] = inter;
			for (Interceptor inter : controllerInters)
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
		Interceptor[] temp = new Interceptor[globalInters.length + controllerInters.length];
		int index = 0;
		for (Interceptor inter : globalInters)
			temp[index++] = inter;
		for (Interceptor inter : controllerInters)
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
	
	/**
	 * Create Interceptors with Annotation of Before. Singleton version.
	 */
	private Interceptor[] createInterceptors(Before beforeAnnotation) {
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
}





