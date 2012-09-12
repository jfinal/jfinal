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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;

/**
 * SessionInViewInterceptor.
 */
public class SessionInViewInterceptor implements Interceptor {
	
	public void intercept(ActionInvocation ai) {
		ai.invoke();
		
		Controller c = ai.getController();
		HttpSession hs = c.getSession(false);
		if (hs != null) {
			c.setAttr("session", new JFinalSession(hs));
		}
	}
}

@SuppressWarnings({"deprecation", "rawtypes"})
class JFinalSession implements HttpSession {
	
	/**
	 * Added by JFinal for FreeMarker and Beetl.
	 */
	public Object get(String key) {
		return s.getAttribute(key);
	}
	
	private HttpSession s;
	
	public JFinalSession(HttpSession session) {
		this.s = session;
	}
	
	public Object getAttribute(String arg0) {
		return s.getAttribute(arg0);
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
	
	public Object getValue(String arg0) {
		return s.getValue(arg0);
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
	
	public void putValue(String arg0, Object arg1) {
		s.putValue(arg0, arg1);
	}
	
	public void removeAttribute(String arg0) {
		s.removeAttribute(arg0);
	}
	
	public void removeValue(String arg0) {
		s.removeValue(arg0);
	}
	
	public void setAttribute(String arg0, Object arg1) {
		s.setAttribute(arg0, arg1);
	}
	
	public void setMaxInactiveInterval(int arg0) {
		s.setMaxInactiveInterval(arg0);
	}
}

//@SuppressWarnings({"rawtypes", "unchecked"})	
//public void intercept(ActionInvocation ai) {
//	ai.invoke();
//	
//	Controller c = ai.getController();
//	HttpSession hs = c.getSession(false);
//	if (hs != null) {
//		Map session = new HashMap();
//		for (Enumeration<String> names=hs.getAttributeNames(); names.hasMoreElements();) {
//			String name = names.nextElement();
//			session.put(name, hs.getAttribute(name));
//		}
//		c.setAttr("session", session);
//	}
//}

