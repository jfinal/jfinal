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

package com.jfinal.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.kit.StrKit;
// import com.jfinal.log.Log;
import com.jfinal.render.Render;
import com.jfinal.render.RenderManager;

/**
 * ActionException.
 */
public class ActionException extends RuntimeException {
	
	private static final long serialVersionUID = 1998063243843477017L;
	// private static final Log log = Log.getLog(ActionException.class);
	private int errorCode;
	private String errorMessage;
	private Render errorRender;
	
	public ActionException(int errorCode, Render errorRender) {
		init(errorCode, errorRender);
	}
	
	private void init(final int errorCode, final Render errorRender) {
		if (errorRender == null) {
			throw new IllegalArgumentException("The parameter errorRender can not be null.");
		}
		
		this.errorCode = errorCode;
		
		if (errorRender instanceof com.jfinal.render.ErrorRender) {
			this.errorRender = errorRender;
		}
		else {
			this.errorRender = new Render() {
				public Render setContext(HttpServletRequest req, HttpServletResponse res, String viewPath) {
					errorRender.setContext(req, res, viewPath);
					res.setStatus(errorCode);	// important
					return this;
				}
				
				public void render() {
					errorRender.render();
				}
			};
		}
	}
	
	public ActionException(int errorCode, String viewOrJson) {
		if (StrKit.isBlank(viewOrJson)) {
			throw new IllegalArgumentException("The parameter viewOrJson can not be blank.");
		}
		
		this.errorCode = errorCode;
		this.errorRender = RenderManager.me().getRenderFactory().getErrorRender(errorCode, viewOrJson);
	}
	
	public ActionException(int errorCode, Render errorRender, String errorMessage) {
		this.errorMessage = errorMessage;
		init(errorCode, errorRender);
		// log.warn(errorMessage);		// ActionHandler 中添加了对 message 的日志输出
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	@Override
	public String getMessage() {
		return errorMessage;
	}
	
	@Override
	public String getLocalizedMessage() {
		return errorMessage;
	}
	
	public Render getErrorRender() {
		return errorRender;
	}
	
	/**
	 * 异常构造函数会调用 fillInStackTrace() 构建整个调用栈，消耗较大
	 * 而 ActionException 无需使用调用栈信息，覆盖此方法用于提升性能
	 */
	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}

