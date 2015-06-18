/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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
import java.util.Locale;
import javax.servlet.ServletContext;
import com.jfinal.config.Constants;
import com.jfinal.kit.PathKit;
import static com.jfinal.core.Const.DEFAULT_FILE_RENDER_BASE_PATH;

/**
 * RenderFactory.
 */
public class RenderFactory {
	
	private Constants constants;
	private static IMainRenderFactory mainRenderFactory;
	private static IErrorRenderFactory errorRenderFactory;
	private static ServletContext servletContext;
	
	static ServletContext getServletContext() {
		return servletContext;
	}
	
	private static final RenderFactory me = new RenderFactory();
	
	private RenderFactory() {
		
	}
	
	public static RenderFactory me() {
		return me;
	}
	
	public static void setMainRenderFactory(IMainRenderFactory mainRenderFactory) {
		if (mainRenderFactory != null)
			RenderFactory.mainRenderFactory = mainRenderFactory;
	}
	
	public static void setErrorRenderFactory(IErrorRenderFactory errorRenderFactory) {
		if (errorRenderFactory != null)
			RenderFactory.errorRenderFactory = errorRenderFactory;
	}
	
	public void init(Constants constants, ServletContext servletContext) {
		this.constants = constants;
		RenderFactory.servletContext = servletContext;
		
		// init Render
		Render.init(constants.getEncoding(), constants.getDevMode());
		initFreeMarkerRender(servletContext);
		initVelocityRender(servletContext);
		initJspRender(servletContext);
		initFileRender(servletContext);
		
		// create mainRenderFactory
		if (mainRenderFactory == null) {
			ViewType defaultViewType = constants.getViewType();
			if (defaultViewType == ViewType.FREE_MARKER)
				mainRenderFactory = new FreeMarkerRenderFactory();
			else if (defaultViewType == ViewType.JSP)
				mainRenderFactory = new JspRenderFactory();
			else if (defaultViewType == ViewType.VELOCITY)
				mainRenderFactory = new VelocityRenderFactory();
			else
				throw new RuntimeException("View Type can not be null.");
		}
		
		// create errorRenderFactory
		if (errorRenderFactory == null) {
			errorRenderFactory = new ErrorRenderFactory();
		}
	}
	
	private void initFreeMarkerRender(ServletContext servletContext) {
		try {
			Class.forName("freemarker.template.Template");	// detect freemarker.jar
			FreeMarkerRender.init(servletContext, Locale.getDefault(), constants.getFreeMarkerTemplateUpdateDelay());
		} catch (ClassNotFoundException e) {
			// System.out.println("freemarker can not be supported!");
		}
	}
	
	private void initVelocityRender(ServletContext servletContext) {
		try {
			Class.forName("org.apache.velocity.VelocityContext");
			VelocityRender.init(servletContext);
		}
		catch (ClassNotFoundException e) {
			// System.out.println("Velocity can not be supported!");
		}
	}
	
	private void initJspRender(ServletContext servletContext) {
		try {
			Class.forName("javax.el.ELResolver");
			Class.forName("javax.servlet.jsp.JspFactory");
			com.jfinal.plugin.activerecord.ModelRecordElResolver.init(servletContext);
		}
		catch (ClassNotFoundException e) {
			// System.out.println("Jsp or JSTL can not be supported!");
		}
		catch (IllegalStateException e) {
			throw e;
		}
		catch (Exception e) {
			
		}
	}
	
	private void initFileRender(ServletContext servletContext) {
		FileRender.init(getFileRenderPath(), servletContext);
	}
	
	private String getFileRenderPath() {
		String result = constants.getFileRenderPath();
		if (result == null) {
			result = PathKit.getWebRootPath() + DEFAULT_FILE_RENDER_BASE_PATH;
		}
		if (!result.endsWith(File.separator) && !result.endsWith("/")) {
			result = result + File.separator;
		}
		return result;
	}
	
