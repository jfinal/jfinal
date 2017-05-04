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

package com.jfinal.render;

import java.io.File;
import javax.servlet.ServletContext;
import com.jfinal.config.Constants;
import com.jfinal.template.Engine;

/**
 * IRenderFactory.
 */
public interface IRenderFactory {
	
	public void init(Engine engine, Constants constants, ServletContext servletContext);
	
	/**
	 * Create Render for Controller.render(String view)
	 */
	public Render getRender(String view);
	
	public Render getTemplateRender(String view);
	
	public Render getFreeMarkerRender(String view);
	
	public Render getJspRender(String view);
	
	public Render getVelocityRender(String view);
	
	public Render getJsonRender();
	
	public Render getJsonRender(String key, Object value);
	
	public Render getJsonRender(String[] attrs);
	
	public Render getJsonRender(String jsonText);
	
	public Render getJsonRender(Object object);
	
	public Render getTextRender(String text);
	
	public Render getTextRender(String text, String contentType);
	
	public Render getTextRender(String text, ContentType contentType);
	
	public Render getDefaultRender(String view);
	
	public Render getErrorRender(int errorCode, String view);
	
	public Render getErrorRender(int errorCode);
	
	public Render getFileRender(String fileName);
	
	public Render getFileRender(String fileName, String downloadFileName);
	
	public Render getFileRender(File file);
	
	public Render getFileRender(File file, String downloadFileName);
	
	public Render getRedirectRender(String url);
	
	public Render getRedirectRender(String url, boolean withQueryString);
	
	public Render getRedirect301Render(String url);
	
	public Render getRedirect301Render(String url, boolean withQueryString);
	
	public Render getNullRender();
	
	public Render getJavascriptRender(String jsText);
	
	public Render getHtmlRender(String htmlText);
	
	public Render getXmlRender(String view);
	
	public Render getCaptchaRender();
	
	public Render getQrCodeRender(String content, int width, int height);
	
	public Render getQrCodeRender(String content, int width, int height, char errorCorrectionLevel);
}


