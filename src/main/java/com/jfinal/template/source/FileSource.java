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

package com.jfinal.template.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import com.jfinal.template.EngineConfig;

/**
 * FileSource 用于从普通文件中加载模板内容
 */
public class FileSource implements ISource {
	
	private String finalFileName;
	private String fileName;
	private String encoding;
	
	private long lastModified;
	
	public FileSource(String baseTemplatePath, String fileName, String encoding) {
		this.finalFileName = buildFinalFileName(baseTemplatePath, fileName);
		this.fileName = fileName;
		this.encoding= encoding;
	}
	
	public FileSource(String baseTemplatePath, String fileName) {
		this(baseTemplatePath, fileName, EngineConfig.DEFAULT_ENCODING);
	}
	
	public boolean isModified() {
		return lastModified != new File(finalFileName).lastModified();
	}
	
	public String getCacheKey() {
		return fileName;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public String getFinalFileName() {
		return finalFileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public StringBuilder getContent() {
		File file = new File(finalFileName);
		if (!file.exists()) {
			throw new RuntimeException("File not found : \"" + finalFileName + "\"");
		}
		
		// 极为重要，否则在开发模式下 isModified() 一直返回 true，缓存一直失效（原因是 lastModified 默认值为 0）
		this.lastModified = file.lastModified();
		
		return loadFile(file, encoding);
	}
	
	private String buildFinalFileName(String baseTemplatePath, String fileName) {
		if (baseTemplatePath == null) {
			return fileName;
		}
		char firstChar = fileName.charAt(0);
		String finalFileName;
		if (firstChar == '/' || firstChar == '\\') {
			finalFileName = baseTemplatePath + fileName;
		} else {
			finalFileName = baseTemplatePath + File.separator + fileName;
		}
		return finalFileName;
	}
	
	public static StringBuilder loadFile(File file, String encoding) {
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), encoding)) {
			StringBuilder ret = new StringBuilder((int)file.length() + 3);
			char[] buf = new char[1024];
			for (int num; (num = isr.read(buf, 0, buf.length)) != -1;) {
				ret.append(buf, 0, num);
			}
			return ret;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("File name: ").append(fileName).append("\n");
		sb.append("Final file name: ").append(finalFileName).append("\n");
		sb.append("Last modified: ").append(lastModified).append("\n");
		return sb.toString();
	}
}




