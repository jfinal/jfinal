/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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
import com.jfinal.util.PathUtil;
import static com.jfinal.core.Const.DEFAULT_FILE_RENDER_BASE_PATH;

/**
 * RenderFactory.
 */
public class RenderFactory {
	
	private Constants constants;
	private static IMainRenderFactory mainRenderFactory;
	private static ServletContext servletContext;
	
	static ServletContext getServletContext() {
		return servletContext;
	}
	
	// singleton
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
	
	public void init(Constants constants, ServletContext servletContext) {
		this.constants = constants;
		RenderFactory.servletContext = servletContext;
		
		// init Render
		Render.init(constants.getEncoding(), constants.getDevMode());
		initFreeMarkerRender(servletContext);
		initVelocityRender(servletContext);
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
	
	private void initFileRender(ServletContext servletContext) {
		FileRender.init(getFileRenderPath(), servletContext);
	}
	
	private String getFileRenderPath() {
		String result = constants.getFileRenderPath();
		if (result == null) {
			result = PathUtil.getWebRootPath() + DEFAULT_FILE_RENDER_BASE_PATH;
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
	
	public Render getTextRender(String text) {
		return new TextRender(text);
	}
	
	public Render getTextRender(String text, String contentType) {
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
	
	public Render getError404Render() {
		String error404View = constants.getError404View();
		return error404View != null ? new Error404Render(error404View) : new Error404Render();
	}
	
	public Render getError404Render(String view) {
		return new Error404Render(view);
	}
	
	public Render getError500Render() {
		String error500View = constants.getError500View();
		return error500View != null ? new Error500Render(error500View) : new Error500Render();
	}
	
	public Render getError500Render(String view) {
		return new Error500Render(view);
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
	
	public Render getRedirectRender(String url, boolean withOutQueryString) {
		return new RedirectRender(url, withOutQueryString);
	}
	
	public Render getRedirect301Render(String url) {
		return new Redirect301Render(url);
	}
	
	public Render getRedirect301Render(String url, boolean withOutQueryString) {
		return new Redirect301Render(url, withOutQueryString);
	}
	
	public Render getNullRender() {
		return NullRender.me();
	}
	
	public Render getJavascriptRender(String jsText) {
		return new JavascriptRender(jsText);
	}
	
	public Render getHtmlRender(String htmlText) {
		return new HtmlRender(htmlText);
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
}


