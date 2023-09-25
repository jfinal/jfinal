/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.render;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.core.Const;
import com.jfinal.log.Log;

/**
 * Render.
 */
public abstract class Render {
	
	protected String view;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	
	private static String encoding = Const.DEFAULT_ENCODING;
	private static boolean devMode = Const.DEFAULT_DEV_MODE;
	
	static void init(String encoding, boolean devMode) {
		Render.encoding = encoding;
		Render.devMode = devMode;
	}
	
	public static String getEncoding() {
		return encoding;
	}
	
	public static boolean getDevMode() {
		return devMode;
	}
	
	public Render setContext(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		return this;
	}
	
	public Render setContext(HttpServletRequest request, HttpServletResponse response, String viewPath) {
		this.request = request;
		this.response = response;
		if (view != null && view.length() > 0 && view.charAt(0) != '/') {
			view = viewPath + view;
		}
		return this;
	}
	
	public String getView() {
		return view;
	}
	
	public void setView(String view) {
		this.view = view;
	}
	
	/**
	 * Render to client
	 */
	public abstract void render();
    
    /**
     * OutputStream、Writer 写入异常时，关闭它们，ActionHandler 中未向底层容器继续抛出 IOException，
     * 以防容器在意外情况下未关闭它们（虽然调试 undertow 源码得知，无论异常产生与否，都将关闭它们）
     */
    protected void close(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                Log.getLog(getClass()).error(e.getMessage(), e);
            }
        }
    }
}
