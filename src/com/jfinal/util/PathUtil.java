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

package com.jfinal.util;

import java.io.File;
import java.io.IOException;

/**
 * new File("..\path\abc.txt") 中的三个方法获取路径的方法
 * 1： getPath() 获取相对路径，例如   ..\path\abc.txt
 * 2： getAbslutlyPath() 获取绝对路径，但可能包含 ".." 或 "." 字符，例如  D:\otherPath\..\path\abc.txt
 * 3： getCanonicalPath() 获取绝对路径，但不包含 ".." 或 "." 字符，例如  D:\path\abc.txt
 */
public class PathUtil {
	
	private static String webRootPath;
	
	@SuppressWarnings("rawtypes")
	public static String getPath(Class clazz) {
		String path = clazz.getResource("").getPath();
		return new File(path).getAbsolutePath();
	}
	
	public static String getPath(Object object) {
		String path = object.getClass().getResource("").getPath();
		return new File(path).getAbsolutePath();
	}
	
	public static String getRootClassPath() {
		String path = PathUtil.class.getClassLoader().getResource("").getPath();
		return new File(path).getAbsolutePath();
	}
	
	public static String getPackagePath(Object object) {
		Package p = object.getClass().getPackage();
		return p != null ? p.getName().replaceAll("\\.", "/") : "";
	}
	
	public static File getFileFromJar(String file) {
		throw new RuntimeException("Not finish. Do not use this method.");
	}
	
	public static String getWebRootPath() {
		if (webRootPath == null)
			webRootPath = detectWebRootPath();;
		return webRootPath;
	}
	
	public static void setWebRootPath(String webRootPath) {
		if (webRootPath.endsWith(File.separator))
			webRootPath = webRootPath.substring(0, webRootPath.length() - 1);
		PathUtil.webRootPath = webRootPath;
	}
	
	private static String detectWebRootPath() {
		try {
			String path = PathUtil.class.getResource("/").getFile();
			return new File(path).getParentFile().getParentFile().getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}


