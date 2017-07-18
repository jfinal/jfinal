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

package com.jfinal.template.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import com.jfinal.template.EngineConfig;

/**
 * ClassPathSource 用于从 class path 以及 jar 包之中加载模板内容
 * 
 * 注意：
 * 1：如果被加载的文件是 class path 中的普通文件，则该文件支持热加载
 * 2：如果被加载的文件处于 jar 包之中，则该文件不支持热加载
 * 3：JFinal Template Engine 开启热加载需要配置 engine.setDevMode(true)
 */
public class ClassPathSource implements ISource {
	
	private String finalFileName;
	private String fileName;
	private String encoding;
	
	private boolean isInJar;
	private long lastModified;
	private ClassLoader classLoader;
	private URL url;
	
	public ClassPathSource(String fileName) {
		this(null, fileName, EngineConfig.DEFAULT_ENCODING);
	}
	
	public ClassPathSource(String baseTemplatePath, String fileName) {
		this(baseTemplatePath, fileName, EngineConfig.DEFAULT_ENCODING);
	}
	
	public ClassPathSource(String baseTemplatePath, String fileName, String encoding) {
		this.finalFileName = buildFinalFileName(baseTemplatePath, fileName);
		this.fileName = fileName;
		this.encoding= encoding;
		this.classLoader = getClassLoader();
		this.url = classLoader.getResource(finalFileName);
		if (url == null) {
			throw new IllegalArgumentException("File not found : \"" + finalFileName + "\"");
		}
		
		processIsInJarAndLastModified();
	}
	
	private void processIsInJarAndLastModified() {
		try {
			URLConnection conn = url.openConnection();
			if ("jar".equals(url.getProtocol()) || conn instanceof JarURLConnection) {
				isInJar = true;
				lastModified = -1;
			} else {
				isInJar = false;
				lastModified = conn.getLastModified();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ClassLoader getClassLoader() {
		ClassLoader ret = Thread.currentThread().getContextClassLoader();
		return ret != null ? ret : getClass().getClassLoader();
	}
	
	private String buildFinalFileName(String baseTemplatePath, String fileName) {
		String finalFileName;
		if (baseTemplatePath != null) {
			char firstChar = fileName.charAt(0);
			if (firstChar == '/' || firstChar == '\\') {
				finalFileName = baseTemplatePath + fileName;
			} else {
				finalFileName = baseTemplatePath + "/" + fileName;
			}
		} else {
			finalFileName = fileName;
		}
		
		if (finalFileName.charAt(0) == '/') {
			finalFileName = finalFileName.substring(1);
		}
		
		return finalFileName;
	}
	
	public String getKey() {
		return fileName;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	private long getLastModified() {
		try {
			URLConnection conn = url.openConnection();
			return conn.getLastModified();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 模板文件在 jar 包文件之内则不支持热加载
	 */
	public boolean isModified() {
		return isInJar ? false : lastModified != getLastModified();
	}
	
	public StringBuilder getContent() {
		// 如果模板文件不在 jar 包文件之中，则需要更新 lastModified 值，否则在模板文件被修改后会不断 reload 模板文件
		if (!isInJar) {
			lastModified = getLastModified();
		}
		
		InputStream inputStream = classLoader.getResourceAsStream(finalFileName);
		if (inputStream == null) {
			throw new RuntimeException("File not found : \"" + finalFileName + "\"");
		}
		
		return loadFile(inputStream, encoding);
	}
	
	public static StringBuilder loadFile(InputStream inputStream, String encoding) {
		StringBuilder ret = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream, encoding));
			// br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			if (line != null) {
				ret.append(line);
			} else {
				return ret;
			}
			
			while ((line=br.readLine()) != null) {
				ret.append('\n').append(line);
			}
			return ret;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					com.jfinal.kit.LogKit.error(e.getMessage(), e);
				}
			}
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("In Jar File: ").append(isInJar).append("\n");
		sb.append("File name: ").append(fileName).append("\n");
		sb.append("Final file name: ").append(finalFileName).append("\n");
		sb.append("Last modified: ").append(lastModified).append("\n");
		return sb.toString();
	}
}


/*
	protected File getFile(URL url) {
		try {
			// return new File(url.toURI().getSchemeSpecificPart());
			return new File(url.toURI());
		} catch (URISyntaxException ex) {
			// Fallback for URLs that are not valid URIs (should hardly ever happen).
			return new File(url.getFile());
		}
	}
*/

