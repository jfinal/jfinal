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

package com.jfinal.server;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.MBeanServer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.management.MBeanContainer;
import org.mortbay.util.Scanner;
import com.jfinal.util.PathUtil;

/**
 * JettyServer is used to config and start jetty web server.
 * Jetty version 6.1.26
 */
/*
 * 1: project dir (no use)
 * 2: port
 * 3: context
 * 4: webapp dir
 * 5: scan interval senconds
 */
class JettyServer implements IServer {
	
	private String webAppDir;
	private int port;
	private String context;
	private int scanIntervalSeconds;
	private boolean isStarted = false;
	private Server server;
	private WebAppContext web;
	private boolean enablescanner = true;
	
	JettyServer(String webAppDir, int port, String context, int scanIntervalSeconds) {
		this.webAppDir = webAppDir;
		this.port = port;
		this.context = context;
		this.scanIntervalSeconds = scanIntervalSeconds;
		checkConfig();
	}
	
	private void checkConfig() {
		if (port < 0 || port > 65536)
			throw new IllegalArgumentException("Invalid port of web server: " + port);
		
		if (scanIntervalSeconds < 1)
			enablescanner = false;
		
		if (context == null)
			throw new IllegalStateException("Invalid context of web server: " + context);
		
		if (webAppDir == null)
			throw new IllegalStateException("Invalid context of web server: " + webAppDir);
	}
	
	public void start() {
		if (! isStarted) {
			try {
				doStart();
			} catch (Exception e) {
				e.printStackTrace();
			}
			isStarted = true;
		}
		else {
			throw new RuntimeException("Server already started.");
		}
	}
	
	private void doStart() throws Exception {
		String context = this.context;
		String webAppDir = this.webAppDir;
		Integer port = this.port;
		Integer scanIntervalSeconds = this.scanIntervalSeconds;
		
		server = new Server();
		
		if (port != null) {
			if (!available(port)) {
				throw new IllegalStateException("port: " + port + " already in use!");
			}
			SelectChannelConnector connector = new SelectChannelConnector();
			connector.setPort(port);
			
			server.addConnector(connector);
		}
		
		web = new WebAppContext();
		
		// 警告: 设置成 true 无法支持热加载
		// web.setParentLoaderPriority(false);
		web.setContextPath(context);
		web.setWar(webAppDir);
		web.setInitParams(Collections.singletonMap("org.mortbay.jetty.servlet.Default.useFileMappedBuffer", "false"));
		server.addHandler(web);
		
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		server.getContainer().addEventListener(mBeanContainer);
		mBeanContainer.start();
		
		// configureScanner
		if (enablescanner) {
			final ArrayList<File> scanList = new ArrayList<File>();
			scanList.add(new File(PathUtil.getRootClassPath()));
			Scanner scanner = new Scanner();
			scanner.setReportExistingFilesOnStartup(false);
			scanner.setScanInterval(scanIntervalSeconds);
			scanner.setScanDirs(scanList);
			scanner.addListener(new Scanner.BulkListener() {
				
				public void filesChanged(@SuppressWarnings("rawtypes") List changes) {
					try {
						System.err.println("Loading changes ......");
						web.stop();
						web.start();
						System.err.println("Loading complete.\n");
						
					} catch (Exception e) {
						System.err.println("Error reconfiguring/restarting webapp after change in watched files");
						e.printStackTrace();
					}
				}
			});
			System.err.println("Starting scanner at interval of " + scanIntervalSeconds + " seconds.");
			scanner.start();
		}
		
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		return;
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
		} finally {
			if (ds != null) {
				ds.close();
			}
			
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					// should not be thrown, just detect port available.
				}
			}
		}
		return false;
	}
}






