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
 * <pre>
 * 注意：
 * 1：如果被加载的文件是 class path 中的普通文件，则该文件支持热加载
 * 
 * 2：如果被加载的文件处于 jar 包之中，则该文件不支持热加载，jar 包之中的文件在运行时通常不会被修改
 *    在极少数情况下如果需要对 jar 包之中的模板文件进行热加载，可以通过继承 ClassPathSource
 *    的方式进行扩展
 * 
 * 3：JFinal Template Engine 开启热加载需要配置 engine.setDevMode(true)
 * </pre>
 */
public class ClassPathSource implements ISource {
	
	protected String finalFileName;
	protected String fileName;
	protected String encoding;
	
	protected boolean isInJar;
	protected long lastModified;
	protected ClassLoader classLoader;
	protected URL url;
	
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
		
		processIsInJarAndlastModified();
	}
	
	protected void processIsInJarAndlastModified() {
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
	
	protected ClassLoader getClassLoader() {
		ClassLoader ret = Thread.currentThread().getContextClassLoader();
		return ret != null ? ret : getClass().getClassLoader();
	}
	
	protected String buildFinalFileName(String baseTemplatePath, String fileName) {
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
	
	protected long getLastModified() {
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
		// 与 FileSorce 不同，ClassPathSource 在构造方法中已经初始化了 lastModified
		// 下面的代码可以去掉，在此仅为了避免继承类忘了在构造中初始化 lastModified 的防卫式代码
		if (!isInJar) {		// 如果模板文件不在 jar 包文件之中，则需要更新 lastModified 值
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

