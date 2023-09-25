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

import java.util.Objects;

/**
 * ProxyManager
 */
public class ProxyManager {
	
	private static final ProxyManager me = new ProxyManager();
	
	private ProxyManager() {}
	
	public static ProxyManager me() {
		return me;
	}
	
	public ProxyManager setProxyFactory(ProxyFactory proxyFactory) {
		Objects.requireNonNull(proxyFactory, "proxyFactory can not be null");
		Proxy.proxyFactory = proxyFactory;
		return this;
	}
	
	public ProxyFactory setPrintGeneratedClassToConsole(boolean printGeneratedClassToConsole) {
		Proxy.proxyFactory.getProxyGenerator().setPrintGeneratedClassToConsole(printGeneratedClassToConsole);
		return Proxy.proxyFactory;
	}
	
	public ProxyFactory setPrintGeneratedClassToLog(boolean printGeneratedClassToLog) {
        Proxy.proxyFactory.getProxyGenerator().setPrintGeneratedClassToLog(printGeneratedClassToLog);
        return Proxy.proxyFactory;
    }
	
	public ProxyFactory getProxyFactory() {
		return Proxy.proxyFactory;
	}
}





