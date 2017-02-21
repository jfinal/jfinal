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
	
	@SuppressWarnings("serial")
	private Set<String> set = new HashSet<String>() {{
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
		String methodName = ai.getMethodName();
		if (set.contains(methodName)) {
			ai.getController().renderError404();
			return ;
		}
		
		Controller controller = ai.getController();
		String controllerKey = ai.getControllerKey();
		String method = controller.getRequest().getMethod().toUpperCase();
		String urlPara = controller.getPara();
		if ("GET".equals(method)) {
			if (urlPara != null) {
				controller.forwardAction(controllerKey + "/show/" + urlPara);
				return ;
			}
		}
		else if ("POST".equals(method)) {
			controller.forwardAction(controllerKey + "/save");
			return ;
		}
		else if ("PUT".equals(method)) {
			controller.forwardAction(controllerKey + "/update/" + urlPara);
			return ;
		}
		else if ("DELETE".equals(method)) {
			controller.forwardAction(controllerKey + "/delete/" + urlPara);
			return ;
		}
		
		ai.invoke();
	}
}






