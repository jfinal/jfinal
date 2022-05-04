/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.jfinal.proxy.Callback;
import com.jfinal.proxy.ProxyMethod;
import com.jfinal.proxy.ProxyMethodCache;
import com.jfinal.core.Action;
import com.jfinal.core.Controller;

/**
 * Invocation is used to invoke the interceptors and the target method
 */
@SuppressWarnings("unchecked")
public class Invocation {
	
	private static final Object[] NULL_ARGS = new Object[0];	// Prevent new Object[0] by jvm for args of method invoking
	
	private Action action;
	private Object target;
	private Method method;
	private Object[] args;
	private Callback callback;
	private Interceptor[] inters;
	private Object returnValue;
	
	private int index = 0;
	
	public Invocation(Object target, Long proxyMethodKey, Callback callback, Object... args) {
		this.action = null;
		this.target = target;
		
		ProxyMethod proxyMethod = ProxyMethodCache.get(proxyMethodKey);
		this.method = proxyMethod.getMethod();
		this.inters = proxyMethod.getInterceptors();
		
		this.callback = callback;
		this.args = args;
	}
	
	public Invocation(Object target, Long proxyMethodKey, Callback callback) {
		this(target, proxyMethodKey, callback, NULL_ARGS);
	}
	
	/**
	 * 用于扩展 ProxyFactory
	 */
	public Invocation(Object target, Method method, Interceptor[] inters, Callback callback, Object[] args) {
		this.action = null;
		this.target = target;
		
		this.method = method;
		this.inters = inters;
		
		this.callback = callback;
		this.args = args;
	}
	
	// InvocationWrapper need this constructor
	protected Invocation() {
		this.action = null;
	}
	
	public Invocation(Action action, Controller controller) {
		this.action = action;
		this.inters = action.getInterceptors();
		this.target = controller;
		
		// this.args = NULL_ARGS;
		this.args = action.getParameterGetter().get(action, controller);
	}
	
	public void invoke() {
		if (index < inters.length) {
			inters[index++].intercept(this);
		}
		else if (index++ == inters.length) {	// index++ ensure invoke action only one time
			try {
				// Invoke the action
				if (action != null) {
					returnValue = action.getMethod().invoke(target, args);
				}
				// Invoke the callback
				else {
					returnValue = callback.call(args);
				}
			}
			catch (InvocationTargetException e) {
				Throwable t = e.getTargetException();
				if (t == null) {t = e;}
				throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(t);
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
	}
	
	public Object getArg(int index) {
		if (index >= args.length)
			throw new ArrayIndexOutOfBoundsException();
		return args[index];
	}
	
	public void setArg(int index, Object value) {
		if (index >= args.length)
			throw new ArrayIndexOutOfBoundsException();
		args[index] = value;
	}
	
	public Object[] getArgs() {
		return args;
	}
	
	/**
	 * Get the target object which be intercepted
	 * <pre>
	 * Example:
	 * OrderService os = getTarget();
	 * </pre>
	 */
	public <T> T getTarget() {
		return (T)target;
	}
	
	/**
	 * Return the method of this action.
	 * <p>
	 * You can getMethod.getAnnotations() to get annotation on action method to do more things
	 */
	public Method getMethod() {
		if (action != null)
			return action.getMethod();
		return method;
	}
	
	/**
	 * Return the method name of this action's method.
	 */
	public String getMethodName() {
		if (action != null)
			return action.getMethodName();
		return method.getName();
	}
	
	/**
	 * Get the return value of the target method
	 */
	public <T> T getReturnValue() {
		return (T)returnValue;
	}
	
	/**
	 * Set the return value of the target method
	 */
	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}
	
	// ---------
	
	/**
	 * Return the controller of this action.
	 */
	public Controller getController() {
		if (action == null)
			throw new RuntimeException("This method can only be used for action interception");
		return (Controller)target;
	}
	
	/**
	 * Return the action key.
	 * actionKey = controllerPath + methodName
	 */
	public String getActionKey() {
		if (action == null)
			throw new RuntimeException("This method can only be used for action interception");
		return action.getActionKey();
	}
	
	/**
	 * Return the controller path.
	 */
	public String getControllerPath() {
		if (action == null)
			throw new RuntimeException("This method can only be used for action interception");
		return action.getControllerPath();
	}
	
	/**
	 * 该方法已改名为 getControllerPath()
	 */
	@Deprecated
	public String getControllerKey() {
		return getControllerPath();
	}
	
	/**
	 * Return view path of this controller.
	 */
	public String getViewPath() {
		if (action == null)
			throw new RuntimeException("This method can only be used for action interception");
		return action.getViewPath();
	}
	
	/**
	 * return true if it is action invocation.
	 */
	public boolean isActionInvocation() {
		return action != null;
	}
}
