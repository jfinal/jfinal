/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.InterceptorManager;
import com.jfinal.aop.Invocation;
import com.jfinal.ext.proxy.InterceptorCache.MethodKey;
import javassist.util.proxy.MethodHandler;

/**
 * JavassistCallback.
 */
class JavassistCallback implements MethodHandler {
	
	private static final InterceptorManager interMan = InterceptorManager.me();
	
	@Override
    public Object invoke(Object target, Method method, Method methodProxy, Object[] args) throws Throwable {
		Class<?> targetClass = target.getClass().getSuperclass();
		
		MethodKey key = InterceptorCache.getMethodKey(targetClass, method);
		Interceptor[] inters = InterceptorCache.get(key);
		if (inters == null) {
			inters = interMan.buildServiceMethodInterceptor(targetClass, method);
			InterceptorCache.put(key, inters);
		}
		
		if (inters.length == 0) {
		    return methodProxy.invoke(target, args);
		}
		
		Invocation invocation = new Invocation(target, method, inters,
			x -> {
				return methodProxy.invoke(target, x);
			}
		, args);
		invocation.invoke();
		return invocation.getReturnValue();
	}
}





