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
import java.util.HashMap;
import java.util.Map;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.StrKit;
import com.jfinal.template.expr.ast.MethodKit;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.jfinal.template.source.ISource;
import com.jfinal.template.source.ISourceFactory;
import com.jfinal.template.source.StringSource;
import com.jfinal.template.stat.Parser;
import com.jfinal.template.stat.ast.Stat;

/**
 * Engine
 * 
 * Example：
 * Engine.use().getTemplate(fileName).render(...);
 * Engine.use().getTemplate(fileName).renderToString(...);
 */
public class Engine {
	
	public static final String MAIN_ENGINE_NAME = "main";
	
	private static Engine MAIN_ENGINE;
	private static Map<String, Engine> engineMap = new HashMap<String, Engine>();
	
	// Create main engine
	static {
		MAIN_ENGINE = new Engine(MAIN_ENGINE_NAME);
		engineMap.put(MAIN_ENGINE_NAME, MAIN_ENGINE);
	}
	
	private String name;
	private boolean devMode = false;
	private EngineConfig config = new EngineConfig();
	private ISourceFactory sourceFactory = config.getSourceFactory();
	
	private Map<String, Template> templateCache = new HashMap<String, Template>();
	
	/**
	 * Create engine without management of JFinal 
	 */
	public Engine() {
		this.name = "NO_NAME";
	}
	
	/**
	 * Create engine by engineName without management of JFinal 
	 */
	public Engine(String engineName) {
		this.name = engineName;
	}
	
	/**
	 * Using the main Engine
	 */
	public static Engine use() {
		return MAIN_ENGINE;
	}
	
	/**
	 * Using the engine with engine name
	 */
	public static Engine use(String engineName) {
		return engineMap.get(engineName);
	}
	
	/**
	 * Create engine with engine name managed by JFinal
	 */
	public synchronized static Engine create(String engineName) {
		if (StrKit.isBlank(engineName)) {
			throw new IllegalArgumentException("Engine name can not be blank");
		}
		engineName = engineName.trim();
		if (engineMap.containsKey(engineName)) {
			throw new IllegalArgumentException("Engine already exists : " + engineName);
		}
		Engine newEngine = new Engine(engineName);
		engineMap.put(engineName, newEngine);
		return newEngine;
	}
	
	/**
	 * Remove engine with engine name managed by JFinal
	 */
	public synchronized static Engine remove(String engineName) {
		Engine removed = engineMap.remove(engineName);
		if (removed != null && MAIN_ENGINE_NAME.equals(removed.name)) {
			Engine.MAIN_ENGINE = null;
		}
		return removed;
	}
	
	/**
	 * Set main engine
	 */
	public synchronized static void setMainEngine(Engine engine) {
		if (engine == null) {
			throw new IllegalArgumentException("Engine can not be null");
		}
		engine.name = Engine.MAIN_ENGINE_NAME;
		engineMap.put(Engine.MAIN_ENGINE_NAME, engine);
		Engine.MAIN_ENGINE = engine;
	}
	
	/**
	 * Get template with file name
	 */
	public Template getTemplate(String fileName) {
		if (fileName.charAt(0) != '/') {
			char[] arr = new char[fileName.length() + 1];
			fileName.getChars(0, fileName.length(), arr, 1);
			arr[0] = '/';
			fileName = new String(arr);
		}
		
		Template template = templateCache.get(fileName);
		if (template == null) {
			template = buildTemplateBySourceFactory(fileName);
			templateCache.put(fileName, template);
		} else if (devMode) {
			if (template.isModified()) {
				template = buildTemplateBySourceFactory(fileName);
				templateCache.put(fileName, template);
			}
		}
		return template;
	}
	
	private Template buildTemplateBySourceFactory(String fileName) {
		// FileSource fileSource = new FileSource(config.getBaseTemplatePath(), fileName, config.getEncoding());
		ISource source = sourceFactory.getSource(config.getBaseTemplatePath(), fileName, config.getEncoding());
		Env env = new Env(config);
		Parser parser = new Parser(env, source.getContent(), fileName);
		if (devMode) {
			env.addSource(source);
		}
		Stat stat = parser.parse();
		Template template = new Template(env, stat);
		return template;
	}
	
	/**
	 * Get template by string content and do not cache the template
	 */
	public Template getTemplateByString(String content) {
		return getTemplateByString(content, false);
	}
	
