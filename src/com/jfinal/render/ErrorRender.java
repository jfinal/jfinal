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

package com.jfinal.render;

import java.io.IOException;
import java.io.PrintWriter;
import com.jfinal.core.Const;

/**
 * ErrorRender.
 */
public class ErrorRender extends Render {
	
	protected static final String contentType = "text/html; charset=" + getEncoding();
	
	protected static final String version = "<center><a href='http://www.jfinal.com?f=ev' target='_blank'><b>Powered by JFinal " + Const.JFINAL_VERSION + "</b></a></center>";
	
	protected static final String html404 = "<html><head><title>404 Not Found</title></head><body bgcolor='white'><center><h1>404 Not Found</h1></center><hr>" + version + "</body></html>";
	protected static final String html500 = "<html><head><title>500 Internal Server Error</title></head><body bgcolor='white'><center><h1>500 Internal Server Error</h1></center><hr>" + version + "</body></html>";
	
	protected static final String html401 = "<html><head><title>401 Unauthorized</title></head><body bgcolor='white'><center><h1>401 Unauthorized</h1></center><hr>" + version + "</body></html>";
	protected static final String html403 = "<html><head><title>403 Forbidden</title></head><body bgcolor='white'><center><h1>403 Forbidden</h1></center><hr>" + version + "</body></html>";
	
	protected int errorCode;
	
	public ErrorRender(int errorCode, String view) {
		this.errorCode = errorCode;
		this.view = view;
	}
	
	public void render() {
		response.setStatus(getErrorCode());	// HttpServletResponse.SC_XXX_XXX
		
		// render with view
		String view = getView();
		if (view != null) {
			RenderFactory.me().getRender(view).setContext(request, response).render();
			return;
		}
		
		// render with html content
		PrintWriter writer = null;
		try {
			response.setContentType(contentType);
	        writer = response.getWriter();
	        writer.write(getErrorHtml());
	        writer.flush();
		} catch (IOException e) {
			throw new RenderException(e);
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}
	
	public String getErrorHtml() {
		int errorCode = getErrorCode();
		if (errorCode == 404)
			return html404;
		if (errorCode == 500)
			return html500;
		if (errorCode == 401)
			return html401;
		if (errorCode == 403)
			return html403;
		return "<html><head><title>" + errorCode + " Error</title></head><body bgcolor='white'><center><h1>" + errorCode + " Error</h1></center><hr>" + version + "</body></html>";
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}





