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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import com.jfinal.core.Const;
import com.jfinal.kit.Okv;

/**
 * ErrorRender.
 */
public class ErrorRender extends Render {
	
	protected static final String contentTypeHtml = "text/html; charset=" + getEncoding();
	protected static final String contentTypeJson = "application/json; charset=" + getEncoding();
	
	protected static final String version = "<center><a href='https://gitee.com/jfinal/jfinal' target='_blank'><b>Powered by JFinal " + Const.JFINAL_VERSION + "</b></a></center>";
	
	protected static final byte[] html404 = ("<html><head><title>404 Not Found</title></head><body bgcolor='white'><center><h1>404 Not Found</h1></center><hr>" + version + "</body></html>").getBytes();
	protected static final byte[] html500 = ("<html><head><title>500 Internal Server Error</title></head><body bgcolor='white'><center><h1>500 Internal Server Error</h1></center><hr>" + version + "</body></html>").getBytes();
	protected static final byte[] html400 = ("<html><head><title>400 Bad Request</title></head><body bgcolor='white'><center><h1>400 Bad Request</h1></center><hr>" + version + "</body></html>").getBytes();
	protected static final byte[] html401 = ("<html><head><title>401 Unauthorized</title></head><body bgcolor='white'><center><h1>401 Unauthorized</h1></center><hr>" + version + "</body></html>").getBytes();
	protected static final byte[] html403 = ("<html><head><title>403 Forbidden</title></head><body bgcolor='white'><center><h1>403 Forbidden</h1></center><hr>" + version + "</body></html>").getBytes();
	
	protected static final byte[] json404 = Okv.of("state", "fail").set("msg", "404 Not Found").toJson().getBytes();
	protected static final byte[] json500 = Okv.of("state", "fail").set("msg", "500 Internal Server Error").toJson().getBytes();
	protected static final byte[] json400 = Okv.of("state", "fail").set("msg", "400 Bad Request").toJson().getBytes();
	protected static final byte[] json401 = Okv.of("state", "fail").set("msg", "401 Unauthorized").toJson().getBytes();
	protected static final byte[] json403 = Okv.of("state", "fail").set("msg", "403 Forbidden").toJson().getBytes();
	
	protected static final Map<Integer, byte[]> errorHtmlMap = new HashMap<>();
	protected static final Map<Integer, byte[]> errorJsonMap = new HashMap<>();
	
	protected static final Map<Integer, String> errorViewMap = new HashMap<Integer, String>();
	
	static {
		errorHtmlMap.put(404, html404);
		errorHtmlMap.put(500, html500);
		errorHtmlMap.put(400, html400);
		errorHtmlMap.put(401, html401);
		errorHtmlMap.put(403, html403);
		
		errorJsonMap.put(404, json404);
		errorJsonMap.put(500, json500);
		errorJsonMap.put(400, json400);
		errorJsonMap.put(401, json401);
		errorJsonMap.put(403, json403);
	}
	
	protected int errorCode;
	protected String viewOrJson;
	
	public ErrorRender(int errorCode, String viewOrJson) {
		this.errorCode = errorCode;
		this.viewOrJson = viewOrJson;
	}
	
	public ErrorRender(int errorCode) {
		this.errorCode = errorCode;
	}
	
	/**
	 * 设置异常发生时响应的错误页面
	 */
	public static void setErrorView(int errorCode, String errorView) {
		errorViewMap.put(errorCode, errorView);
	}
	
	public static String getErrorView(int errorCode) {
		return errorViewMap.get(errorCode);
	}
	
	/**
	 * 设置异常发生时响应的 html 内容
	 */
	public static void setErrorHtmlContent(int errorCode, String htmlContent) {
		try {
			errorHtmlMap.put(errorCode, htmlContent.getBytes(getEncoding()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 设置异常发生时响应的 json 内容
	 */
	public static void setErrorJsonContent(int errorCode, String jsonContent) {
		try {
			errorJsonMap.put(errorCode, jsonContent.getBytes(getEncoding()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void render() {
		response.setStatus(getErrorCode());	// HttpServletResponse.SC_XXX_XXX
		
		String ct = request.getContentType();
		boolean isJsonContentType = ct != null && ct.indexOf("json") != -1;
		
		// 支持 me.setErrorView(xxx.html) 配置
		// 注意：针对 json 的 setErrorJsonContent(...) 直接覆盖掉了默认值，会走后面的 response.getOutputStream().write(...)
		if (viewOrJson == null) {
		    if (! isJsonContentType) {
		        viewOrJson = getErrorView(getErrorCode());
		    }
		}
		
		// render with viewOrJson
		if (viewOrJson != null) {
			if (isJsonContentType) {
				RenderManager.me().getRenderFactory().getJsonRender(viewOrJson).setContext(request, response).render();
			} else {
				RenderManager.me().getRenderFactory().getRender(viewOrJson).setContext(request, response).render();
			}
			return;
		}
		
		// render with html content
		OutputStream os = null;
		try {
			response.setContentType(isJsonContentType ? contentTypeJson : contentTypeHtml);
			os = response.getOutputStream();
			os.write(isJsonContentType ? getErrorJson() : getErrorHtml());
		} catch (Exception e) {
		    if (e instanceof IOException) {
		        close(os);
		    }
			throw new RenderException(e);
		}
	}
	
	public byte[] getErrorHtml() {
		byte[] ret = errorHtmlMap.get(getErrorCode());
		return ret != null ? ret : ("<html><head><title>" + errorCode + " Error</title></head><body bgcolor='white'><center><h1>" + errorCode + " Error</h1></center><hr>" + version + "</body></html>").getBytes();
	}
	
	public byte[] getErrorJson() {
		byte[] ret = errorJsonMap.get(getErrorCode());
		return ret != null ? ret : Okv.of("state", "fail").set("msg", errorCode + " Error").toJson().getBytes();
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}





