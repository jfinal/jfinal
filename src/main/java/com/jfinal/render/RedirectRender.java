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

import java.io.IOException;
import com.jfinal.core.JFinal;

/**
 * RedirectRender with status: 302 Found.
 */
public class RedirectRender extends Render {
	
	private String url;
	private boolean withQueryString;
	private static final String contextPath = getContxtPath();
	
	static String getContxtPath() {
		String cp = JFinal.me().getContextPath();
		return ("".equals(cp) || "/".equals(cp)) ? null : cp;
	}
	
	public RedirectRender(String url) {
		this.url = url;
		this.withQueryString = false;
	}
	
	public RedirectRender(String url, boolean withQueryString) {
		this.url = url;
		this.withQueryString =  withQueryString;
	}
	
	public String buildFinalUrl() {
		String result;
		// 如果一个url为/login/connect?goto=http://www.jfinal.com，则有错误
		// ^((https|http|ftp|rtsp|mms)?://)$   ==> indexOf 取值为 (3, 5)
		if (contextPath != null && (url.indexOf("://") == -1 || url.indexOf("://") > 5)) {
			result = contextPath + url;
		} else {
			result = url;
		}
		
		if (withQueryString) {
			String queryString = request.getQueryString();
			if (queryString != null) {
				if (result.indexOf("?") == -1) {
					result = result + "?" + queryString;
				} else {
					result = result + "&" + queryString;
				}
			}
		}
		
		return result;
	}
	
	public void render() {
		String finalUrl = buildFinalUrl();
		
		try {
			response.sendRedirect(finalUrl);	// always 302
		} catch (IOException e) {
			throw new RenderException(e);
		}
	}
}

