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

package com.jfinal.core;

import java.util.List;
import javax.servlet.ServletContext;
import com.jfinal.config.Constants;
import com.jfinal.config.JFinalConfig;
import com.jfinal.handler.Handler;
import com.jfinal.handler.HandlerFactory;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.render.RenderManager;
import com.jfinal.server.JettyServerForIDEA;
import com.jfinal.server.IServer;
import com.jfinal.server.ServerFactory;
import com.jfinal.token.ITokenCache;
import com.jfinal.token.TokenManager;
import com.jfinal.upload.OreillyCos;

/**
 * JFinal
 */
public final class JFinal {
	
	private Constants constants;
	private ActionMapping actionMapping;
	private Handler handler;
	private ServletContext servletContext;
	private String contextPath = "";
	private static IServer server;
	
	private static final JFinal me = new JFinal();
	
	private JFinal() {
	}
	
	public static JFinal me() {
		return me;
	}
	
	void init(JFinalConfig jfinalConfig, ServletContext servletContext) {
		this.servletContext = servletContext;
		this.contextPath = servletContext.getContextPath();
		
		initPathKit();
		
		Config.configJFinal(jfinalConfig);	// start plugin, init log factory and init engine in this method
		constants = Config.getConstants();
		
		initActionMapping();
		initHandler();
		initRender();
		initOreillyCos();
		initTokenManager();
	}
	
	private void initTokenManager() {
		ITokenCache tokenCache = constants.getTokenCache();
		if (tokenCache != null) {
			TokenManager.init(tokenCache);
		}
	}
	
	private void initHandler() {
		ActionHandler actionHandler = Config.getHandlers().getActionHandler();
		if (actionHandler == null) {
			actionHandler = new ActionHandler();
		}
		
		actionHandler.init(actionMapping, constants);
		handler = HandlerFactory.getHandler(Config.getHandlers().getHandlerList(), actionHandler);
	}
	
	private void initOreillyCos() {
		OreillyCos.init(constants.getBaseUploadPath(), constants.getMaxPostSize(), constants.getEncoding());
	}
	
	private void initPathKit() {
		String path = servletContext.getRealPath("/");
		PathKit.setWebRootPath(path);
	}
	
	private void initRender() {
		RenderManager.me().init(Config.getEngine(), constants, servletContext);
	}
	
	private void initActionMapping() {
		actionMapping = new ActionMapping(Config.getRoutes());
		actionMapping.buildActionMapping();
		Config.getRoutes().clear();
	}
	
	void stopPlugins() {
		List<IPlugin> plugins = Config.getPlugins().getPluginList();
		if (plugins != null) {
			for (int i=plugins.size()-1; i >= 0; i--) {		// stop plugins
				boolean success = false;
				try {
					success = plugins.get(i).stop();
				} 
				catch (Exception e) {
					success = false;
					LogKit.error(e.getMessage(), e);
				}
				if (!success) {
					System.err.println("Plugin stop error: " + plugins.get(i).getClass().getName());
				}
			}
		}
	}
	
	Handler getHandler() {
		return handler;
	}
	
	public Constants getConstants() {
		return Config.getConstants();
	}
	
	public String getContextPath() {
		return contextPath;
	}
	
	public ServletContext getServletContext() {
		return this.servletContext;
	}
	
	public Action getAction(String url, String[] urlPara) {
		return actionMapping.getAction(url, urlPara);
	}
	
	public List<String> getAllActionKeys() {
		return actionMapping.getAllActionKeys();
	}
	
	public static void start() {
		server = ServerFactory.getServer();
		server.start();
	}
	
	/**
	 * 用于在 Eclipse 中，通过创建 main 方法的方式启动项目，支持热加载
	 */
	public static void start(String webAppDir, int port, String context, int scanIntervalSeconds) {
		server = ServerFactory.getServer(webAppDir, port, context, scanIntervalSeconds);
		server.start();
	}
	
	/**
	 * 用于在 IDEA 中，通过创建 main 方法的方式启动项目，不支持热加载
	 * 本方法存在的意义在于此方法启动的速度比 maven 下的 jetty 插件要快得多
	 * 
	 * 注意：不支持热加载。建议通过 Ctrl + F5 快捷键，来快速重新启动项目，速度并不会比 eclipse 下的热加载慢多少
	 *     实际操作中是先通过按 Alt + 5 打开 debug 窗口，才能按 Ctrl + F5 重启项目
	 */
	public static void start(String webAppDir, int port, String context) {
		server = new JettyServerForIDEA(webAppDir, port, context);
		server.start();
	}
	
	public static void stop() {
		server.stop();
	}
	
	/**
	 * Run JFinal Server with Debug Configurations or Run Configurations in Eclipse or IDEA
	 * Example for Eclipse:	src/main/webapp 80 / 5
	 * Example for IDEA:	src/main/webapp 80 /
	 */
	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			server = ServerFactory.getServer();
			server.start();
			return ;
		}
		
		// for Eclipse
		if (args.length == 4) {
			String webAppDir = args[0];
			int port = Integer.parseInt(args[1]);
			String context = args[2];
			int scanIntervalSeconds = Integer.parseInt(args[3]);
			server = ServerFactory.getServer(webAppDir, port, context, scanIntervalSeconds);
			server.start();
			return ;
		}
		
		// for IDEA
		if (args.length == 3) {
			start(args[0], Integer.parseInt(args[1]), args[2]);
			return ;
		}
		
		throw new RuntimeException("Boot parameter error. The right parameter like this: src/main/webapp 80 / 5");
	}
}










