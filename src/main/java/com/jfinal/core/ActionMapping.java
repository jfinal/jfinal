/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.InterceptorManager;
import com.jfinal.config.Routes;
import com.jfinal.config.Routes.Route;

/**
 * ActionMapping
 */
public class ActionMapping {
	
	protected static final String SLASH = "/";
	
	protected Routes routes;
	protected Map<String, Action> mapping = new HashMap<String, Action>(2048, 0.5F);
	
	public ActionMapping(Routes routes) {
		this.routes = routes;
	}
	
	protected List<Routes> getRoutesList() {
		List<Routes> routesList = Routes.getRoutesList();
		List<Routes> ret = new ArrayList<Routes>(routesList.size() + 1);
		ret.add(routes);
		ret.addAll(routesList);
		return ret;
	}
	
	protected void buildActionMapping() {
		mapping.clear();
		Class<?> dc;
		InterceptorManager interMan = InterceptorManager.me();
		for (Routes routes : getRoutesList()) {
		for (Route route : routes.getRouteItemList()) {
			Class<? extends Controller> controllerClass = route.getControllerClass();
			Interceptor[] controllerInters = interMan.createControllerInterceptor(controllerClass);
			
			boolean declaredMethods = routes.getMappingSuperClass()
					? controllerClass.getSuperclass() == Controller.class
					: true;
			
			Method[] methods = (declaredMethods ? controllerClass.getDeclaredMethods() : controllerClass.getMethods());
			for (Method method : methods) {
				if (declaredMethods) {
					if (!Modifier.isPublic(method.getModifiers()))
						continue ;
				} else {
					dc = method.getDeclaringClass();
					if (dc == Controller.class || dc == Object.class)
						continue ;
				}
				
				if (method.getAnnotation(NotAction.class) != null) {
					continue ;
				}
				
				Interceptor[] actionInters = interMan.buildControllerActionInterceptor(routes.getInterceptors(), controllerInters, controllerClass, method);
				String controllerKey = route.getControllerKey();
				
				String methodName = method.getName();
				ActionKey ak = method.getAnnotation(ActionKey.class);
				String actionKey;
				if (ak != null) {
					actionKey = ak.value().trim();
					if ("".equals(actionKey))
						throw new IllegalArgumentException(controllerClass.getName() + "." + methodName + "(): The argument of ActionKey can not be blank.");
					
					if (!actionKey.startsWith(SLASH))
						actionKey = SLASH + actionKey;
				}
				else if (methodName.equals("index")) {
					actionKey = controllerKey;
				}
				else {
					actionKey = controllerKey.equals(SLASH) ? SLASH + methodName : controllerKey + SLASH + methodName;
				}
				
				Action action = new Action(controllerKey, actionKey, controllerClass, method, methodName, actionInters, route.getFinalViewPath(routes.getBaseViewPath()));
				if (mapping.put(actionKey, action) != null) {
					throw new RuntimeException(buildMsg(actionKey, controllerClass, method));
				}
			}
		}
		}
		routes.clear();
		
		// support url = controllerKey + urlParas with "/" of controllerKey
		Action action = mapping.get("/");
		if (action != null) {
			mapping.put("", action);
		}
	}
	
	protected String buildMsg(String actionKey, Class<? extends Controller> controllerClass, Method method) {
		StringBuilder sb = new StringBuilder("The action \"")
			.append(controllerClass.getName()).append(".")
			.append(method.getName()).append("()\" can not be mapped, ")
			.append("actionKey \"").append(actionKey).append("\" is already in use.");
		
		String msg = sb.toString();
		System.err.println("\nException: " + msg);
		return msg;
	}
	
	/**
	 * Support four types of url
	 * 1: http://abc.com/controllerKey                 ---> 00
	 * 2: http://abc.com/controllerKey/para            ---> 01
	 * 3: http://abc.com/controllerKey/method          ---> 10
	 * 4: http://abc.com/controllerKey/method/para     ---> 11
	 * The controllerKey can also contains "/"
	 * Example: http://abc.com/uvw/xyz/method/para
	 */
	public Action getAction(String url, String[] urlPara) {
		Action action = mapping.get(url);
		if (action != null) {
			return action;
		}
		
		// --------
		int i = url.lastIndexOf('/');
		if (i != -1) {
			action = mapping.get(url.substring(0, i));
			if (action != null) {
				urlPara[0] = url.substring(i + 1);
			}
		}
		
		return action;
	}
	
	public List<String> getAllActionKeys() {
		List<String> allActionKeys = new ArrayList<String>(mapping.keySet());
		Collections.sort(allActionKeys);
		return allActionKeys;
	}
}







