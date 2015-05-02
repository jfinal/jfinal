/**
 * NotFound Action Interceptor
 */
package com.jfinal.ext.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;

/**
 * @author 朱丛启  2015年4月29日 上午10:32:34
 *
 */
public class NotFoundActionInterceptor implements Interceptor {

	/**
	 * 参数处理
	 * 1. 参数为空时直接调用
	 * 2. 参数不为空时先判断是否使用？间隔，使用了？间隔先split一下参数，拿到？之前的参数，查找是否有这个action。没有就404，有就直接invoke
	 */
	@Override
	public void intercept(ActionInvocation ai) {
		// 获取controller
		Controller controller = ai.getController();
		// 获取controller 的参数
		String param = controller.getPara();
		if (param == null) {
			ai.invoke();
		} else {
			String[] params = null;
			if (param.contains("?")) {
				params = param.split("?");
			}
			if (params != null) {
				param = params[0];
			}

			try {
				controller.getClass().getMethod(param, null, null);
				ai.invoke();
			} catch (NoSuchMethodException e) {
				controller.renderError(404);
			} 
//			
//			boolean contained = false;
//			Method[] methods = controller.getClass().getMethods();
//			for (Method method : methods) {
//				if (param.equals(method.getName())) {
//					contained = true;
//					break;
//				}
//			}
//			if (contained){
//				ai.invoke();
//			}
//			else{
//				controller.renderError(404);
//			}
		}
	}
}
