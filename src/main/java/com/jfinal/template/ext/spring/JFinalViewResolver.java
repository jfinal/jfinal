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

package com.jfinal.template.ext.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import com.jfinal.kit.StrKit;
import com.jfinal.template.Directive;
import com.jfinal.template.Engine;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.jfinal.template.source.ISourceFactory;

/**
 * JFinalViewResolver
 * 
 * <pre>
 * 关键配置：
 * 1：setDevMode(true) 设置支持热加载模板文件
 * 
 * 2：addSharedFunction(file) 添加共享函数文件
 * 
 * 3：setSourceFactory(new ClassPathSourceFactory())，从 class path 与 jar 包中加载模板文件
 *    一般用于 sprint boot
 * 
 * 4：setSessionInView(true) 设置在模板中可通过 #(session.value) 访问 session 中的数据
 * 
 * 5：setCreateSession(boolean) 用来设置 request.getSession(boolean) 调时的参数
 * 
 * 6：setBaseTemplatePath(path) 设置模板文件所在的基础路径，通常用于 spring mvc
 *   默认值为 web 根路径，一般不需要设置
 * </pre>
 */
public class JFinalViewResolver extends AbstractTemplateViewResolver {
	
	public static final Engine engine = new Engine();
	
	static List<String> sharedFunctionFiles = new ArrayList<String>();
	static boolean sessionInView = false;
	static boolean createSession = true;
	
	private static JFinalViewResolver me = null;
	
	/**
	 * me 会保存在第一次被创建对象
	 */
	public static JFinalViewResolver me() {
		return me;
	}
	
	public Engine getEngine() {
		return engine;
	}
	
	/**
	 * 设置开发模式，值为 true 时支持模板文件热加载
	 */
	public void setDevMode(boolean devMode) {
		engine.setDevMode(devMode);
	}
	
	/**
	 * 设置 shared function 文件，多个文件用逗号分隔
	 * 
	 * 主要用于 Spring MVC 的 xml 配置方式
	 * 
	 * Spring Boot 的代码配置方式可使用 addSharedFunction(...) 进行配置
	 */
	public void setSharedFunction(String sharedFunctionFiles) {
		if (StrKit.isBlank(sharedFunctionFiles)) {
			throw new IllegalArgumentException("sharedFunctionFiles can not be blank");
		}
		
		String[] fileArray = sharedFunctionFiles.split(",");
		for (String fileName : fileArray) {
			JFinalViewResolver.sharedFunctionFiles.add(fileName);
		}
	}
	
	/**
	 * 通过 List 配置多个 shared function file
	 * <pre>
	 * 配置示例：
	 * 	<property name="sharedFunctionList">
	 *     	<list>
	 *     		<value>_layout.html</value>
	 *     		<value>_paginate.html</value>
	 *     	</list>
	 * 	</property>
	 * </pre>
	 */
	public void setSharedFunctionList(List<String> sharedFunctionList) {
		if (sharedFunctionList != null) {
			JFinalViewResolver.sharedFunctionFiles.addAll(sharedFunctionList);
		}
	}
	
	/**
	 * 添加 shared function 文件，可调用多次添加多个文件
	 */
	public void addSharedFunction(String fileName) {
		// 等待 SourceFactory、baseTemplatePath 配置到位，利用 sharedFunctionFiles 实现延迟加载
		sharedFunctionFiles.add(fileName);
	}
	
	/**
	 * 添加自定义指令
	 */
	public void addDirective(String directiveName, Class<? extends Directive> directiveClass) {
		engine.addDirective(directiveName, directiveClass);
	}
	
	/**
	 * 添加自定义指令，已被 addDirective(String, Class<? extends Directive>) 方法取代
	 */
	@Deprecated
	public void addDirective(String directiveName, Directive directive) {
		addDirective(directiveName, directive.getClass());
	}
	
	/**
	 * 添加共享对象
	 */
	public void addSharedObject(String name, Object object) {
		engine.addSharedObject(name, object);
	}
	
	/**
	 * 添加共享方法
	 */
	public void addSharedMethod(Object sharedMethodFromObject) {
		engine.addSharedMethod(sharedMethodFromObject);
	}
	
	/**
	 * 添加共享方法
	 */
	public void addSharedMethod(Class<?> sharedMethodFromClass) {
		engine.addSharedMethod(sharedMethodFromClass);
	}
	
	/**
	 * 添加扩展方法
	 */
	public static void addExtensionMethod(Class<?> targetClass, Object objectOfExtensionClass) {
		Engine.addExtensionMethod(targetClass, objectOfExtensionClass);
	}
	
	/**
	 * 添加扩展方法
	 */
	public static void addExtensionMethod(Class<?> targetClass, Class<?> extensionClass) {
		Engine.addExtensionMethod(targetClass, extensionClass);
	}
	
