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

package com.jfinal.server;

import java.io.File;
import com.jfinal.kit.PathKit;

/**
 * ServerFactory
 */
public class ServerFactory {
	
	private static final int DEFAULT_PORT = 80;
	private static final int DEFAULT_SCANINTERVALSECONDS = 5;
	
	private ServerFactory() {
		
	}
	
	/**
	 * Return web server.
	 * <p>
	 * important: if scanIntervalSeconds < 1 then you will turn off the hot swap
	 * @param webAppDir the directory of the project web root
	 * @param port the port
	 * @param context the context
	 * @param scanIntervalSeconds the scan interval seconds
	 */
	public static IServer getServer(String webAppDir, int port, String context, int scanIntervalSeconds) {
		return new JettyServer(webAppDir, port, context, scanIntervalSeconds);
	}
	
	public static IServer getServer(String webAppDir, int port, String context) {
		return getServer(webAppDir, port, context, DEFAULT_SCANINTERVALSECONDS);
	}
	
	public static IServer getServer(int port, String context, int scanIntervalSeconds) {
		return getServer(detectWebAppDir(), port, context, scanIntervalSeconds);
	}
	
	public static IServer getServer(int port, String context) {
		return getServer(detectWebAppDir(), port, context, DEFAULT_SCANINTERVALSECONDS);
	}
	
	public static IServer getServer(int port) {
		return getServer(detectWebAppDir(), port, "/", DEFAULT_SCANINTERVALSECONDS);
	}
	
	public static IServer getServer() {
		return getServer(detectWebAppDir(), DEFAULT_PORT, "/", DEFAULT_SCANINTERVALSECONDS);
	}
	
	private static String detectWebAppDir() {
		String rootClassPath = PathKit.getRootClassPath();
		String[] temp = null;
		if (rootClassPath.indexOf("\\WEB-INF\\") != -1)
			temp = rootClassPath.split("\\\\");
		else if (rootClassPath.indexOf("/WEB-INF/") != -1)
			temp = rootClassPath.split("/");
		else
			throw new RuntimeException("WEB-INF directory not found.");
		return temp[temp.length - 3];
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private static String detectWebAppDir_old() {
		String rootClassPath = PathKit.getRootClassPath();
		String[] temp = null;
		try {
			temp = rootClassPath.split(File.separator);
		}
		catch (Exception e) {
			temp = rootClassPath.split("\\\\");
		}
		return temp[temp.length - 3];
	}
}




