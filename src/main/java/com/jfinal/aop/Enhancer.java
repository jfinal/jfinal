/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
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

/**
 * Enhancer
 * 
 * <pre>
 * 自 jfinal 3.5 开始，新增了更强大的 Aop 工具，建议使用 Aop.get(...) 以及
 * Aop.inject(...) 来代替 Enhancer 的功能
 * 
 * 下一个版本所有 Aop 功能将会被 Aop.java 取代，并且为了拦截器的整体缓存不会再支持
 * Inject Interceptor 参数，所以删除了 Enhancer 中所有带 injectInters 参数
 * 的方法
 * 
 * 下一个版本的 Singleton 判别将由 @Singleton 注解以及 AopFactory 中的默认值决定，
 * 所以删除了 Enhancer 中所有带 singletonKey 参数的方法
 * </pre>
 */
@SuppressWarnings("unchecked")
public class Enhancer {
	
	private Enhancer() {}
	
	public static <T> T enhance(Class<T> targetClass) {
		return (T)net.sf.cglib.proxy.Enhancer.create(targetClass, new Callback());
	}
	
	/**
	 * 下一个版本的 aop 将不再支持 inject interceptor，所以本方法被 Deprecated
	 */
	@Deprecated
	public static <T> T enhance(Class<T> targetClass, Interceptor... injectInters) {
		return (T)net.sf.cglib.proxy.Enhancer.create(targetClass, new Callback(injectInters));
	}
}


