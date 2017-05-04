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

package com.jfinal.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import com.jfinal.core.Const;

/**
 * FileStringSource
 */
public class FileStringSource implements IStringSource {
	
	private String finalFileName;
	private String fileName;
	private String encoding;
	
	private long lastModified;
	
	public FileStringSource(String baseTemplatePath, String fileName, String encoding) {
		this.finalFileName = buildFinalFileName(baseTemplatePath, fileName);
		this.fileName = fileName;
		this.encoding= encoding;
	}
	
	public FileStringSource(String baseTemplatePath, String fileName) {
		this(baseTemplatePath, fileName, Const.DEFAULT_ENCODING);
	}
	
	public boolean isModified() {
		return lastModified != new File(finalFileName).lastModified();
	}
	
	public String getKey() {
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
			throw new RuntimeException("File not found : " + finalFileName);
		}
		
		// 极为重要，否则在模板文件被修改后会不断 reload 模板文件
		this.lastModified = file.lastModified();
		
		return loadFile(file, encoding);
	}
	
	private String buildFinalFileName(String baseTemplatePath, String fileName) {
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
		StringBuilder ret = new StringBuilder((int)file.length() + 3);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
			// br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			if (line != null) {
				ret.append(line);
			} else {
				return ret;
			}
			
			while ((line=br.readLine()) != null) {
				ret.append("\n").append(line);
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
		sb.append("File name: ").append(fileName).append("\n");
		sb.append("Final file name: ").append(finalFileName).append("\n");
		sb.append("Last modified: ").append(lastModified).append("\n");
		return sb.toString();
	}
}