	/**
	 * Get template by string content
	 * 
	 * 重要：StringSource 中的 key = HashKit.md5(content)，也即 key
	 *     与 content 有紧密的对应关系，当 content 发生变化时 key 值也相应变化
	 *     因此，原先 key 所对应的 Template 缓存对象已无法被获取，当 getTemplateByString(String)
	 *     的 String 参数的数量不确定时会引发内存泄漏
	 *     
	 *     当 getTemplateByString(String, boolean) 中的 String 参数的
	 *     数量可控并且确定时，才可对其使用缓存 
	 *     
	 * @param content 模板内容
	 * @param cache true 则缓存 Template，否则不缓存
	 */
	public Template getTemplateByString(String content, boolean cache) {
		if (!cache) {
			return buildTemplateBySource(new StringSource(content, cache));
		}
		
		String key = HashKit.md5(content);
		Template template = templateCache.get(key);
		if (template == null) {
			template = buildTemplateBySource(new StringSource(content, cache));
			templateCache.put(key, template);
		} else if (devMode) {
			if (template.isModified()) {
				template = buildTemplateBySource(new StringSource(content, cache));
				templateCache.put(key, template);
			}
		}
		return template;
	}
	
	/**
	 * Get template with implementation of ISource
	 */
	public Template getTemplate(ISource source) {
		String key = source.getKey();
		if (key == null) {	// key 为 null 则不缓存，详见 ISource.getKey() 注释
			return buildTemplateBySource(source);
		}
		
		Template template = templateCache.get(key);
		if (template == null) {
			template = buildTemplateBySource(source);
			templateCache.put(key, template);
		} else if (devMode) {
			if (template.isModified()) {
				template = buildTemplateBySource(source);
				templateCache.put(key, template);
			}
		}
		return template;
	}
	
	private Template buildTemplateBySource(ISource source) {
		Env env = new Env(config);
		Parser parser = new Parser(env, source.getContent(), null);
		if (devMode) {
			env.addSource(source);
		}
		Stat stat = parser.parse();
		Template template = new Template(env, stat);
		return template;
	}
	
	/**
	 * Add shared function with file
	 */
	public Engine addSharedFunction(String fileName) {
		config.addSharedFunction(fileName);
		return this;
	}
	
	/**
	 * Add shared function by ISource
	 */
	public Engine addSharedFunction(ISource source) {
		config.addSharedFunction(source);
		return this;
	}
	
	/**
	 * Add shared function with files
	 */
	public Engine addSharedFunction(String... fileNames) {
		config.addSharedFunction(fileNames);
		return this;
	}
	
	/**
	 * Add shared function by string content
	 */
	public Engine addSharedFunctionByString(String content) {
		config.addSharedFunctionByString(content);
		return this;
	}
	
	/**
	 * Add shared object
	 */
	public Engine addSharedObject(String name, Object object) {
		config.addSharedObject(name, object);
		return this;
	}
	
	/**
	 * Set output directive factory
	 */
	public Engine setOutputDirectiveFactory(IOutputDirectiveFactory outputDirectiveFactory) {
		config.setOutputDirectiveFactory(outputDirectiveFactory);
		return this;
	}
	
	/**
	 * Add directive
	 * <pre>
	 * 示例：
	 * addDirective("now", NowDirective.class)
	 * </pre>
	 */
	public Engine addDirective(String directiveName, Class<? extends Directive> directiveClass) {
		config.addDirective(directiveName, directiveClass);
		return this;
	}
	
	/**
	 * 该方法已被 addDirective(String, Class<? extends Directive>) 所代替
	 */
	@Deprecated
	public Engine addDirective(String directiveName, Directive directive) {
		return addDirective(directiveName, directive.getClass());
	}
	
	/**
	 * Remove directive
	 */
	public Engine removeDirective(String directiveName) {
		config.removeDirective(directiveName);
		return this;
	}
	
	/**
	 * Add shared method from object
	 */
	public Engine addSharedMethod(Object sharedMethodFromObject) {
		config.addSharedMethod(sharedMethodFromObject);
		return this;
	}
	
	/**
	 * Add shared method from class
	 */
	public Engine addSharedMethod(Class<?> sharedMethodFromClass) {
		config.addSharedMethod(sharedMethodFromClass);
		return this;
	}
	
	/**
	 * Add shared static method of Class
	 */
	public Engine addSharedStaticMethod(Class<?> sharedStaticMethodFromClass) {
		config.addSharedStaticMethod(sharedStaticMethodFromClass);
		return this;
	}
	
	/**
	 * Remove shared Method with method name
	 */
	public Engine removeSharedMethod(String methodName) {
		config.removeSharedMethod(methodName);
		return this;
	}
	
	/**
	 * Remove shared Method of the Class
	 */
	public Engine removeSharedMethod(Class<?> clazz) {
		config.removeSharedMethod(clazz);
		return this;
	}
	