	/**
	 * 设置 ISourceFactory 用于为 engine 切换不同的 ISource 实现类
	 * 
	 * <pre>
	 * 配置为 ClassPathSourceFactory 时特别注意：
	 *    由于在 initServletContext() 通过如下方法中已设置了 baseTemplatePath 值：
	 *        setBaseTemplatePath(servletContext.getRealPath("/"))
	 *    
	 *    而 ClassPathSourceFactory 在 initServletContext() 方法中设置的
	 *    值之下不能工作，所以在本方法中通过如下方法清掉了该值：
	 *         setBaseTemplatePath(null)
	 *    
	 *    这种处理方式适用于绝大部分场景，如果在使用 ClassPathSourceFactory 的同时
	 *    仍然需要设置 baseTemplatePath，则在调用该方法 “之后” 通过如下代码再次配置：
	 *         setBaseTemplatePath(value)
	 * </pre>
	 */
	public void setSourceFactory(ISourceFactory sourceFactory) {
		if (sourceFactory instanceof ClassPathSourceFactory) {
			engine.setBaseTemplatePath(null);
		}
		engine.setSourceFactory(sourceFactory);
	}
	
	/**
	 * 设置模板基础路径
	 */
	public void setBaseTemplatePath(String baseTemplatePath) {
		engine.setBaseTemplatePath(baseTemplatePath);
	}
	
	/**
	 * 设置为 true 时支持在模板中使用 #(session.value) 形式访问 session 中的数据
	 */
	public void setSessionInView(boolean sessionInView) {
		JFinalViewResolver.sessionInView = sessionInView;
	}
	
	/**
	 * 在使用 request.getSession(createSession) 时传入
	 * 用来指示 session 不存在时是否立即创建
	 */
	public void setCreateSession(boolean createSession) {
		JFinalViewResolver.createSession = createSession;
	}
	
	/**
	 * 设置 encoding
	 */
	public void setEncoding(String encoding) {
		engine.setEncoding(encoding);
	}
	
	/**
	 * 设置 #date(...) 指令，对于 Date、Timestamp、Time 的输出格式
	 */
	public void setDatePattern(String datePattern) {
		engine.setDatePattern(datePattern);
	}
	
	// ---------------------------------------------------------------
	
	public JFinalViewResolver() {
		synchronized(JFinalViewResolver.class) {
			if (me == null) {
				me = this;
			}
		}
		
		setViewClass(requiredViewClass());
		setOrder(0);
		setContentType("text/html;charset=UTF-8");
		// setPrefix("/view/");
		// setSuffix(".html");
	}
	
	@Override
	protected Class<?> requiredViewClass() {
		return JFinalView.class;
	}
	
	/**
	 * 支持 jfinal enjoy、jsp、freemarker、velocity 四类模板共存于一个项目中
	 * 
	 * 注意：这里采用识别 ".jsp"、".ftl"、".vm" 模板后缀名的方式来实现功能
	 *     所以 jfinal enjoy 模板不要采用上述三种后缀名，否则功能将失效
	 *     还要注意与 jsp、freemarker、velocity 以外类型模板共存使用时
	 *     需要改造该方法
	 */
	protected View loadView(String viewName, Locale locale) throws Exception {
		String suffix = getSuffix();
		if (".jsp".equals(suffix) || ".ftl".equals(suffix) || ".vm".equals(suffix)) {
			return null;
		} else {
			return super.loadView(viewName, locale);
		}
	}
	
	/**
	 * spring 回调，利用 ServletContext 做必要的初始化工作
	 */
	@Override
	protected void initServletContext(ServletContext servletContext) {
		super.initServletContext(servletContext);
		super.setExposeRequestAttributes(true);
		
		initBaseTemplatePath(servletContext);
		initSharedFunction();
	}
	
	/**
	 * 初始化 baseTemplatePath 值，启用 ClassPathSourceFactory 时
	 * 无需设置 baseTemplatePath 为 web 根路径
	 */
	private void initBaseTemplatePath(ServletContext servletContext) {
		if (engine.getSourceFactory() instanceof ClassPathSourceFactory) {
			// do nothing
		} else {
			if (StrKit.isBlank(engine.getBaseTemplatePath())) {
				String path = servletContext.getRealPath("/");
				engine.setBaseTemplatePath(path);
			}
		}
	}
	
	/**
	 * 利用 sharedFunctionFiles 延迟调用 addSharedFunction
	 * 因为需要等待 baseTemplatePath 以及 ISourceFactory 设置完毕以后
	 * 才能正常工作
	 */
	private void initSharedFunction() {
		for (String file : sharedFunctionFiles) {
			engine.addSharedFunction(file.trim());
		}
	}
}







