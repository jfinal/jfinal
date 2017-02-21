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

package com.jfinal.core;

import java.util.List;
import com.jfinal.config.Constants;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Routes;
import com.jfinal.config.Plugins;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.plugin.IPlugin;

class Config {
	
	private static final Constants constants = new Constants();
	private static final Routes routes = new Routes(){public void config() {}};
	private static final Plugins plugins = new Plugins();
	private static final Interceptors interceptors = new Interceptors();
	private static final Handlers handlers = new Handlers();
	
	// prevent new Config();
	private Config() {
	}
	
	/*
	 * Config order: constant, route, plugin, interceptor, handler
	 */
	static void configJFinal(JFinalConfig jfinalConfig) {
		jfinalConfig.configConstant(constants);
		jfinalConfig.configRoute(routes);
		jfinalConfig.configPlugin(plugins);					startPlugins();	// very important!!!
		jfinalConfig.configInterceptor(interceptors);
		jfinalConfig.configHandler(handlers);
	}
	
	public static final Constants getConstants() {
		return constants;
	}
	
	public static final Routes getRoutes() {
		return routes;
	}
	
	public static final Plugins getPlugins() {
		return plugins;
	}
	
	public static final Interceptors getInterceptors() {
		return interceptors;
	}
	
	public static Handlers getHandlers() {
		return handlers;
	}
	
	private static void startPlugins() {
		List<IPlugin> pluginList = plugins.getPluginList();
		if (pluginList != null) {
			for (IPlugin plugin : pluginList) {
				try {
					boolean success = plugin.start();
					if (!success) {
						System.err.println("Plugin start error: " + plugin.getClass().getName());
						throw new RuntimeException("Plugin start error: " + plugin.getClass().getName());
					}
				}
				catch (Exception e) {
					System.err.println("Plugin start error: " + plugin.getClass().getName());
					throw new RuntimeException("Plugin start error: " + plugin.getClass().getName(), e);
				}
			}
		}
	}
}
