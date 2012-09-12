/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.HttpServletRequest;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.jfinal.render.Render;

/**
 * CacheInterceptor.
 */
public class CacheInterceptor implements Interceptor {
	
	private static final String renderKey = "$renderKey$";
	private static volatile ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<String, ReentrantLock>();
	
	private ReentrantLock getLock(String key) {
		ReentrantLock lock = lockMap.get(key);
		if (lock != null)
			return lock;
		
		lock = new ReentrantLock();
		ReentrantLock previousLock = lockMap.putIfAbsent(key, lock);
		return previousLock == null ? lock : previousLock;
	}
	
	final public void intercept(ActionInvocation ai) {
		Controller controller = ai.getController();
		String cacheName = buildCacheName(ai, controller);
		String cacheKey = buildCacheKey(ai, controller);
		Map<String, Object> cacheData = CacheKit.get(cacheName, cacheKey);
		if (cacheData == null) {
			Lock lock = getLock(cacheName);
			lock.lock();					// prevent cache snowslide
			try {
				cacheData = CacheKit.get(cacheName, cacheKey);
				if (cacheData == null) {
					ai.invoke();
					cacheAction(cacheName, cacheKey, controller);
					return ;
				}
			}
			finally {
				lock.unlock();
			}
		}
		
		useCacheDataAndRender(cacheData, controller);
	}
	
	private String buildCacheName(ActionInvocation ai, Controller controller) {
		CacheName cacheName = ai.getMethod().getAnnotation(CacheName.class);
		if (cacheName != null)
			return cacheName.value();
		cacheName = controller.getClass().getAnnotation(CacheName.class);
		if (cacheName != null)
			return cacheName.value();
		return ai.getActionKey();
	}
	
	private String buildCacheKey(ActionInvocation ai, Controller controller) {
		StringBuilder sb = new StringBuilder(ai.getActionKey());
		String urlPara = controller.getPara();
		if (urlPara != null)
			sb.append("/").append(urlPara);
		
		String queryString = controller.getRequest().getQueryString();
		if (queryString != null)
			sb.append("?").append(queryString);
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	private void cacheAction(String cacheName, String cacheKey, Controller controller) {
		HttpServletRequest request = controller.getRequest();
		Map<String, Object> cacheData = new HashMap<String, Object>();
		for (Enumeration<String> names=request.getAttributeNames(); names.hasMoreElements();) {
			String name = names.nextElement();
			cacheData.put(name, request.getAttribute(name));
		}
		
		cacheData.put(renderKey, controller.getRender());		// cache render
		CacheKit.put(cacheName, cacheKey, cacheData);
	}
	
	private void useCacheDataAndRender(Map<String, Object> data, Controller controller) {
		HttpServletRequest request = controller.getRequest();
		Set<Entry<String, Object>> set = data.entrySet();
		for (Iterator<Entry<String, Object>> it=set.iterator(); it.hasNext();) {
			Entry<String, Object> entry = it.next();
			request.setAttribute(entry.getKey(), entry.getValue());
		}
		
		controller.render((Render)request.getAttribute(renderKey));		// set render from cache
		request.removeAttribute(renderKey);
	}
}




