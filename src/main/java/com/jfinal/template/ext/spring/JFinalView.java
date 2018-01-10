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

package com.jfinal.template.ext.spring;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.view.AbstractTemplateView;

/**
 * JFinalView
 * 
 * <pre>
 * 关键设置：
 * 1：setContentType("text/html;charset=UTF-8") 设置 content type 字符集为 UTF-8
 * 
 * 2：setExposeRequestAttributes(true) 设置将 request 中的属性值注入到 model 中去
 *    便于在模板中使用 #(value) 访问 request.setAttribute(...) 进去的值
 *    
 * 3： setExposeSessionAttributes(true) 设置将 session 中的属性值注入到 model 中去
 *    使用在模板中使用 #(value) 访问 session.setAttribute(...) 进去的值
 * 
 * 注意：JFinalViewResolver.setSessionInView(true) 中的配置与
 *      JFinalView.setExposeSessionAttributes(true) 可实现
 *      相似的功能，区别在于前者访问方式为 #(session.value) 而后者为
 *      #(value)，两种配置只选其一
 * </pre>
 */
public class JFinalView extends AbstractTemplateView {
	
	@Override
	protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (JFinalViewResolver.sessionInView) {
			HttpSession hs = request.getSession(JFinalViewResolver.createSession);
			if (hs != null) {
				model.put("session", new InnerSession(hs));
			}
		}
		
		OutputStream os = response.getOutputStream();
		JFinalViewResolver.engine.getTemplate(getUrl()).render(model, os);
	}

	@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
	public static class InnerSession extends HashMap<Object, Object> implements HttpSession {
		
		private static final long serialVersionUID = -8679493647540628009L;
		private HttpSession session;
		
		public InnerSession(HttpSession session) {
			this.session = session;
		}
		
		// HashMap 相关方法处理 ----------------------------------------------------
		/**
		 * 覆盖 HashMap 的 put
		 */
		public Object put(Object name, Object value) {
			session.setAttribute((String)name, value);
			return null;
		}
		
		/**
		 * 覆盖 HashMap 的 get
		 */
		public Object get(Object name) {
			return session.getAttribute((String)name);
		}
		
		// Session 相关方法处理 ----------------------------------------------------
		public Object getAttribute(String key) {
			return session.getAttribute(key);
		}
		
		public Enumeration getAttributeNames() {
			return session.getAttributeNames();
		}
		
		public long getCreationTime() {
			return session.getCreationTime();
		}
		
		public String getId() {
			return session.getId();
		}
		
		public long getLastAccessedTime() {
			return session.getLastAccessedTime();
		}
		
		public int getMaxInactiveInterval() {
			return session.getMaxInactiveInterval();
		}
		
		public ServletContext getServletContext() {
			return session.getServletContext();
		}
		
		public javax.servlet.http.HttpSessionContext getSessionContext() {
			return session.getSessionContext();
		}
		
		public Object getValue(String key) {
			return session.getValue(key);
		}
		
		public String[] getValueNames() {
			return session.getValueNames();
		}
		
		public void invalidate() {
			session.invalidate();
		}
		
		public boolean isNew() {
			return session.isNew();
		}
		
		public void putValue(String key, Object value) {
			session.putValue(key, value);
		}
		
		public void removeAttribute(String key) {
			session.removeAttribute(key);
		}
		
		public void removeValue(String key) {
			session.removeValue(key);
		}
		
		public void setAttribute(String key, Object value) {
			session.setAttribute(key, value);
		}
		
		public void setMaxInactiveInterval(int maxInactiveInterval) {
			session.setMaxInactiveInterval(maxInactiveInterval);
		}
		
		public String toString() {
			return session != null ? session.toString() : "null";
		}
	}
}