	/**
	 * Return Render by default ViewType which config in JFinalConfig
	 */
	public Render getRender(String view) {
		return mainRenderFactory.getRender(view);
	}
	
	public Render getFreeMarkerRender(String view) {
		return new FreeMarkerRender(view);
	}
	
	public Render getJspRender(String view) {
		return new JspRender(view);
	}
	
	public Render getVelocityRender(String view) {
		return new VelocityRender(view);
	}
	
	public Render getJsonRender() {
		return new JsonRender();
	}
	
	public Render getJsonRender(String key, Object value) {
		return new JsonRender(key, value);
	}
	
	public Render getJsonRender(String[] attrs) {
		return new JsonRender(attrs);
	}
	
	public Render getJsonRender(String jsonText) {
		return new JsonRender(jsonText);
	}
	
	public Render getJsonRender(Object object) {
		return new JsonRender(object);
	}
	
	public Render getTextRender(String text) {
		return new TextRender(text);
	}
	
	public Render getTextRender(String text, String contentType) {
		return new TextRender(text, contentType);
	}
	
	public Render getTextRender(String text, ContentType contentType) {
		return new TextRender(text, contentType);
	}
	
	public Render getDefaultRender(String view) {
		ViewType viewType = constants.getViewType();
		if (viewType == ViewType.FREE_MARKER) {
			return new FreeMarkerRender(view + constants.getFreeMarkerViewExtension());
		}
		else if (viewType == ViewType.JSP) {
			return new JspRender(view + constants.getJspViewExtension());
		}
		else if (viewType == ViewType.VELOCITY) {
			return new VelocityRender(view + constants.getVelocityViewExtension());
		}
		else {
			return mainRenderFactory.getRender(view + mainRenderFactory.getViewExtension());
		}
	}
	
	public Render getErrorRender(int errorCode, String view) {
		return errorRenderFactory.getRender(errorCode, view);
	}
	
	public Render getErrorRender(int errorCode) {
		return errorRenderFactory.getRender(errorCode, constants.getErrorView(errorCode));
	}
	
	public Render getFileRender(String fileName) {
		return new FileRender(fileName);
	}
	
	public Render getFileRender(File file) {
		return new FileRender(file);
	}
	
	public Render getRedirectRender(String url) {
		return new RedirectRender(url);
	}
	
	public Render getRedirectRender(String url, boolean withQueryString) {
		return new RedirectRender(url, withQueryString);
	}
	
	public Render getRedirect301Render(String url) {
		return new Redirect301Render(url);
	}
	
	public Render getRedirect301Render(String url, boolean withQueryString) {
		return new Redirect301Render(url, withQueryString);
	}
	
	public Render getNullRender() {
		return new NullRender();
	}
	
	public Render getJavascriptRender(String jsText) {
		return new JavascriptRender(jsText);
	}
	
	public Render getHtmlRender(String htmlText) {
		return new HtmlRender(htmlText);
	}
	
	public Render getXmlRender(String view) {
		return new XmlRender(view);
	}
	
	// --------
	private static final class FreeMarkerRenderFactory implements IMainRenderFactory {
		public Render getRender(String view) {
			return new FreeMarkerRender(view);
		}
		public String getViewExtension() {
			return ".html";
		}
	}
	
	private static final class JspRenderFactory implements IMainRenderFactory {
		public Render getRender(String view) {
			return new JspRender(view);
		}
		public String getViewExtension() {
			return ".jsp";
		}
	}
	
	private static final class VelocityRenderFactory implements IMainRenderFactory {
		public Render getRender(String view) {
			return new VelocityRender(view);
		}
		public String getViewExtension() {
			return ".html";
		}
	}
	
	private static final class ErrorRenderFactory implements IErrorRenderFactory {
		public Render getRender(int errorCode, String view) {
			return new ErrorRender(errorCode, view);
		}
	}
}


