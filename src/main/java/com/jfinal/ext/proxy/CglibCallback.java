/**
 * Copyright (c) 2011-2021, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.ext.proxy;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.InterceptorManager;
import com.jfinal.aop.Invocation;
import com.jfinal.ext.proxy.CglibProxyFactory.IntersCache;
import com.jfinal.ext.proxy.CglibProxyFactory.MethodKey;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * CglibCallback.
 */
class CglibCallback implements MethodInterceptor {
	
	private static final Set<String> excludedMethodName = buildExcludedMethodName();
	private static final InterceptorManager interMan = InterceptorManager.me();
	
	public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		if (excludedMethodName.contains(method.getName())) {
			return methodProxy.invokeSuper(target, args);
		}
		
		Class<?> targetClass = target.getClass();
		if (targetClass.getName().indexOf("$$EnhancerBy") != -1) {
			targetClass = targetClass.getSuperclass();
		}
		
		
		MethodKey key = IntersCache.getMethodKey(targetClass, method);
		Interceptor[] inters = IntersCache.get(key);
		if (inters == null) {
			inters = interMan.buildServiceMethodInterceptor(targetClass, method);
			IntersCache.put(key, inters);
		}
		
		Invocation invocation = new Invocation(target, method, inters,
			x -> {
				return methodProxy.invokeSuper(target, x);
			}
		, args);
		
		
		invocation.invoke();
		return invocation.getReturnValue();
	}
	
	private static final Set<String> buildExcludedMethodName() {
		Set<String> excludedMethodName = new HashSet<String>(64, 0.25F);
		Method[] methods = Object.class.getDeclaredMethods();
		for (Method m : methods) {
			excludedMethodName.add(m.getName());
		}
		// getClass() registerNatives() can not be enhanced
		// excludedMethodName.remove("getClass");	
		// excludedMethodName.remove("registerNatives");
		return excludedMethodName;
	}
}


