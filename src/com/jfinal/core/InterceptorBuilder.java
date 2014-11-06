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
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.aop.ClearLayer;
import com.jfinal.aop.Interceptor;

/**
 * InterceptorBuilder
 */
class InterceptorBuilder {
	
	private static final Interceptor[] NULL_INTERCEPTOR_ARRAY = new Interceptor[0];
	
	@SuppressWarnings("unchecked")
	void addToInterceptorsMap(Interceptor[] defaultInters) {
		for (Interceptor inter : defaultInters)
			intersMap.put((Class<Interceptor>)inter.getClass(), inter);
	}
	
	/**
	 * Build interceptors of Controller
	 */
	Interceptor[] buildControllerInterceptors(Class<? extends Controller> controllerClass) {
		Before before = controllerClass.getAnnotation(Before.class);
		return before != null ? createInterceptors(before) : NULL_INTERCEPTOR_ARRAY;
	}
	
	/**
	 * Build interceptors of Method
	 */
	Interceptor[] buildMethodInterceptors(Method method) {
		Before before = method.getAnnotation(Before.class);
		return before != null ? createInterceptors(before) : NULL_INTERCEPTOR_ARRAY;
	}
	
	/**
	 * Build interceptors of Action
	 */
	Interceptor[] buildActionInterceptors(Interceptor[] defaultInters, Interceptor[] controllerInters, Class<? extends Controller> controllerClass, Interceptor[] methodInters, Method method) {
		ClearLayer controllerClearType = getControllerClearType(controllerClass);
		if (controllerClearType != null) {
			defaultInters = NULL_INTERCEPTOR_ARRAY;
		}
		
		ClearLayer methodClearType = getMethodClearType(method);
		if (methodClearType != null) {
			controllerInters = NULL_INTERCEPTOR_ARRAY;
			if (methodClearType == ClearLayer.ALL) {
				defaultInters = NULL_INTERCEPTOR_ARRAY;
			}
		}
		
		int size = defaultInters.length + controllerInters.length + methodInters.length;
		Interceptor[] result = (size == 0 ? NULL_INTERCEPTOR_ARRAY : new Interceptor[size]);
		
		int index = 0;
		for (int i=0; i<defaultInters.length; i++) {
			result[index++] = defaultInters[i];
		}
		for (int i=0; i<controllerInters.length; i++) {
			result[index++] = controllerInters[i];
		}
		for (int i=0; i<methodInters.length; i++) {
			result[index++] = methodInters[i];
		}
		
		return result;
	}
	
	private ClearLayer getMethodClearType(Method method) {
		ClearInterceptor clearInterceptor = method.getAnnotation(ClearInterceptor.class);
		return clearInterceptor != null ? clearInterceptor.value() : null ;
	}
	
	private ClearLayer getControllerClearType(Class<? extends Controller> controllerClass) {
		ClearInterceptor clearInterceptor = controllerClass.getAnnotation(ClearInterceptor.class);
		return clearInterceptor != null ? clearInterceptor.value() : null ;
	}
	
	private Map<Class<Interceptor>, Interceptor> intersMap = new HashMap<Class<Interceptor>, Interceptor>();
	
	/**
	 * Create interceptors with Annotation of Before. Singleton version.
	 */
	private Interceptor[] createInterceptors(Before beforeAnnotation) {
		Interceptor[] result = null;
		@SuppressWarnings("unchecked")
		Class<Interceptor>[] interceptorClasses = (Class<Interceptor>[]) beforeAnnotation.value();
		if (interceptorClasses != null && interceptorClasses.length > 0) {
			result = new Interceptor[interceptorClasses.length];
			for (int i=0; i<result.length; i++) {
				result[i] = intersMap.get(interceptorClasses[i]);
				if (result[i] != null)
					continue;
				
				try {
					result[i] = (Interceptor)interceptorClasses[i].newInstance();
					intersMap.put(interceptorClasses[i], result[i]);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return result;
	}
}