	/**
	 * Remove shared Method
	 */
	public Engine removeSharedMethod(Method method) {
		config.removeSharedMethod(method);
		return this;
	}
	
	/**
	 * Remove template cache with template key
	 */
	public void removeTemplateCache(String templateKey) {
		templateCache.remove(templateKey);
	}
	
	/**
	 * Remove all template cache
	 */
	public void removeAllTemplateCache() {
		templateCache.clear();
	}
	
	public int getTemplateCacheSize() {
		return templateCache.size();
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return "Template Engine: " + name;
	}
	
	// Engine config below ---------
	
	public EngineConfig  getEngineConfig() {
		return config;
	}
	
	/**
	 * 设置 true 为开发模式，支持模板文件热加载
	 * 设置 false 为生产模式，不支持模板文件热加载，以达到更高的性能
	 */
	public Engine setDevMode(boolean devMode) {
		this.devMode = devMode;
		this.config.setDevMode(devMode);
		if (this.devMode) {
			removeAllTemplateCache();
		}
		return this;
	}
	
	public boolean getDevMode() {
		return devMode;
	}
	
	/**
	 * 设置 ISourceFactory 用于为 engine 切换不同的 ISource 实现类
	 * ISource 用于从不同的来源加载模板内容
	 * 
	 * <pre>
	 * 配置为 ClassPathSourceFactory 时特别注意：
	 *    由于 JFinal 会在 configEngine(Engine me) 方法调用 “之前”，会默认调用一次如下方法：
	 *       me.setBaseTemplatePath(PathKit.getWebRootPath())
	 *    
	 *    而 ClassPathSourceFactory 在以上默认值下不能工作，所以需要通过如下方式清掉该值：
	 *       me.setBaseTemplatePath(null)
	 *    
	 *    或者配置具体要用的 baseTemplatePath 值，例如：
	 *       me.setBaseTemplatePath("view");
	 * </pre>
	 */
	public Engine setSourceFactory(ISourceFactory sourceFactory) {
		this.config.setSourceFactory(sourceFactory);	// 放第一行先进行参数验证
		this.sourceFactory = sourceFactory;
		return this;
	}
	
	/**
	 * 设置为 ClassPathSourceFactory 的快捷方法
	 */
	public Engine setToClassPathSourceFactory() {
		return setSourceFactory(new ClassPathSourceFactory());
	}
	
	public ISourceFactory getSourceFactory() {
		return sourceFactory;
	}
	
	public Engine setBaseTemplatePath(String baseTemplatePath) {
		config.setBaseTemplatePath(baseTemplatePath);
		return this;
	}
	
	public String getBaseTemplatePath() {
		return config.getBaseTemplatePath();
	}
	
	public Engine setDatePattern(String datePattern) {
		config.setDatePattern(datePattern);
		return this;
	}
	
	public String getDatePattern() {
		return config.getDatePattern();
	}
	
	public Engine setEncoding(String encoding) {
		config.setEncoding(encoding);
		return this;
	}
	
	public String getEncoding() {
		return config.getEncoding();
	}
	
	public Engine setWriterBufferSize(int bufferSize) {
		config.setWriterBufferSize(bufferSize);
		return this;
	}
	
	/**
	 * Engine 独立设置为 devMode 可以方便模板文件在修改后立即生效，
	 * 但如果在 devMode 之下并不希望对 addSharedFunction(...)，
	 * 添加的模板进行是否被修改的检测可以通过此方法设置 false 参进去
	 * 
	 * 注意：Engine 在生产环境下(devMode 为 false)，该参数无效
	 */
	public Engine setReloadModifiedSharedFunctionInDevMode(boolean reloadModifiedSharedFunctionInDevMode) {
		config.setReloadModifiedSharedFunctionInDevMode(reloadModifiedSharedFunctionInDevMode);
		return this;
	}
	
	public static void addExtensionMethod(Class<?> targetClass, Object objectOfExtensionClass) {
		MethodKit.addExtensionMethod(targetClass, objectOfExtensionClass);
	}
	
	public static void addExtensionMethod(Class<?> targetClass, Class<?> extensionClass) {
		MethodKit.addExtensionMethod(targetClass, extensionClass);
	}
	
	public static void removeExtensionMethod(Class<?> targetClass, Object objectOfExtensionClass) {
		MethodKit.removeExtensionMethod(targetClass, objectOfExtensionClass);
	}
	
	public static void removeExtensionMethod(Class<?> targetClass, Class<?> extensionClass) {
		MethodKit.removeExtensionMethod(targetClass, extensionClass);
	}
}





