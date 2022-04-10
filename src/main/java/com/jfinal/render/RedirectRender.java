/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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
import com.jfinal.kit.StrKit;

/**
 * RedirectRender with status: 302 Found.
 * 
 * 
 * 注意：使用 nginx 代理实现 https 的场景，解决 https 被重定向到了 http 的问题，需要在 nginx 中添加如下配置：
 *      proxy_set_header X-Forwarded-Proto $scheme;
 *      proxy_set_header X-Forwarded-Port $server_port;
 *      
 *      
 * PS：nginx 将 http 重定向到 https 的配置为：
 *     rewrite ^(.*)$ https://$host$1;
 *     注意: 需要同时支持 http 与 https 的场景不能使用该配置
 *     
 */
public class RedirectRender extends Render {
	
	protected String url;
	protected boolean withQueryString;
	protected static final String contextPath = getContxtPath();
	
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
		// 如果一个url为/login/connect?goto=https://jfinal.com，则有错误
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
		if (ret.toLowerCase().startsWith("http")) {
			return ret;
		}
		
		/**
		 * 注意：nginx 代理 https 的场景，需要使用如下配置:
		 *       proxy_set_header X-Forwarded-Proto $scheme;
		 *       proxy_set_header X-Forwarded-Port $server_port;
		 */
		if ("https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"))) {
			String serverName = request.getServerName();
			
			/**
			 * 获取 nginx 端通过配置 proxy_set_header X-Forwarded-Port $server_port;
			 * 传递过来的端口号，保障重定向时端口号是正确的
			 */
			String port = request.getHeader("X-Forwarded-Port");
			if (StrKit.notBlank(port)) {
				serverName = serverName + ":" + port;
			}
			
			if (ret.charAt(0) != '/') {
				return "https://" + serverName + "/" + ret;
			} else {
				return "https://" + serverName + ret;
			}
			
		} else {
			return ret;
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

