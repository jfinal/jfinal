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

package com.jfinal.plugin.ehcache;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;

/**
 * EvictInterceptor.
 */
public class EvictInterceptor implements Interceptor {
	
	final public void intercept(ActionInvocation ai) {
		ai.invoke();
		
		CacheKit.removeAll(buildCacheName(ai));
	}
	
	private String buildCacheName(ActionInvocation ai) {
		CacheName cacheName = ai.getMethod().getAnnotation(CacheName.class);
		if (cacheName != null)
			return cacheName.value();
		
		cacheName = ai.getController().getClass().getAnnotation(CacheName.class);
		if (cacheName == null)
			throw new RuntimeException("EvictInterceptor need CacheName annotation in controller.");
		return cacheName.value();
	}
}

