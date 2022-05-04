/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com) / 玛雅牛 (myaniu AT gmail dot com).
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

package com.jfinal.core.paragetter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

/**
 * JsonRequest 包装 json 请求，从底层接管所有 parameter 操作
 */
public class JsonRequest implements HttpServletRequest {

	// 缓存 JSONObject、JSONArray 对象
	private com.alibaba.fastjson.JSONObject jsonObject;
	private com.alibaba.fastjson.JSONArray jsonArray;

	// 包装请求对象
	private HttpServletRequest req;

	// 通过 JSONObject 延迟生成 paraMap
	private HashMap<String, String[]> paraMap;

	public JsonRequest(String jsonString, HttpServletRequest req) {
		Object json = com.alibaba.fastjson.JSON.parse(jsonString);
		if (json instanceof com.alibaba.fastjson.JSONObject) {
			jsonObject = (com.alibaba.fastjson.JSONObject)json;
		} else if (json instanceof com.alibaba.fastjson.JSONArray) {
			jsonArray = (com.alibaba.fastjson.JSONArray)json;
		}

		this.req = req;
	}

	/**
	 * 第一个版本只做简单转换，用户获取 JSONObject 与 JSONArray 后可以进一步进行复杂转换
	 */
	public com.alibaba.fastjson.JSONObject getJSONObject() {
		return jsonObject;
	}

	public com.alibaba.fastjson.JSONArray getJSONArray() {
		return jsonArray;
	}

	/*public Map<String, Object> getJsonMap() {
		return jsonObject;
	}
	public java.util.List<Object> getJsonList() {
		return jsonArray;
	}*/

	/**
	 * 获取内部 HttpServletRequest 对象
	 */
	public HttpServletRequest getInnerRequest() {
		return req;
	}

	/**
	 * 请求参数是否为 JSONObject 对象
	 */
	public boolean isJSONObject() {
		return jsonObject != null;
	}

	/**
	 * 请求参数是否为 JSONArray 对象
	 */
	public boolean isJSONArray() {
		return jsonArray != null;
	}

	// 延迟创建，不是每次都会调用 parameter 相关方法
	private HashMap<String, String[]> getParaMap() {
		if (paraMap == null) {
			paraMap = (jsonObject != null ? createParaMap(jsonObject) : new HashMap<>());
		}
		return paraMap;
	}

	private HashMap<String, String[]> createParaMap(com.alibaba.fastjson.JSONObject jsonPara) {
		HashMap<String, String[]> newPara = new HashMap<>();

		// 先读取 parameter，否则后续从流中读取 rawData 后将无法读取 parameter（部分 servlet 容器）
		Map<String, String[]> oldPara = req.getParameterMap();
		if (oldPara != null && oldPara.size() > 0) {
			newPara.putAll(oldPara);
		}

		for (Map.Entry<String, Object> e : jsonPara.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();
			// 只转换最外面一层 json 数据，如果存在多层 json 结构，仅将其视为 String 留给后续流程转换
			if (value instanceof com.alibaba.fastjson.JSON) {
				newPara.put(key, new String[]{((com.alibaba.fastjson.JSON)value).toJSONString()});
			} else if (value != null) {
				newPara.put(key, new String[]{value.toString()});
			} else {
				// 需要考虑 value 是否转成 String[] array = {""}，ActionRepoter.getParameterValues() 有依赖
				newPara.put(key, null);
			}
		}

		return newPara;
	}

	@Override
	public String getParameter(String name) {
		// String[] ret = getParaMap().get(name);
		// return ret != null && ret.length != 0 ? ret[0] : null;

		// 优化性能，避免调用 getParaMap() 触发调用 createParaMap()，从而大概率避免对整个 jsonObject 进行转换
		if (jsonObject != null && jsonObject.containsKey(name)) {
			Object value = jsonObject.get(name);
			if (value instanceof com.alibaba.fastjson.JSON) {
				return ((com.alibaba.fastjson.JSON)value).toJSONString();
			} else if (value != null) {
				return value.toString();
			} else {
				// 需要考虑是否返回 ""，表单提交请求只要 name 存在则值不会为 null
				return null;
			}
		} else {
			return req.getParameter(name);
		}
	}

	/**
	 * 该方法将触发 createParaMap()，框架内部应尽可能避免该事情发生，以优化性能
	 */
	@Override
	public Map<String, String[]> getParameterMap() {
		return getParaMap();
	}

