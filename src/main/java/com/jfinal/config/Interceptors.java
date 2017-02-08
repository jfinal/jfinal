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

package com.jfinal.config;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.InterceptorManager;

/**
 * The Interceptors is used to config global action interceptors and global service interceptors.
 */
final public class Interceptors {
	
	/**
	 * The same as addGlobalActionInterceptor. It is used to compatible with earlier version of jfinal
	 */
	public Interceptors add(Interceptor globalActionInterceptor) {
		if (globalActionInterceptor == null) {
			throw new IllegalArgumentException("globalActionInterceptor can not be null.");
		}
		InterceptorManager.me().addGlobalActionInterceptor(globalActionInterceptor);
		return this;
	}
	
	/**
	 * Add the global action interceptor to intercept all the actions.
	 */
	public Interceptors addGlobalActionInterceptor(Interceptor globalActionInterceptor) {
		if (globalActionInterceptor == null) {
			throw new IllegalArgumentException("globalActionInterceptor can not be null.");
		}
		InterceptorManager.me().addGlobalActionInterceptor(globalActionInterceptor);
		return this;
	}
	
	/**
	 * Add the global service interceptor to intercept all the method enhanced by aop Enhancer.
	 */
	public Interceptors addGlobalServiceInterceptor(Interceptor globalServiceInterceptor) {
		if (globalServiceInterceptor == null) {
			throw new IllegalArgumentException("globalServiceInterceptor can not be null.");
		}
		InterceptorManager.me().addGlobalServiceInterceptor(globalServiceInterceptor);
		return this;
	}
}

