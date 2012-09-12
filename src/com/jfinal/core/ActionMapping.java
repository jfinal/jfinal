/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.jfinal.aop.Interceptor;
import com.jfinal.config.Interceptors;
import com.jfinal.config.Routes;

/**
 * ActionMapping
 */
final class ActionMapping {
	
	private static final String SLASH = "/";
	private Routes routes;
	private Interceptors interceptors;
	
	private final Map<String, Action> actionMapping = new HashMap<String, Action>();
	
	ActionMapping(Routes routes, Interceptors interceptors) {
		this.routes = routes;
		this.interceptors = interceptors;
	}
	
	private Set<String> buildExcludedMethodName() {
		Set<String> excludedMethodName = new HashSet<String>();
		Method[] methods = Controller.class.getMethods();
		for (Method m : methods) {
			if (m.getParameterTypes().length == 0)
				excludedMethodName.add(m.getName());
		}
		return excludedMethodName;
	}
	
	void buildActionMapping() {
		Set<String> excludedMethodName = buildExcludedMethodName();
		InterceptorBuilder interceptorBuilder = new InterceptorBuilder();
		Interceptor[] defaultInters = interceptors.getInterceptorArray();
		interceptorBuilder.addToInterceptorsMap(defaultInters);
		for (Entry<String, Class<? extends Controller>> entry : routes.getEntrySet()) {
			Class<? extends Controller> controllerClass = entry.getValue();
			Interceptor[] controllerInters = interceptorBuilder.buildControllerInterceptors(controllerClass);
			Method[] methods = controllerClass.getMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				if (!excludedMethodName.contains(methodName) && method.getParameterTypes().length == 0) {
					Interceptor[] methodInters = interceptorBuilder.buildMethodInterceptors(method);
					Interceptor[] actionInters = interceptorBuilder.buildActionInterceptors(defaultInters, controllerInters, controllerClass, methodInters, method);
					String controllerKey = entry.getKey();
					
					if (methodName.equals("index")) {
						String actionKey = controllerKey;
						
						Action action = new Action(controllerKey, actionKey, controllerClass, method, methodName, actionInters, routes.getViewPath(controllerKey));
						action = actionMapping.put(actionKey, action);
						
						if (action != null) {
							warnning(action.getActionKey(), action.getControllerClass(), action.getMethod());
						}
					}
					else {
						String actionKey = controllerKey.equals(SLASH) ? SLASH + methodName : controllerKey + SLASH + methodName;
						
						if (actionMapping.containsKey(actionKey)) {
							warnning(actionKey, controllerClass, method);
							continue;
						}
						
						Action action = new Action(controllerKey, actionKey, controllerClass, method, methodName, actionInters, routes.getViewPath(controllerKey));
						actionMapping.put(actionKey, action);
					}
				}
			}
		}
		
		// support url = controllerKey + urlParas with "/" of controllerKey
		Action actoin = actionMapping.get("/");
		if (actoin != null)
			actionMapping.put("", actoin);
	}
	
	private static final void warnning(String actionKey, Class<? extends Controller> controllerClass, Method method) {
		StringBuilder sb = new StringBuilder();
		sb.append("--------------------------------------------------------------------------------\nWarnning!!!\n")
		.append("ActionKey already used: \"").append(actionKey).append("\" \n") 
		.append("Action can not be mapped: \"")
		.append(controllerClass.getName()).append(".").append(method.getName()).append("()\" \n")
		.append("--------------------------------------------------------------------------------");
		System.out.println(sb.toString());
	}
	
	/**
	 * Support four types of url
	 * 1: http://abc.com/controllerKey                 ---> 00
	 * 2: http://abc.com/controllerKey/para            ---> 01
	 * 3: http://abc.com/controllerKey/method          ---> 10
	 * 4: http://abc.com/controllerKey/method/para     ---> 11
	 */
	Action getAction(String url, String[] urlPara) {
		Action action = actionMapping.get(url);
		if (action != null) {
			return action;
		}
		
		// --------
		int i = url.lastIndexOf(SLASH);
		if (i != -1) {
			action = actionMapping.get(url.substring(0, i));
			urlPara[0] = url.substring(i + 1);
		}
		
		return action;
	}
	
	List<String> getAllActionKeys() {
		List<String> allActionKeys = new ArrayList<String>(actionMapping.keySet());
		Collections.sort(allActionKeys);
		return allActionKeys;
	}
}





