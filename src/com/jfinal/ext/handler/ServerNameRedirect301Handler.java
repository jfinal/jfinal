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

package com.jfinal.ext.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.handler.Handler;

/**
 * ServerNameRedirect301Handler redirect to new server name with 301 Moved Permanently.
 */
public class ServerNameRedirect301Handler extends Handler {
	
	private String originalServerName;
	private String targetServerName;
	private int serverNameLength;
	
	/**
	 * Example: new ServerNameRedirectHandler("http://abc.com", "http://www.abc.com")
	 * @param originalServerName	The original server name that you want be redirect 
	 * @param targetServerName		The target server name that redirect to
	 */
	public ServerNameRedirect301Handler(String originalServerName, String targetServerName) {
		this.originalServerName = originalServerName;
		this.targetServerName = targetServerName;
		
		format();
		serverNameLength = this.originalServerName.length();
	}
	
	private final void format() {
		if (originalServerName.endsWith("/"))
			originalServerName = originalServerName.substring(0, originalServerName.length() - 1);
		
		if (targetServerName.endsWith("/"))
			targetServerName = targetServerName.substring(0, targetServerName.length() - 1);
		
		// 此处没有考虑 https 的情况, 该情况由用户在 new ServerNameRedirectHandler() 时自行解决
		if (originalServerName.indexOf("://") == -1)
			originalServerName = "http://" + originalServerName;
		
		if (targetServerName.indexOf("://") == -1)
			targetServerName = "http://" + targetServerName;
	}
	
	@Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		String url = request.getRequestURL().toString();
		if (url.startsWith(originalServerName)) {
			isHandled[0] = true;
			
			String queryString = request.getQueryString();
			queryString = (queryString == null ? "" : "?" + queryString);
			url = targetServerName + url.substring(serverNameLength) + queryString;
			
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			// response.sendRedirect(url);	// always 302
			response.setHeader("Location", url);
			response.setHeader("Connection", "close");
		}
		else  {
			nextHandler.handle(target, request, response, isHandled);
		}
	}
}




/*

http://souwangxiao.com redirect 301 to  http://www.souwangxiao.com

<%@ page language="java" pageEncoding="utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%
if(request.getRequestURL().indexOf("http://souwangxiao.com") >= 0) {
	// String requestURI = request.getRequestURI();
	// String queryString = request.getQueryString();
	// queryString = (queryString == null ? "" : "?" + queryString);
	//attempt to merge non-www urls
	response.setStatus(301);	// 301 rewrite
	// response.setHeader("Location", "http://www.souwangxiao.com" + requestURI + queryString);
	response.setHeader("Location", "http://www.souwangxiao.com");
	response.setHeader("Connection", "close");
}
else {%>
	<s:action namespace="/" name="index" executeResult="true" />
<%}%>

*/