	/**
	 * 该方法将触发 createParaMap()，框架内部应尽可能避免该事情发生，以优化性能
	 */
	@Override
	public String[] getParameterValues(String name) {
		return getParaMap().get(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		// return Collections.enumeration(getParaMap().keySet());
		if (jsonObject != null) {
			return Collections.enumeration(jsonObject.keySet());
		} else {
			return Collections.emptyEnumeration();
		}
	}

	// ---------------------------------------------------------------
	// 以下方法仅为对 req 对象的转调 -------------------------------------

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return req.getInputStream();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return req.getReader();
	}

	@Override
	public Object getAttribute(String name) {
		return req.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return req.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding() {
		return req.getCharacterEncoding();
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		req.setCharacterEncoding(env);
	}

	@Override
	public int getContentLength() {
		return req.getContentLength();
	}

	@Override
	public long getContentLengthLong() {
		return req.getContentLengthLong();
	}

	@Override
	public String getContentType() {
		return req.getContentType();
	}

	@Override
	public String getProtocol() {
		return req.getProtocol();
	}

	@Override
	public String getScheme() {
		return req.getScheme();
	}

	@Override
	public String getServerName() {
		return req.getServerName();
	}

	@Override
	public int getServerPort() {
		return req.getServerPort();
	}

	@Override
	public String getRemoteAddr() {
		return req.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		return req.getRemoteHost();
	}

	@Override
	public void setAttribute(String name, Object o) {
		req.setAttribute(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		req.removeAttribute(name);
	}

	@Override
	public Locale getLocale() {
		return req.getLocale();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return req.getLocales();
	}

	@Override
	public boolean isSecure() {
		return req.isSecure();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return req.getRequestDispatcher(path);
	}

	@Override
	@SuppressWarnings("deprecation")
	public String getRealPath(String path) {
		return req.getRealPath(path);
	}

	@Override
	public int getRemotePort() {
		return req.getRemotePort();
	}

	@Override
	public String getLocalName() {
		return req.getLocalName();
	}

	@Override
	public String getLocalAddr() {
		return req.getLocalAddr();
	}

	@Override
	public int getLocalPort() {
		return req.getLocalPort();
	}

	@Override
	public ServletContext getServletContext() {
		return req.getServletContext();
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return req.startAsync();
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		return req.startAsync(servletRequest, servletResponse);
	}

	@Override
	public boolean isAsyncStarted() {
		return req.isAsyncStarted();
	}

	@Override
	public boolean isAsyncSupported() {
		return req.isAsyncSupported();
	}

	@Override
	public AsyncContext getAsyncContext() {
		return req.getAsyncContext();
	}

	@Override
	public DispatcherType getDispatcherType() {
		return req.getDispatcherType();
	}

	@Override
	public String getAuthType() {
		return req.getAuthType();
	}

	@Override
	public Cookie[] getCookies() {
		return req.getCookies();
	}

	@Override
	public long getDateHeader(String name) {
		return req.getDateHeader(name);
	}

	@Override
	public String getHeader(String name) {
		return req.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return req.getHeaders(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return req.getHeaderNames();
	}

	@Override
	public int getIntHeader(String name) {
		return req.getIntHeader(name);
	}

	@Override
	public String getMethod() {
		return req.getMethod();
	}

	@Override
	public String getPathInfo() {
		return req.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return req.getPathTranslated();
	}

	@Override
	public String getContextPath() {
		return req.getContextPath();
	}

	@Override
	public String getQueryString() {
		return req.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		return req.getRemoteUser();
	}

	@Override
	public boolean isUserInRole(String role) {
		return req.isUserInRole(role);
	}

	@Override
	public Principal getUserPrincipal() {
		return req.getUserPrincipal();
	}

	@Override
	public String getRequestedSessionId() {
		return req.getRequestedSessionId();
	}

	@Override
	public String getRequestURI() {
		return req.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return req.getRequestURL();
	}

	@Override
	public String getServletPath() {
		return req.getServletPath();
	}

	@Override
	public HttpSession getSession(boolean create) {
		return req.getSession(create);
	}

	@Override
	public HttpSession getSession() {
		return req.getSession();
	}

	@Override
	public String changeSessionId() {
		return req.changeSessionId();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return req.isRequestedSessionIdValid();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return req.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return req.isRequestedSessionIdFromURL();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isRequestedSessionIdFromUrl() {
		return req.isRequestedSessionIdFromUrl();
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return req.authenticate(response);
	}

	@Override
	public void login(String username, String password) throws ServletException {
		req.login(username, password);
	}

	@Override
	public void logout() throws ServletException {
		req.logout();
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return req.getParts();
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		return req.getPart(name);
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		return req.upgrade(handlerClass);
	}
}

