package com.jfinal.template.ext.spring;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.view.AbstractTemplateView;

public class JFinalView extends AbstractTemplateView {
	
	@Override
	protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// response.setContentType(getContentType());
		
		if (JFinalViewResolver.sessionInView) {
			HttpSession hs = request.getSession(JFinalViewResolver.createSession);
			if (hs != null) {
				model.put("session", new InnerSession(hs));
			}
		}
		
		for (Enumeration<String> attrs=request.getAttributeNames(); attrs.hasMoreElements();) {
			String attrName = attrs.nextElement();
			model.put(attrName, request.getAttribute(attrName));
		}
		
		JFinalViewResolver.engine.getTemplate(getUrl()).render(model, response.getWriter());
	}
}

@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
class InnerSession extends HashMap<Object, Object> implements HttpSession {
	
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
}







