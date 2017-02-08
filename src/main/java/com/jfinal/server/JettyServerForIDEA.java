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

package com.jfinal.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import com.jfinal.core.Const;
import com.jfinal.kit.FileKit;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;

/**
 * IDEA 专用于在 IDEA 之下用 main 方法启动，原启动方式在 IDEA 下会报异常
 * 注意：用此方法启动对热加载支持不完全，只支持方法内部修改的热加载，不支持添加、删除方法
 *      不支持添加类文件热加载，建议开发者在 IDEA 下使用 jrebel 或者 maven 下的 jetty
 *      插件支持列为完全的热加载
 */
public class JettyServerForIDEA implements IServer {
	
	private String webAppDir;
	private int port;
	private String context;
	// private int scanIntervalSeconds;
	private boolean running = false;
	private Server server;
	private WebAppContext webApp;
	
	public JettyServerForIDEA(String webAppDir, int port, String context) {
		if (webAppDir == null) {
			throw new IllegalStateException("Invalid webAppDir of web server: " + webAppDir);
		}
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Invalid port of web server: " + port);
		}
		if (StrKit.isBlank(context)) {
			throw new IllegalStateException("Invalid context of web server: " + context);
		}
		
		this.webAppDir = webAppDir;
		this.port = port;
		this.context = context;
		// this.scanIntervalSeconds = scanIntervalSeconds;
	}
	
	public void start() {
		if (!running) {
			try {
				running = true;
				doStart();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				LogKit.error(e.getMessage(), e);
			}
		}
	}
	
	public void stop() {
		if (running) {
			try {server.stop();} catch (Exception e) {LogKit.error(e.getMessage(), e);}
			running = false;
		}
	}
	
	private void doStart() {
		if (!available(port)) {
			throw new IllegalStateException("port: " + port + " already in use!");
		}
		
		deleteSessionData();
		
		System.out.println("Starting JFinal " + Const.JFINAL_VERSION);
		server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		server.addConnector(connector);
		webApp = new WebAppContext();
		webApp.setThrowUnavailableOnStartupException(true);	// 在启动过程中允许抛出异常终止启动并退出 JVM
		webApp.setContextPath(context);
		webApp.setResourceBase(webAppDir);	// webApp.setWar(webAppDir);
		webApp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
		webApp.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");	// webApp.setInitParams(Collections.singletonMap("org.mortbay.jetty.servlet.Default.useFileMappedBuffer", "false"));
		persistSession(webApp);
		
		server.setHandler(webApp);
		// changeClassLoader(webApp);
		
		// configureScanner
//		if (scanIntervalSeconds > 0) {
//			Scanner scanner = new Scanner(PathKit.getRootClassPath(), scanIntervalSeconds) {
//				public void onChange() {
//					try {
//						System.err.println("\nLoading changes ......");
//						webApp.stop();
//						JFinalClassLoader loader = new JFinalClassLoader(webApp, getClassPath());
//						webApp.setClassLoader(loader);
//						webApp.start();
//						System.err.println("Loading complete.");
//					} catch (Exception e) {
//						System.err.println("Error reconfiguring/restarting webapp after change in watched files");
//						LogKit.error(e.getMessage(), e);
//					}
//				}
//			};
//			System.out.println("Starting scanner at interval of " + scanIntervalSeconds + " seconds.");
//			scanner.start();
//		}
		
		try {
			System.out.println("Starting web server on port: " + port);
			server.start();
			System.out.println("Starting Complete. Welcome To The JFinal World :)");
			server.join();
		} catch (Exception e) {
			LogKit.error(e.getMessage(), e);
			System.exit(100);
		}
		return;
	}
	
//	private void changeClassLoader(WebAppContext webApp) {
//		try {
//			String classPath = getClassPath();
//			JFinalClassLoader jfcl = new JFinalClassLoader(webApp, classPath);
//			// jfcl.addClassPath(classPath);
//			webApp.setClassLoader(jfcl);
//		} catch (IOException e) {
//			LogKit.error(e.getMessage(), e);
//		}
//	}
	
//	private String getClassPath() {
//		return System.getProperty("java.class.path");
//	}
	
	private void deleteSessionData() {
		try {
			FileKit.delete(new File(getStoreDir()));
		}
		catch (Exception e) {
			LogKit.logNothing(e);
		}
	}
	
	private String getStoreDir() {
		String storeDir = PathKit.getWebRootPath() + "/../../session_data" + context;
		if ("\\".equals(File.separator)) {
			storeDir = storeDir.replaceAll("/", "\\\\");
		}
		return storeDir;
	}
	
	private void persistSession(WebAppContext webApp) {
		String storeDir = getStoreDir();
		
		SessionManager sm = webApp.getSessionHandler().getSessionManager();
		if (sm instanceof HashSessionManager) {
			((HashSessionManager)sm).setStoreDirectory(new File(storeDir));
			return ;
		}
		
		HashSessionManager hsm = new HashSessionManager();
		hsm.setStoreDirectory(new File(storeDir));
		SessionHandler sh = new SessionHandler();
		sh.setSessionManager(hsm);
		webApp.setSessionHandler(sh);
	}
	
	private static boolean available(int port) {
		if (port <= 0) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}
		
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			LogKit.logNothing(e);
		} finally {
			if (ds != null) {
				ds.close();
			}
			
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					// should not be thrown, just detect port available.
					LogKit.logNothing(e);
				}
			}
		}
		return false;
	}
}






