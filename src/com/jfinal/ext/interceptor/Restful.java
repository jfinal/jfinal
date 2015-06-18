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

package com.jfinal.ext.interceptor;

import java.util.HashSet;
import java.util.Set;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * Invocation 中添加 Method method
 * 
	The standard definition is as follows:
	index - GET - A view of all (or a selection of) the records
	show - GET - A view of a single record
	add - GET - A form to post to create
	save - POST - Create a new record
	edit - GET - A form to edit a single record
	update - PUT - Update a record
	delete - DELETE - Delete a record
 * 
 * GET		/user			--->	index
 * GET		/user/id		--->	show  
 * GET		/user/add		--->	add
 * POST		/user			--->	save	
 * GET		/user/edit/id	--->	edit
 * PUT		/user/id		--->	update
 * DELETE	/user/id		--->	delete
 */
public class Restful implements Interceptor {
	
	private static final String isRestfulForwardKey = "_isRestfulForward_";
	private Set<String> set = new HashSet<String>() {
		private static final long serialVersionUID = 2717581127375143508L;{
		// add edit 与  JFinal 原有规则相同
		add("show");
		add("save");
		add("update");
		add("delete");
	}};
	
	/**
	 * add  edit 无需处理
	 * 
	 * GET		/user			--->	index
	 * GET		/user/id		--->	show  
	 * POST		/user			--->	save	
	 * PUT		/user/id		--->	update
	 * DELECT	/user/id		--->	delete
	 */
	public void intercept(Invocation inv) {
		// 阻止 JFinal 原有规则 action 请求
		Controller controller = inv.getController();
		Boolean isRestfulForward = controller.getAttr(isRestfulForwardKey);
		String methodName = inv.getMethodName();
		if (set.contains(methodName) && isRestfulForward== null) {
			inv.getController().renderError(404);
			return ;
		}
		
		if (isRestfulForward != null && isRestfulForward) {
			inv.invoke();
			return ;
		}
		
		String controllerKey = inv.getControllerKey();
		String method = controller.getRequest().getMethod().toUpperCase();
		String urlPara = controller.getPara();
		if ("GET".equals(method)) {
			if (urlPara != null && !"edit".equals(methodName)) {
				controller.setAttr(isRestfulForwardKey, Boolean.TRUE);
				controller.forwardAction(controllerKey + "/show/" + urlPara);
				return ;
			}
		}
		else if ("POST".equals(method)) {
			controller.setAttr(isRestfulForwardKey, Boolean.TRUE);
			controller.forwardAction(controllerKey + "/save");
			return ;
		}
		else if ("PUT".equals(method)) {
			controller.setAttr(isRestfulForwardKey, Boolean.TRUE);
			controller.forwardAction(controllerKey + "/update/" + urlPara);
			return ;
		}
		else if ("DELETE".equals(method)) {
			controller.setAttr(isRestfulForwardKey, Boolean.TRUE);
			controller.forwardAction(controllerKey + "/delete/" + urlPara);
			return ;
		}
		
		inv.invoke();
	}
}






