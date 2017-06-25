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

import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import com.jfinal.template.Engine;

/**
 * 关键设置：
 * 1：setBaseTemplatePath(path) 设置模板文件所在的基础路径
 * 2：setDevMode(true) 设置模板文件被修改后的热加载
 * 3：setSessionInView(true) 设置在模板中可通过 #(session.value) 访问 session 中的数据
 * 3：setCreateSession(boolean) 用来设置 request.getSession(boolean) 调时的参数
 */
public class JFinalViewResolver extends AbstractTemplateViewResolver {
	
	public static final Engine engine = new Engine();
	
	static boolean sessionInView = false;
	static boolean createSession = true;
	
	public Engine getEngine() {
		return engine;
	}
	
	public void setDevMode(boolean devMode) {
		engine.setDevMode(devMode);
	}
	
	public void setBaseTemplatePath(String baseTemplatePath) {
		engine.setBaseTemplatePath(baseTemplatePath);
	}
	
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
	
	// ---------------------------------------------------------------
	
	public JFinalViewResolver() {
		setViewClass(requiredViewClass());
		// setSuffix(".html");
        // setContentType("text/html;charset=UTF-8");
        // setOrder(0);
	}
	
	@Override
	protected Class<?> requiredViewClass() {
		return JFinalView.class;
	}
}







