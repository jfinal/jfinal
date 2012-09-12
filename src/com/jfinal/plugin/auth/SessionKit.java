package com.jfinal.plugin.auth;

public class SessionKit {
	
	public ISession getSession(String accessToken, boolean create) {
		return null;
	}
	
	public ISession getSession(String accessToken) {
		return getSession(accessToken, true);
	}
	
	public void removeSession(String accessToken) {
		
	}
}
