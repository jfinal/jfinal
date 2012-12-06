package com.jfinal.ext.plugin.shiro;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;

public class ShiroInterceptor implements Interceptor {
	
	public void intercept(ActionInvocation ai) {
		throw new RuntimeException("Not finish!!!");
	}
}
