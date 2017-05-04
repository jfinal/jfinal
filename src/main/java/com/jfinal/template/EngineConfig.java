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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.jfinal.core.Const;
import com.jfinal.kit.StrKit;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.expr.ast.SharedMethodKit;
import com.jfinal.template.ext.directive.*;
import com.jfinal.template.ext.sharedmethod.Json;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.Parser;
import com.jfinal.template.stat.ast.Define;
import com.jfinal.template.stat.ast.Output;
import com.jfinal.template.stat.ast.Stat;

/**
 * EngineConfig
 */
public class EngineConfig {
	
	private Map<String, Define> sharedFunctionMap = new HashMap<String, Define>();
	private List<IStringSource> sharedFunctionSourceList = new ArrayList<IStringSource>();		// for devMode only
	
	Map<String, Object> sharedObjectMap = null;
	
	private IOutputDirectiveFactory outputDirectiveFactory = OutputDirectiveFactory.me;
	private Map<String, Stat> directiveMap = new HashMap<String, Stat>();
	private SharedMethodKit sharedMethodKit = new SharedMethodKit();
	
	private boolean devMode = false;
	private boolean reloadModifiedSharedFunctionInDevMode = true;
	private String baseTemplatePath = null;
	private String encoding = Const.DEFAULT_ENCODING;
	private String datePattern = "yyyy-MM-dd HH:mm";
	
	public EngineConfig() {
		// Add official directive of Template Engine
		addDirective("render", new RenderDirective());
		addDirective("date", new DateDirective());
		addDirective("escape", new EscapeDirective());
		addDirective("string", new StringDirective());
		addDirective("random", new RandomDirective());
		
		// Add official shared method of Template Engine
		addSharedMethod(new Json());
	}
	
	/**
	 * Add shared function with file
	 */
	public void addSharedFunction(String fileName) {
		FileStringSource fileStringSource = new FileStringSource(baseTemplatePath, fileName, encoding);
		doAddSharedFunction(fileStringSource, fileName);
	}
	
	private synchronized void doAddSharedFunction(IStringSource stringSource, String fileName) {
		Env env = new Env(this);
		new Parser(env, stringSource.getContent(), fileName).parse();
		addToSharedFunctionMap(sharedFunctionMap, env);
		if (devMode) {
			sharedFunctionSourceList.add(stringSource);
			env.addStringSource(stringSource);
		}
	}
	
	/**
	 * Add shared function with files
	 */
	public void addSharedFunction(String... fileNames) {
		for (String fileName : fileNames) {
			addSharedFunction(fileName);
		}
	}
	
	/**
	 * Add shared function by string content
	 */
	public void addSharedFunctionByString(String content) {
		MemoryStringSource memoryStringSource = new MemoryStringSource(content);
		doAddSharedFunction(memoryStringSource, null);
	}
	
	/**
	 * Add shared function by IStringSource
	 */
	public void addSharedFunction(IStringSource stringSource) {
		String fileName = stringSource instanceof FileStringSource ? ((FileStringSource)stringSource).getFileName() : null;
		doAddSharedFunction(stringSource, fileName);
	}
	
	private void addToSharedFunctionMap(Map<String, Define> sharedFunctionMap, Env env) {
		Map<String, Define> funcMap = env.getFunctionMap();
		for (Entry<String, Define> e : funcMap.entrySet()) {
			if (sharedFunctionMap.containsKey(e.getKey())) {
				throw new IllegalArgumentException("Template function already exists : " + e.getKey());
			}
			Define func = e.getValue();
			if (devMode) {
				func.setEnvForDevMode(env);
			}
			sharedFunctionMap.put(e.getKey(), func);
		}
	}
	
	/**
	 * Get shared function by Env
	 */
	Define getSharedFunction(String functionName) {
		Define func = sharedFunctionMap.get(functionName);
		if (func == null) {
			/**
			 * 如果 func 最初未定义，但后续在共享模板文件中又被添加进来
			 * 此时在本 if 分支中无法被感知，仍然返回了 null
			 * 
			 * 但共享模板文件会在后续其它的 func 调用时被感知修改并 reload
			 * 所以本 if 分支不考虑处理模板文件中追加 #define 的情况
			 * 
			 * 如果要处理，只能是每次在 func 为 null 时，判断 sharedFunctionSourceList
			 * 中的模板是否被修改过，再重新加载，不优雅
			 */
			return null;
		}
		
		if (devMode && reloadModifiedSharedFunctionInDevMode) {
			if (func.isSourceModifiedForDevMode()) {
				synchronized (this) {
					func = sharedFunctionMap.get(functionName);
					if (func.isSourceModifiedForDevMode()) {
						reloadSharedFunctionSourceList();
						func = sharedFunctionMap.get(functionName);
					}
				}
			}
		}
		return func;
	}
	
