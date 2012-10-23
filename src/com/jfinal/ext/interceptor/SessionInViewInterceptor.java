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

package com.jfinal.ext.interceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;

/**
 * SessionInViewInterceptor.
 */
public class SessionInViewInterceptor implements Interceptor {
	
	private boolean createSession = false;
	
	public SessionInViewInterceptor() {
	}
	
	public SessionInViewInterceptor(boolean createSession) {
		this.createSession = createSession;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})	
	public void intercept(ActionInvocation ai) {
		ai.invoke();
		
		Controller c = ai.getController();
		HttpSession hs = c.getSession(createSession);
		if (hs != null) {
			Map session = new JFinalSession(hs);
			for (Enumeration<String> names=hs.getAttributeNames(); names.hasMoreElements();) {
				String name = names.nextElement();
				session.put(name, hs.getAttribute(name));
			}
			c.setAttr("session", session);
		}
	}
}

@SuppressWarnings({"deprecation", "serial", "rawtypes"})
class JFinalSession extends HashMap implements HttpSession {
	
	private HttpSession s;
	
	public JFinalSession(HttpSession session) {
		this.s = session;
	}
	
	public Object getAttribute(String key) {
		return s.getAttribute(key);
	}
	
	public Enumeration getAttributeNames() {
		return s.getAttributeNames();
	}
	
	public long getCreationTime() {
		return s.getCreationTime();
	}
	
	public String getId() {
		return s.getId();
	}
	
	public long getLastAccessedTime() {
		return s.getLastAccessedTime();
	}
	
	public int getMaxInactiveInterval() {
		return s.getMaxInactiveInterval();
	}
	
	public ServletContext getServletContext() {
		return s.getServletContext();
	}
	
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		return s.getSessionContext();
	}
	
	public Object getValue(String key) {
		return s.getValue(key);
	}
	
	public String[] getValueNames() {
		return s.getValueNames();
	}
	
	public void invalidate() {
		s.invalidate();
	}
	
	public boolean isNew() {
		return s.isNew();
	}
	
	public void putValue(String key, Object value) {
		s.putValue(key, value);
	}
	
	public void removeAttribute(String key) {
		s.removeAttribute(key);
	}
	
	public void removeValue(String key) {
		s.removeValue(key);
	}
	
	public void setAttribute(String key, Object value) {
		s.setAttribute(key, value);
	}
	
	public void setMaxInactiveInterval(int maxInactiveInterval) {
		s.setMaxInactiveInterval(maxInactiveInterval);
	}
}

/*
public void intercept(ActionInvocation ai) {
	ai.invoke();
	
	Controller c = ai.getController();
	HttpSession hs = c.getSession(createSession);
	if (hs != null) {
		c.setAttr("session", new JFinalSession(hs));
	}
}
*/
