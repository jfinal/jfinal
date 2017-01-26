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

package com.jfinal.aop;

/**
 * duang duang duang
 */
public class Duang {
	
	private Duang(){}

	public static <T> T duang(Class<T> targetClass) {
		return (T)Enhancer.enhance(targetClass);
	}
	
	public static <T> T duang(Class<T> targetClass, Interceptor... injectInters) {
		return (T)Enhancer.enhance(targetClass, injectInters);
	}
	
	public static <T> T duang(Class<T> targetClass, Class<? extends Interceptor>... injectIntersClasses) {
		return (T)Enhancer.enhance(targetClass, injectIntersClasses);
	}
	
	public static <T> T duang(Class<T> targetClass, Class<? extends Interceptor> injectIntersClass) {
		return (T)Enhancer.enhance(targetClass, injectIntersClass);
	}
	
	public static <T> T duang(Class<T> targetClass, Class<? extends Interceptor> injectIntersClass1, Class<? extends Interceptor> injectIntersClass2) {
		return (T)Enhancer.enhance(targetClass, injectIntersClass1, injectIntersClass2);
	}
	
	public static <T> T duang(Class<T> targetClass, Class<? extends Interceptor> injectIntersClass1, Class<? extends Interceptor> injectIntersClass2, Class<? extends Interceptor> injectIntersClass3) {
		return (T)Enhancer.enhance(targetClass, injectIntersClass1, injectIntersClass2, injectIntersClass3);
	}
	
	public static <T> T getTarget(String singletonKey) {
		return (T)Enhancer.getTarget(singletonKey);
	}
	
	public static <T> T duang(String singletonKey, Class<T> targetClass) {
		return (T)Enhancer.enhance(singletonKey, targetClass);
	}
	
	public static <T> T duang(String singletonKey, Class<T> targetClass, Interceptor... injectInters) {
		return (T)Enhancer.enhance(singletonKey, targetClass, injectInters);
	}
	
	public static <T> T duang(String singletonKey, Class<T> targetClass, Class<? extends Interceptor>... injectIntersClasses) {
		return (T)Enhancer.enhance(singletonKey, targetClass, injectIntersClasses);
	}
	
	public static <T> T duang(Object target) {
		return (T)Enhancer.enhance(target);
	}
	
	public static <T> T duang(Object target, Interceptor... injectInters) {
		return (T)Enhancer.enhance(target, injectInters);
	}
	
	public static <T> T duang(Object target, Class<? extends Interceptor>... injectIntersClasses) {
		return (T)Enhancer.enhance(target, injectIntersClasses);
	}
	
	public static <T> T duang(Object target, Class<? extends Interceptor> injectIntersClass) {
		return (T)Enhancer.enhance(target, injectIntersClass);
	}
	
	public static <T> T duang(Object target, Class<? extends Interceptor> injectIntersClass1, Class<? extends Interceptor> injectIntersClass2) {
		return (T)Enhancer.enhance(target, injectIntersClass1, injectIntersClass2);
	}
	
	public static <T> T duang(Object target, Class<? extends Interceptor> injectIntersClass1, Class<? extends Interceptor> injectIntersClass2, Class<? extends Interceptor> injectIntersClass3) {
		return (T)Enhancer.enhance(target, injectIntersClass1, injectIntersClass2, injectIntersClass3);
	}
	
	public static <T> T duang(String singletonKey, Object target) {
		return (T)Enhancer.enhance(singletonKey, target);
	}
	
	public static <T> T duang(String singletonKey, Object target, Interceptor... injectInters) {
		return (T)Enhancer.enhance(singletonKey, target, injectInters);
	}
	
	public static <T> T duang(String singletonKey, Object target, Class<? extends Interceptor>... injectIntersClasses) {
		return (T)Enhancer.enhance(singletonKey, target, injectIntersClasses);
	}
}




