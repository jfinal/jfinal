package com.jfinal.ext.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;

/**
 * Accept POST method only.
 */
public class POST implements Interceptor {
	public void intercept(ActionInvocation ai) {
		Controller controller = ai.getController();
		if ("POST".equalsIgnoreCase(controller.getRequest().getMethod().toUpperCase()))
			ai.invoke();
		else
			controller.renderError404();
	}
}
