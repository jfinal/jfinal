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

package com.jfinal.plugin.ehcache;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * EvictInterceptor.
 */
public class EvictInterceptor implements Interceptor {
	
	public void intercept(Invocation inv) {
		inv.invoke();
		
		// @CacheName 注解中的多个 cacheName 可用逗号分隔
		String[] cacheNames = getCacheName(inv).split(",");
		if (cacheNames.length == 1) {
			CacheKit.removeAll(cacheNames[0].trim());
		} else {
			for (String cn : cacheNames) {
				CacheKit.removeAll(cn.trim());
			}
		}
	}
	
	/**
	 * 获取 @CacheName 注解配置的 cacheName，注解可配置在方法和类之上
	 */
	protected String getCacheName(Invocation inv) {
		CacheName cacheName = inv.getMethod().getAnnotation(CacheName.class);
		if (cacheName != null) {
			return cacheName.value();
		}
		
		cacheName = inv.getController().getClass().getAnnotation(CacheName.class);
		if (cacheName == null) {
			throw new RuntimeException("EvictInterceptor need CacheName annotation in controller.");
		}
		
		return cacheName.value();
	}
}

