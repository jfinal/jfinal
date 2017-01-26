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

package com.jfinal.render;

import javax.servlet.http.HttpServletResponse;

/**
 * Redirect301Render.
 */
public class Redirect301Render extends RedirectRender {
	
	public Redirect301Render(String url) {
		super(url);
	}
	
	public Redirect301Render(String url, boolean withQueryString) {
		super(url, withQueryString);
	}
	
	public void render() {
		String finalUrl = buildFinalUrl();
		
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		// response.sendRedirect(url);	// always 302
		response.setHeader("Location", finalUrl);
		response.setHeader("Connection", "close");
	}
}






