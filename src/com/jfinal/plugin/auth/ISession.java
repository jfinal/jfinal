package com.jfinal.plugin.auth;

public interface ISession {
	String getId();
	Object getAttr(String name);
	ISession setAttr(String name, Object value);
	String[] getAttrNames();
	void removeAttr(String name);
	void invalidate(String accessToken);
}
