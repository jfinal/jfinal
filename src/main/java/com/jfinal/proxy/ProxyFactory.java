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

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProxyFactory
 */
@SuppressWarnings("unchecked")
public class ProxyFactory {

	protected ConcurrentHashMap<Class<?>, Class<?>> cache = new ConcurrentHashMap<>();

	protected ProxyGenerator proxyGenerator = new ProxyGenerator();
	protected ProxyCompiler proxyCompiler = new ProxyCompiler();
	protected ProxyClassLoader proxyClassLoader = new ProxyClassLoader();

	public <T> T get(Class<T> target) {
		try {
			Class<T> ret = (Class<T>)cache.get(target);
			if (ret != null) {
				return (T)ret.newInstance();
			} else {
				return getProxyClass(target).newInstance();
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	protected <T> Class<T> getProxyClass(Class<T> target) throws ReflectiveOperationException {
		// 在此不对 static 类做检测，支持对 static 类的代理
		int mod = target.getModifiers();
		if ( ! Modifier.isPublic(mod) ) {
			throw new IllegalArgumentException("Only public class can be proxied : " + target.getName());
		}
		if (Modifier.isFinal(mod)) {
			throw new IllegalArgumentException("final class can not be proxied : " + target.getName());
		}
		if (Modifier.isAbstract(mod)) {
			throw new IllegalArgumentException("abstract class or interface can not be proxied : " + target.getName());
		}

		synchronized (target) {
			Class<T> ret = (Class<T>)cache.get(target);
			if (ret != null) {
				return ret;
			}

			ProxyClass proxyClass = proxyGenerator.generate(target);
			if (proxyClass.needProxy()) {
				proxyCompiler.compile(proxyClass);
				ret = (Class<T>)proxyClassLoader.loadProxyClass(proxyClass);
				proxyClass.setClazz(ret);

				cacheMethodProxy(proxyClass);	// 放在 loadClass 动作之后

				cache.put(target, ret);
				return ret;
			} else {
				cache.put(target, target);		// 无需代理的情况映射原参数 target
				return target;
			}
		}
	}

	/**
	 * 在生成类被 loadClass 成功以后缓存 MethodProxy，否则 MethodProxyCache
	 * 将存进去不健康的 ProxyMethod
	 */
	protected void cacheMethodProxy(ProxyClass proxyClass) {
		for (ProxyMethod m : proxyClass.getProxyMethodList()) {
			m.setProxyClass(proxyClass.getClazz());
			ProxyMethodCache.put(m);
		}
	}

	public void setProxyGenerator(ProxyGenerator proxyGenerator) {
		Objects.requireNonNull(proxyGenerator, "proxyGenerator can not be null");
		this.proxyGenerator = proxyGenerator;
	}

	public ProxyGenerator getProxyGenerator() {
		return proxyGenerator;
	}

	public void setProxyCompiler(ProxyCompiler proxyCompiler) {
		Objects.requireNonNull(proxyCompiler, "proxyCompiler can not be null");
		this.proxyCompiler = proxyCompiler;
	}

	public ProxyCompiler getProxyCompiler() {
		return proxyCompiler;
	}

	public void setProxyClassLoader(ProxyClassLoader proxyClassLoader) {
		Objects.requireNonNull(proxyClassLoader, "proxyClassLoader can not be null");
		this.proxyClassLoader = proxyClassLoader;
	}

	public ProxyClassLoader getProxyClassLoader() {
		return proxyClassLoader;
	}
}

