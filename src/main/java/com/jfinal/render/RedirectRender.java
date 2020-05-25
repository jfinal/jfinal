/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
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
	
	protected String url;
	protected boolean withQueryString;
	protected static final String contextPath = getContxtPath();
	
	protected static String protocol = null;
	
	/**
	 * 配置重定向时使用的协议，只允许配置为 http 与 https
	 * 
	 * 该配置将协议添加到未指定协议的 url 之中，主要用于解决 nginx 代理做 https 时无法重定向到 https 的问题
	 * 
	 * 
	 * 注意：当 url 中已经包含协议时，该配置无效，因为要支持跨域名重定向
	 *      例如： redirect("https://jfinal.com");
	 */
	public static void setProtocol(String protocol) {
		if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
			throw new IllegalArgumentException("protocol must be \"http\" or \"https\"");
		}
		RedirectRender.protocol = protocol.toLowerCase() + "://";
	}
	
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
		String ret;
		// 如果一个url为/login/connect?goto=http://www.jfinal.com，则有错误
		// ^((https|http|ftp|rtsp|mms)?://)$   ==> indexOf 取值为 (3, 5)
		if (contextPath != null && (url.indexOf("://") == -1 || url.indexOf("://") > 5)) {
			ret = contextPath + url;
		} else {
			ret = url;
		}
		
		if (withQueryString) {
			String queryString = request.getQueryString();
			if (queryString != null) {
				if (ret.indexOf('?') == -1) {
					ret = ret + "?" + queryString;
				} else {
					ret = ret + "&" + queryString;
				}
			}
		}
		
		// 跳过 http/https 已指定过协议类型的 url，用于支持跨域名重定向
		if (protocol == null || ret.toLowerCase().startsWith("http")) {
			return ret;
		}
		
		/**
		 * 支持 https 协议下的重定向
		 *     https://jfinal.com/feedback/6939
		 * 
		 * PS：nginx 层面配置 http 重定向到 https 的方法为：
		 *     proxy_redirect http:// https://;
		 */
		String serverName = request.getServerName();
		
		int port = request.getServerPort();
		if (port != 80 && port != 443) {
			serverName = serverName + ":" + port;
		}
		
		if (ret.charAt(0) != '/') {
			return protocol + serverName + "/" + ret;
		} else {
			return protocol + serverName + ret;
		}
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

