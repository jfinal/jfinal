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
 * JsonRequest 包装 json 请求解析结果，从底层接管所有 parameter 操作
 */
public class JsonRequest implements HttpServletRequest {
	
	private final HttpServletRequest req;
	private final HashMap<String, String[]> paraMap;
	
	public JsonRequest(HttpServletRequest req, HashMap<String, String[]> paraMap) {
		this.req = req;
		this.paraMap = paraMap;
	}
	
	@Override
	public String getParameter(String name) {
		String[] ret = paraMap.get(name);
		return ret != null && ret.length != 0 ? ret[0] : null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(paraMap.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		return paraMap.get(name);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return paraMap;
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

