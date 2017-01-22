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

package com.jfinal.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.jfinal.core.Action;
import com.jfinal.core.Controller;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Invocation is used to invoke the interceptors and the target method
 */
@SuppressWarnings("unchecked")
public class Invocation {
	
	private Action action;
	private static final Object[] NULL_ARGS = new Object[0];	// Prevent new Object[0] by jvm for paras of action invocation.
	
	boolean useInjectTarget;
	private Object target;
	private Method method;
	private Object[] args;
	private MethodProxy methodProxy;
	private Interceptor[] inters;
	private Object returnValue = null;
	
	private int index = 0;
	
	// InvocationWrapper need this constructor
	protected Invocation() {
		this.action = null;
	}
	
	public Invocation(Action action, Controller controller) {
		this.action = action;
		this.inters = action.getInterceptors();
		this.target = controller;
		this.args = NULL_ARGS;
	}
	
	public Invocation(Object target, Method method, Object[] args, MethodProxy methodProxy, Interceptor[] inters) {
		this.action = null;
		this.target = target;
		this.method = method;
		this.args = args;
		this.methodProxy = methodProxy;
		this.inters = inters;
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
				// Invoke the method
				else {
					// if (!Modifier.isAbstract(method.getModifiers()))
						// returnValue = methodProxy.invokeSuper(target, args);
					if (useInjectTarget)
						returnValue = methodProxy.invoke(target, args);
					else
						returnValue = methodProxy.invokeSuper(target, args);
				}
			}
			catch (InvocationTargetException e) {
				Throwable t = e.getTargetException();
				throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(e);
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
	 * actionKey = controllerKey + methodName
	 */
	public String getActionKey() {
		if (action == null)
			throw new RuntimeException("This method can only be used for action interception");
		return action.getActionKey();
	}
	
	/**
	 * Return the controller key.
	 */
	public String getControllerKey() {
		if (action == null)
			throw new RuntimeException("This method can only be used for action interception");
		return action.getControllerKey();
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
