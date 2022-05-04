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

/**
 * Proxy
 */
public class Proxy {
	
	static ProxyFactory proxyFactory = new ProxyFactory();
	
	/**
	 * 获取代理对象
	 * @param target 被代理的类
	 * @return 代理对象
	 */
	public static <T> T get(Class<T> target) {
		return proxyFactory.get(target);
	}
}




