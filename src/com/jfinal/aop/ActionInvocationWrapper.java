/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.aop;

import java.lang.reflect.Method;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;

/**
 * ActionInvocationWrapper invoke the InterceptorStack.
 */
class ActionInvocationWrapper extends ActionInvocation {
	
	private Interceptor[] inters;
	private ActionInvocation actionInvocation;
	private int index = 0;
	
	ActionInvocationWrapper(ActionInvocation actionInvocation, Interceptor[] inters) {
		this.actionInvocation = actionInvocation;
		this.inters = inters;
	}
	
	/**
	 * Invoke the action
	 */
	@Override
	public final void invoke() {
		if (index < inters.length)
			inters[index++].intercept(this);
		else if (index++ == inters.length)
			actionInvocation.invoke();
	}
	
	@Override
	public Controller getController() {
		return actionInvocation.getController();
	}
	
	@Override
	public String getActionKey() {
		return actionInvocation.getActionKey();
	}
	
	@Override
	public String getControllerKey() {
		return actionInvocation.getControllerKey();
	}
	
	@Override
	public Method getMethod() {
		return actionInvocation.getMethod();
	}
	
	@Override
	public String getMethodName() {
		return actionInvocation.getMethodName();
	}
	
	/**
	 * Return view path of this controller
	 */
	@Override
	public String getViewPath() {
		return actionInvocation.getViewPath();
	}
	
	/*
	 * It should be added method below when com.jfinal.core.ActionInvocation add method, otherwise null will be returned.
	 */
}







