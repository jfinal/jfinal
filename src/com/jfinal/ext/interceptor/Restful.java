package com.jfinal.ext.interceptor;

import java.util.HashSet;
import java.util.Set;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;

/**
 * ActionInvocation 中添加 Method method
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
 * DELECT	/user/id		--->	delete
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
	public void intercept(ActionInvocation ai) {
		// 阻止 JFinal 原有规则 action 请求
		Controller controller = ai.getController();
		Boolean isRestfulForward = controller.getAttr(isRestfulForwardKey);
		String methodName = ai.getMethodName();
		if (set.contains(methodName) && isRestfulForward== null) {
			ai.getController().renderError(404);
			return ;
		}
		
		if (isRestfulForward != null && isRestfulForward) {
			ai.invoke();
			return ;
		}
		
		String controllerKey = ai.getControllerKey();
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
		
		ai.invoke();
	}
}






