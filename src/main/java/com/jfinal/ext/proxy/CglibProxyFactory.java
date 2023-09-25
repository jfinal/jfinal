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

import com.jfinal.proxy.ProxyFactory;

/**
 * CglibProxyFactory 用于扩展 cglib 的代理模式，默认不使用
 * 
 * <pre>
 * 配置方法：
 * public void configConstant(Constants me) {
 *     ProxyManager.me().setProxyFactory(new CglibProxyFactory());
 * }
 * </pre>
 */
public class CglibProxyFactory extends ProxyFactory {
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> target) {
		// 被 cglib 代理过的类名包含 "$$EnhancerBy"。仅需调用一次 getSuperclass() 即可
		if (target.getName().indexOf("$$EnhancerBy") > -1) {
			target = (Class<T>) target.getSuperclass();
		}
		return (T) net.sf.cglib.proxy.Enhancer.create(target, new CglibCallback());
	}
}



