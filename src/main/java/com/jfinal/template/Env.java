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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.ast.Define;

/**
 * Env
 * 
 * 1：解析时存放 #define 定义的模板函数
 * 2：运行时提供 #define 定义的模板函数
 * 3：每个 Template 对象持有一个 Env 对象
 */
public class Env {
	
	EngineConfig engineConfig;
	private Map<String, Define> functionMap = new HashMap<String, Define>();
	private Map<String, TemplateFile> templateFileMap = null;
	
	public Env(EngineConfig engineConfig) {
		this.engineConfig = engineConfig;
	}
	
	public EngineConfig getEngineConfig() {
		return engineConfig;
	}
	
	/**
	 * Add template function
	 */
	public void addFunction(Define function) {
		String fn = function.getFunctionName();
		if (functionMap.containsKey(fn)) {
			Define previous = functionMap.get(fn);
			throw new ParseException(
				"Template function \"" + fn + "\" already defined in " + 
				getAlreadyDefinedLocation(previous.getLocation()),
				function.getLocation()
			);
		}
		functionMap.put(fn, function);
	}
	
	private String getAlreadyDefinedLocation(Location loc) {
		StringBuilder buf = new StringBuilder();
		if (loc.getTemplateFile() != null) {
			buf.append(loc.getTemplateFile()).append(", line ").append(loc.getRow());
		} else {
			buf.append("string template line ").append(loc.getRow());
		}
		return buf.toString();
	}
	
	/**
	 * Get function of current template first, getting shared function if null before
	 */
	public Define getFunction(String functionName) {
		Define func = functionMap.get(functionName);
		return func != null ? func : engineConfig.getSharedFunction(functionName);
	}
	
	/**
	 * For EngineConfig.addSharedFunction(...) only
	 */
	Map<String, Define> getFunctionMap() {
		return functionMap;
	}
	
	boolean isTemplateFileModified() {
		if (templateFileMap == null) {
			return false;
		}
		for (TemplateFile fi : templateFileMap.values()) {
			if (fi.isModified()) {
				return true;
			}
		}
		return false;
	}
	
	// For Engine only
	void addTemplateFinalFileName(String finalFileName) {
		if (templateFileMap == null) {
			templateFileMap = new HashMap<String, TemplateFile>();
		}
		templateFileMap.put(finalFileName, new TemplateFile(finalFileName));
	}
	
	// For Include only
	public void addTemplateFinalFileName(String finalFileName, String fileName, Location location) {
		if (templateFileMap == null) {
			templateFileMap = new HashMap<String, TemplateFile>();
		} else if (templateFileMap.containsKey(finalFileName)) {
			// 解决同一文件被同一模板多次 include 以及无限递归 include 问题
			throw new ParseException("Template file already included: " + fileName, location);
		}
		templateFileMap.put(finalFileName, new TemplateFile(finalFileName));
	}
	
	private static class TemplateFile {
		String finalFileName;
		long lastModified;
		
		TemplateFile(String finalFileName) {
			this.finalFileName = finalFileName;
			this.lastModified = new File(finalFileName).lastModified();
		}
		
		boolean isModified() {
			File file = new File(finalFileName);
			if (!file.exists()) {
				return true;		// 文件可能被删和改名
			}
			return lastModified != file.lastModified();
		}
	}
}



