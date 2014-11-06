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

package com.jfinal.kit;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.render.RenderException;
import com.jfinal.render.RenderFactory;

/**
 * HandlerKit.
 */
public class HandlerKit {
	
	private HandlerKit(){}
	
	public static void renderError404(String view, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		isHandled[0] = true;
		
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		RenderFactory.me().getRender(view).setContext(request, response).render();
	}
	
	public static void renderError404(HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		isHandled[0] = true;
		
		RenderFactory.me().getErrorRender(404).setContext(request, response).render();
	}
	
	public static void redirect301(String url, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		isHandled[0] = true;
		
		String queryString = request.getQueryString();
		if (queryString != null)
			url += "?" + queryString;
		
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		response.setHeader("Location", url);
		response.setHeader("Connection", "close");
	}
	
	public static void redirect(String url, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		isHandled[0] = true;
		
		String queryString = request.getQueryString();
		if (queryString != null)
			url = url + "?" + queryString;
		
		try {
			response.sendRedirect(url);	// always 302
		} catch (IOException e) {
			throw new RenderException(e);
		}
	}
}
