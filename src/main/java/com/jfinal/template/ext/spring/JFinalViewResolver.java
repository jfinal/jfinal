package com.jfinal.template.ext.spring;

import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import com.jfinal.template.Engine;

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







