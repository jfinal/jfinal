package com.jfinal.ext.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;

/**
 * Accept GET method only.
 */
public class GET implements Interceptor {
	public void intercept(ActionInvocation ai) {
		Controller controller = ai.getController();
		if ("GET".equalsIgnoreCase(controller.getRequest().getMethod()))
			ai.invoke();
		else
			controller.renderError404();
	}
}