	/**
	 * Reload shared function source list
	 * 
	 * devMode 要照顾到 sharedFunctionFiles，所以暂不提供
	 * removeSharedFunction(String functionName) 功能
	 * 开发者可直接使用模板注释功能将不需要的 function 直接注释掉
	 */
	private synchronized void reloadSharedFunctionSourceList() {
		Map<String, Define> newMap = new HashMap<String, Define>();
		for (int i = 0, size = sharedFunctionSourceList.size(); i < size; i++) {
			IStringSource ss = sharedFunctionSourceList.get(i);
			String fileName = ss instanceof FileStringSource ? ((FileStringSource)ss).getFileName() : null;
			
			Env env = new Env(this);
			new Parser(env, ss.getContent(), fileName).parse();
			addToSharedFunctionMap(newMap, env);
			if (devMode) {
				env.addStringSource(ss);
			}
		}
		this.sharedFunctionMap = newMap;
	}
	
	public synchronized void addSharedObject(String name, Object object) {
		if (sharedObjectMap == null) {
			sharedObjectMap = new HashMap<String, Object>();
		} else if (sharedObjectMap.containsKey(name)) {
			throw new IllegalArgumentException("Shared object already exists: " + name);
		}
		sharedObjectMap.put(name, object);
	}
	
	Map<String, Object> getSharedObjectMap() {
		return sharedObjectMap;
	}
	
	/**
	 * Set output directive factory
	 */
	public void setOutputDirectiveFactory(IOutputDirectiveFactory outputDirectiveFactory) {
		if (outputDirectiveFactory == null) {
			throw new IllegalArgumentException("outputDirectiveFactory can not be null");
		}
		this.outputDirectiveFactory = outputDirectiveFactory;
	}
	
	public Output getOutputDirective(ExprList exprList, Location location) {
		return outputDirectiveFactory.getOutputDirective(exprList, location);
	}
	
	/**
	 * Invoked by Engine only
	 */
	void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
	
	public boolean isDevMode() {
		return devMode;
	}
	
	public void setBaseTemplatePath(String baseTemplatePath) {
		if (StrKit.isBlank(baseTemplatePath)) {
			throw new IllegalArgumentException("baseTemplatePath can not be blank");
		}
		baseTemplatePath = baseTemplatePath.trim();
		if (baseTemplatePath.length() > 1) {
			if (baseTemplatePath.endsWith("/") || baseTemplatePath.endsWith("\\")) {
				baseTemplatePath = baseTemplatePath.substring(0, baseTemplatePath.length() - 1);
			}
		}
		this.baseTemplatePath = baseTemplatePath;
	}
	
	public String getBaseTemplatePath() {
		return baseTemplatePath;
	}
	
	public void setEncoding(String encoding) {
		if (StrKit.isBlank(encoding)) {
			throw new IllegalArgumentException("encoding can not be blank");
		}
		this.encoding = encoding;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setDatePattern(String datePattern) {
		if (StrKit.isBlank(datePattern)) {
			throw new IllegalArgumentException("datePattern can not be blank");
		}
		this.datePattern = datePattern;
	}
	
	public String getDatePattern() {
		return datePattern;
	}
	
	public void setReloadModifiedSharedFunctionInDevMode(boolean reloadModifiedSharedFunctionInDevMode) {
		this.reloadModifiedSharedFunctionInDevMode = reloadModifiedSharedFunctionInDevMode;
	}
	
	public synchronized void addDirective(String directiveName, Directive directive) {
		if (StrKit.isBlank(directiveName)) {
			throw new IllegalArgumentException("directive name can not be blank");
		}
		if (directive == null) {
			throw new IllegalArgumentException("directive can not be null");
		}
		if (directiveMap.containsKey(directiveName)) {
			throw new IllegalArgumentException("directive already exists : " + directiveName);
		}
		directiveMap.put(directiveName, directive);
	}
	
	public Stat getDirective(String directiveName) {
		return directiveMap.get(directiveName);
	}
	
	public void removeDirective(String directiveName) {
		directiveMap.remove(directiveName);
	}
	
	/**
	 * Add shared method from object
	 */
	public void addSharedMethod(Object sharedMethodFromObject) {
		sharedMethodKit.addSharedMethod(sharedMethodFromObject);
	}
	
	/**
	 * Add shared static method of Class
	 */
	public void addSharedStaticMethod(Class<?> sharedClass) {
		sharedMethodKit.addSharedStaticMethod(sharedClass);
	}
	
	/**
	 * Remove shared Method with method name
	 */
	public void removeSharedMethod(String methodName) {
		sharedMethodKit.removeSharedMethod(methodName);
	}
	
	/**
	 * Remove shared Method of the Class
	 */
	public void removeSharedMethod(Class<?> sharedClass) {
		sharedMethodKit.removeSharedMethod(sharedClass);
	}
	
	/**
	 * Remove shared Method
	 */
	public void removeSharedMethod(Method method) {
		sharedMethodKit.removeSharedMethod(method);
	}
	
	public SharedMethodKit getSharedMethodKit() {
		return sharedMethodKit;
	}
}





