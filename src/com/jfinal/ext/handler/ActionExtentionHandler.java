/**
 * 
 */
package com.jfinal.ext.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;

/**
 * @author 朱丛启
 * May 2, 201510:44:09 AM
 */
public class ActionExtentionHandler extends Handler {

	// 伪静态处理
	public static String htmlExt = ".html";
	public static String htmExt = ".htm";
	public static String jsonExt = ".json";
	
	private int len = ActionExtentionHandler.htmlExt.length();
	private String actionExt = ActionExtentionHandler.htmlExt;
	
	public ActionExtentionHandler(){
		this(ActionExtentionHandler.htmExt);
	}
	
	public ActionExtentionHandler(String actionExt){
		this.actionExt = actionExt;
		this.len = actionExt.length();
	}
	
	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, boolean[] isHandled) {
		if (target.endsWith(this.actionExt))
            target = target.substring(0, target.length() - this.len);
        nextHandler.handle(target, request, response, isHandled);
	}

}
