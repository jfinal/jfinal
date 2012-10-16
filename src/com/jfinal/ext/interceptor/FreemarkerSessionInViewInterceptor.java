package com.jfinal.ext.interceptor;

import javax.servlet.http.HttpSession;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;

import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.ObjectWrapper;

/**
 * Freemarker作为视图时，Session注入拦截器
 * @author wen3062@qq.com
 *
 */
public class FreemarkerSessionInViewInterceptor implements Interceptor {
	private boolean createSession = false;
	
	public FreemarkerSessionInViewInterceptor(boolean createSession)
	{
		this.createSession = createSession;
	}
	
	@Override
	public void intercept(ActionInvocation ai) {
		ai.invoke();

		Controller c = ai.getController();
		HttpSession hs = c.getSession(createSession);
		if (hs != null) {
			c.setAttr("session", new HttpSessionHashModel(hs,
					ObjectWrapper.DEFAULT_WRAPPER));
		}
	}
}
