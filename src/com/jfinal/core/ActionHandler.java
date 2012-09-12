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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.config.Constants;
import com.jfinal.handler.Handler;
import com.jfinal.log.Logger;
import com.jfinal.render.Error404Exception;
import com.jfinal.render.Error500Exception;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;
import com.jfinal.render.RenderFactory;

/**
 * ActionHandler
 */
final class ActionHandler extends Handler {
	
	private final boolean devMode;
	private final ActionMapping actionMapping;
	private static final RenderFactory renderFactory = RenderFactory.me();
	private static final Logger log = Logger.getLogger(ActionHandler.class);
	
	public ActionHandler(ActionMapping actionMapping, Constants constants) {
		this.actionMapping = actionMapping;
		this.devMode = constants.getDevMode();
	}
	
	/**
	 * handle
	 * 1: Action action = actionMapping.getAction(target)
	 * 2: new ActionInvocation(...).invoke()
	 * 3: render(...)
	 */
	public final void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		if (target.indexOf(".") != -1) {
			return ;
		}
		
		isHandled[0] = true;
		String[] urlPara = {null};
		Action action = actionMapping.getAction(target, urlPara);
		
		if (action == null) {
			if (log.isWarnEnabled()) {
				String qs = request.getQueryString();
				log.warn("Action not found: " + (qs == null ? target : target + "?" + qs));
			}
			renderFactory.getError404Render().setContext(request, response).render();
			return ;
		}
		
		try {
			Controller controller = action.getControllerClass().newInstance();
			controller.init(request, response, urlPara[0]);
			
			if (devMode) {
				boolean isMultipartRequest = ActionReporter.reportCommonRequest(controller, action);
				new ActionInvocation(action, controller).invoke();
				if (isMultipartRequest) ActionReporter.reportMultipartRequest(controller, action);
			}
			else {
				new ActionInvocation(action, controller).invoke();
			}
			
			Render render = controller.getRender();
			if (render instanceof ActionRender) {
				String actionUrl = ((ActionRender)render).getActionUrl();
				if (target.equals(actionUrl))
					throw new RuntimeException("The forward action url is the same as before.");
				else
					handle(actionUrl, request, response, isHandled);
				return ;
			}
			
			if (render == null)
				render = renderFactory.getDefaultRender(action.getViewPath() + action.getMethodName());
			render.setContext(request, response, action.getViewPath()).render();
		}
		catch (RenderException e) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
		}
		catch (Error404Exception e) {
			if (log.isWarnEnabled()) {
				String qs = request.getQueryString();
				log.warn("Resource not found: " + (qs == null ? target : target + "?" + qs));
			}
			e.getError404Render().setContext(request, response).render();
		}
		catch (Error500Exception e) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
			e.getError500Render().setContext(request, response).render();
		}
		catch (Exception e) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
			renderFactory.getError500Render().setContext(request, response).render();
		}
	}
}





