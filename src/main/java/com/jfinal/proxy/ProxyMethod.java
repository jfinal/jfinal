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

package com.jfinal.proxy;

import java.lang.reflect.Method;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.InterceptorManager;

/**
 * ProxyMethod
 * 
 * 在 ProxyFactory 生成、编译、加载代理类彻底完成之后，
 * 再将 ProxyMethod 放入缓存，避免中途出现异常时缓存
 * 不完整的 ProxyMethod 对象
 */
public class ProxyMethod {
	
	static final InterceptorManager interMan = InterceptorManager.me();
	
	private Long key;
	
	private Class<?> targetClass;
	private Class<?> proxyClass;
	private Method method;
	private Interceptor[] interceptors = null;
	
	public void setKey(long key) {
		this.key = key;
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setTargetClass(Class<?> targetClass) {
		this.targetClass = targetClass;
	}
	
	public Class<?> getTargetClass() {
		return targetClass;
	}
	
	/**
	 * 代理类在 ProxyFactory 中才被 loadClass，所以本方法在 ProxyFactory 中被调用
	 */
	public void setProxyClass(Class<?> proxyClass) {
		this.proxyClass = proxyClass;
	}
	
	public Class<?> getProxyClass() {
		return proxyClass;
	}
	
	public void setMethod(Method method) {
		this.method = method;
	}
	
	public Method getMethod() {
		return method;
	}
	
	/**
	 * 分离类的生成与对象的创建，避免 ProxyGenerator 与 AopFactory 形成死循环
	 * 
	 * 本方法仅在 Invocation 构造方法中调用
	 */
	public Interceptor[] getInterceptors() {
		if (interceptors == null) {
			Interceptor[] ret = interMan.buildServiceMethodInterceptor(targetClass, method);
			interceptors = ret;
		}
		return interceptors;
	}
}


