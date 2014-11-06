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

package com.jfinal.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.jfinal.aop.Interceptor;

/**
 * ActionInvocation invoke the action
 */
public class ActionInvocation {
	
	private Controller controller;
	private Interceptor[] inters;
	private Action action;
	private int index = 0;
	
	private static final Object[] NULL_ARGS = new Object[0];	// Prevent new Object[0] by jvm for paras of action invocation.
	
	// ActionInvocationWrapper need this constructor
	protected ActionInvocation() {
		
	}
	
	ActionInvocation(Action action, Controller controller) {
		this.controller = controller;
		this.inters = action.getInterceptors();
		this.action = action;
	}
	
	/**
	 * Invoke the action.
	 */
	public void invoke() {
		if (index < inters.length)
			inters[index++].intercept(this);
		else if (index++ == inters.length)	// index++ ensure invoke action only one time
			// try {action.getMethod().invoke(controller, NULL_ARGS);} catch (Exception e) {throw new RuntimeException(e);}
			try {
				action.getMethod().invoke(controller, NULL_ARGS);
			}
			catch (InvocationTargetException e) {
				Throwable cause = e.getTargetException();
				if (cause instanceof RuntimeException)
					throw (RuntimeException)cause;
				throw new RuntimeException(e);
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
	}
	
	/**
	 * Return the controller of this action.
	 */
	public Controller getController() {
		return controller;
	}
	
	/**
	 * Return the action key.
	 * actionKey = controllerKey + methodName
	 */
	public String getActionKey() {
		return action.getActionKey();
	}
	
	/**
	 * Return the controller key.
	 */
	public String getControllerKey() {
		return action.getControllerKey();
	}
	
	/**
	 * Return the method of this action.
	 * <p>
	 * You can getMethod.getAnnotations() to get annotation on action method to do more things
	 */
	public Method getMethod() {
		return action.getMethod();
		/*
		try {
			return controller.getClass().getMethod(action.getMethod().getName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}*/
	}
	
	/**
	 * Return the method name of this action's method.
	 */
	public String getMethodName() {
		return action.getMethodName();
	}
	
	/**
	 * Return view path of this controller.
	 */
	public String getViewPath() {
		return action.getViewPath();
	}
}
