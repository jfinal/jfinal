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

package com.jfinal.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.config.Constants;
import com.jfinal.aop.Invocation;
import com.jfinal.handler.Handler;
import com.jfinal.log.Log;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;
import com.jfinal.render.RenderManager;

/**
 * ActionHandler
 */
public class ActionHandler extends Handler {
	
	protected boolean devMode;
	protected ActionMapping actionMapping;
	protected ControllerFactory controllerFactory;
	protected static final RenderManager renderManager = RenderManager.me();
	private static final Log log = Log.getLog(ActionHandler.class);
	
	protected void init(ActionMapping actionMapping, Constants constants) {
		this.actionMapping = actionMapping;
		this.devMode = constants.getDevMode();
		this.controllerFactory = constants.getControllerFactory();
	}
	
	/**
	 * handle
	 * 1: Action action = actionMapping.getAction(target)
	 * 2: new Invocation(...).invoke()
	 * 3: render(...)
	 */
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		if (target.indexOf('.') != -1) {
			return ;
		}
		
		isHandled[0] = true;
		String[] urlPara = {null};
		Action action = actionMapping.getAction(target, urlPara);
		
		if (action == null) {
			if (log.isWarnEnabled()) {
				String qs = request.getQueryString();
				log.warn("404 Action Not Found: " + (qs == null ? target : target + "?" + qs));
			}
			renderManager.getRenderFactory().getErrorRender(404).setContext(request, response).render();
			return ;
		}
		
		Controller controller = null;
		try {
			// Controller controller = action.getControllerClass().newInstance();
			controller = controllerFactory.getController(action.getControllerClass());
			controller.init(action, request, response, urlPara[0]);
			
			if (devMode) {
				if (ActionReporter.isReportAfterInvocation(request)) {
					new Invocation(action, controller).invoke();
					ActionReporter.report(target, controller, action);
				} else {
					ActionReporter.report(target, controller, action);
					new Invocation(action, controller).invoke();
				}
			}
			else {
				new Invocation(action, controller).invoke();
			}
			
			Render render = controller.getRender();
			if (render instanceof ForwardActionRender) {
				String actionUrl = ((ForwardActionRender)render).getActionUrl();
				if (target.equals(actionUrl)) {
					throw new RuntimeException("The forward action url is the same as before.");
				} else {
					handle(actionUrl, request, response, isHandled);
				}
				return ;
			}
			
			if (render == null) {
				render = renderManager.getRenderFactory().getDefaultRender(action.getViewPath() + action.getMethodName());
			}
			render.setContext(request, response, action.getViewPath()).render();
		}
		catch (RenderException e) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
		}
		catch (ActionException e) {
			int errorCode = e.getErrorCode();
			String msg = null;
			if (errorCode == 404) {
				msg = "404 Not Found: ";
			} else if (errorCode == 401) {
				msg = "401 Unauthorized: ";
			} else if (errorCode == 403) {
				msg = "403 Forbidden: ";
			}
			
			if (msg != null) {
				if (log.isWarnEnabled()) {
					String qs = request.getQueryString();
					log.warn(msg + (qs == null ? target : target + "?" + qs));
				}
			} else {
				if (log.isErrorEnabled()) {
					String qs = request.getQueryString();
					log.error(qs == null ? target : target + "?" + qs, e);
				}
			}
			
			e.getErrorRender().setContext(request, response, action.getViewPath()).render();
		}
		catch (Exception e) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
			renderManager.getRenderFactory().getErrorRender(500).setContext(request, response, action.getViewPath()).render();
		} finally {
			if (controller != null) {
				controller.clear();
			}
		}
	}
}





