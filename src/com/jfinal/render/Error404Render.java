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

package com.jfinal.render;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.core.Const;

/**
 * Error404Render.
 */
@SuppressWarnings("serial")
class Error404Render extends Render {
	
	private static final String contentType = "text/html;charset=" + getEncoding();
	private static final String defaultHtml = "<html><head><title>404 Not Found</title></head><body bgcolor='white'><center><h1>404 Not Found</h1></center><hr><center><a href='http://www.jfinal.com'>JFinal/" + Const.JFINAL_VERSION + "</a></center></body></html>";
	private Render render;
	
	public Error404Render(String view) {
		this.view = view;
	}
	
	public Error404Render() {
		
	}
	
	public void render() {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		
		// render with view
		if (view != null) {
			render = RenderFactory.me().getRender(view);
			render.setContext(request, response);
			render.render();
			return;
		}
		
		// render with defaultHtml
		PrintWriter writer = null;
		try {
			response.setContentType(contentType);
	        writer = response.getWriter();
	        writer.write(defaultHtml);
	        writer.flush();
		} catch (IOException e) {
			throw new RenderException(e);
		}
		finally {
			writer.close();
		}
	}
}




