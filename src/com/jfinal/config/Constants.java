/**
 * Copyright (c) 2011-2016, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.config;

import java.util.HashMap;
import java.util.Map;
import com.jfinal.core.ActionReporter;
import com.jfinal.core.Const;
import com.jfinal.i18n.I18n;
import com.jfinal.json.IJsonFactory;
import com.jfinal.json.JsonManager;
import com.jfinal.kit.StrKit;
import com.jfinal.log.ILogFactory;
import com.jfinal.log.LogManager;
import com.jfinal.render.IErrorRenderFactory;
import com.jfinal.render.IMainRenderFactory;
import com.jfinal.render.IXmlRenderFactory;
import com.jfinal.render.RenderFactory;
import com.jfinal.render.ViewType;
import com.jfinal.token.ITokenCache;

/**
 * The constant for JFinal runtime.
 */
final public class Constants {
	
	private boolean devMode = Const.DEFAULT_DEV_MODE;
	
	private String baseUploadPath = Const.DEFAULT_BASE_UPLOAD_PATH;
	private String baseDownloadPath = Const.DEFAULT_BASE_DOWNLOAD_PATH;
	
	private String encoding = Const.DEFAULT_ENCODING;
	private String urlParaSeparator = Const.DEFAULT_URL_PARA_SEPARATOR;
	private ViewType viewType = Const.DEFAULT_VIEW_TYPE;
	private String jspViewExtension = Const.DEFAULT_JSP_EXTENSION;
	private String freeMarkerViewExtension = Const.DEFAULT_FREE_MARKER_EXTENSION;
	private String velocityViewExtension = Const.DEFAULT_VELOCITY_EXTENSION;
	private int maxPostSize = Const.DEFAULT_MAX_POST_SIZE;
	private int freeMarkerTemplateUpdateDelay = Const.DEFAULT_FREEMARKER_TEMPLATE_UPDATE_DELAY;	// just for not devMode
	
	private ITokenCache tokenCache = null;
	
	/**
	 * Set ITokenCache implementation otherwise JFinal will use the HttpSesion to hold the token.
	 * @param tokenCache the token cache
	 */
	public void setTokenCache(ITokenCache tokenCache) {
		this.tokenCache = tokenCache;
	}
	
	public ITokenCache getTokenCache() {
		return tokenCache;
	}
	
	/**
	 * Set development mode.
	 * @param devMode the development mode
	 */
	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
	
	/**
	 * Set encoding. The default encoding is UTF-8.
	 * @param encoding the encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public boolean getDevMode() {
		return devMode;
	}
	
	public String getUrlParaSeparator() {
		return urlParaSeparator;
	}
	
	public ViewType getViewType() {
		return viewType;
	}
	
	/**
	 * Set view type. The default value is ViewType.FREE_MARKER
	 * Controller.render(String view) will use the view type to render the view.
	 * @param viewType the view type 
	 */
	public void setViewType(ViewType viewType) {
		if (viewType == null) {
			throw new IllegalArgumentException("viewType can not be null");
		}
		if (viewType != ViewType.OTHER) {	// setMainRenderFactory will set ViewType.OTHER
			this.viewType = viewType;
		}
	}
	
	/**
	 * Set urlPara separator. The default value is "-"
	 * @param urlParaSeparator the urlPara separator
	 */
	public void setUrlParaSeparator(String urlParaSeparator) {
		if (StrKit.isBlank(urlParaSeparator) || urlParaSeparator.contains("/")) {
			throw new IllegalArgumentException("urlParaSepartor can not be blank and can not contains \"/\"");
		}
		this.urlParaSeparator = urlParaSeparator;
	}
	
	public String getJspViewExtension() {
		return jspViewExtension;
	}
	
	/**
	 * Set Jsp view extension. The default value is ".jsp"
	 * @param jspViewExtension the Jsp view extension
	 */
	public void setJspViewExtension(String jspViewExtension) {
		this.jspViewExtension = jspViewExtension.startsWith(".") ? jspViewExtension : "." + jspViewExtension;
	}
	
	public String getFreeMarkerViewExtension() {
		return freeMarkerViewExtension;
	}
	
	/**
	 * Set FreeMarker view extension. The default value is ".html" not ".ftl"
	 * @param freeMarkerViewExtension the FreeMarker view extension
	 */
	public void setFreeMarkerViewExtension(String freeMarkerViewExtension) {
		this.freeMarkerViewExtension = freeMarkerViewExtension.startsWith(".") ? freeMarkerViewExtension : "." + freeMarkerViewExtension;
	}
	
	public String getVelocityViewExtension() {
		return velocityViewExtension;
	}
	
	/**
	 * Set Velocity view extension. The default value is ".vm"
	 * @param velocityViewExtension the Velocity view extension
	 */
	public void setVelocityViewExtension(String velocityViewExtension) {
		this.velocityViewExtension = velocityViewExtension.startsWith(".") ? velocityViewExtension : "." + velocityViewExtension;
	}
	
	/**
	 * Set error 404 view.
	 * @param error404View the error 404 view
	 */
	public void setError404View(String error404View) {
		errorViewMapping.put(404, error404View);
	}
	
	/**
	 * Set error 500 view.
	 * @param error500View the error 500 view
	 */
	public void setError500View(String error500View) {
		errorViewMapping.put(500, error500View);
	}
	
	/**
	 * Set error 401 view.
	 * @param error401View the error 401 view
	 */
	public void setError401View(String error401View) {
		errorViewMapping.put(401, error401View);
	}
	
	/**
	 * Set error 403 view.
	 * @param error403View the error 403 view
	 */
	public void setError403View(String error403View) {
		errorViewMapping.put(403, error403View);
	}
	
	private Map<Integer, String> errorViewMapping = new HashMap<Integer, String>();
	
	public void setErrorView(int errorCode, String errorView) {
		errorViewMapping.put(errorCode, errorView);
	}
	
	public String getErrorView(int errorCode) {
		return errorViewMapping.get(errorCode);
	}
	
	public String getBaseDownloadPath() {
		return baseDownloadPath;
	}
	
	/**
	 * Set file base download path for Controller.renderFile(...)
	 * 设置文件下载基础路径，当路径以 "/" 打头或是以 windows 磁盘盘符打头，
	 * 则将路径设置为绝对路径，否则路径将是以应用根路径为基础的相对路径
	 * <pre>
	 * 例如：
	 * 1：参数 "/var/www/download" 为绝对路径，下载文件存放在此路径之下
	 * 2：参数 "download" 为相对路径，下载文件存放在 PathKit.getWebRoot() + "/download" 路径之下
	 * </pre>
	 */
	public void setBaseDownloadPath(String baseDownloadPath) {
		if (StrKit.isBlank(baseDownloadPath)) {
			throw new IllegalArgumentException("baseDownloadPath can not be blank.");
		}
		this.baseDownloadPath = baseDownloadPath;
	}
	
	/**
	 * Set file base upload path.
	 * 设置文件上传保存基础路径，当路径以 "/" 打头或是以 windows 磁盘盘符打头，
	 * 则将路径设置为绝对路径，否则路径将是以应用根路径为基础的相对路径
	 * <pre>
	 * 例如：
	 * 1：参数 "/var/www/upload" 为绝对路径，上传文件将保存到此路径之下
	 * 2：参数 "upload" 为相对路径，上传文件将保存到 PathKit.getWebRoot() + "/upload" 路径之下
	 * </pre>
	 */
	public void setBaseUploadPath(String baseUploadPath) {
		if (StrKit.isBlank(baseUploadPath)) {
			throw new IllegalArgumentException("baseUploadPath can not be blank.");
		}
		this.baseUploadPath = baseUploadPath;
	}
	
	public String getBaseUploadPath() {
		return baseUploadPath;
	}
	
	public int getMaxPostSize() {
		return maxPostSize;
	}
	
	/**
	 * Set max size of http post. The upload file size depend on this value.
	 */
	public void setMaxPostSize(int maxPostSize) {
		this.maxPostSize = maxPostSize;
	}
	
	/**
	 * Set default base name to load Resource bundle.
	 * The default value is "i18n".<tr>
	 * Example:
	 * setI18nDefaultBaseName("i18n");
	 */
	public void setI18nDefaultBaseName(String defaultBaseName) {
		I18n.setDefaultBaseName(defaultBaseName);
	}
	
	/**
	 * Set default locale to load Resource bundle.
	 * The locale string like this: "zh_CN" "en_US".<br>
	 * Example:
	 * setI18nDefaultLocale("zh_CN");
	 */
	public void setI18nDefaultLocale(String defaultLocale) {
		I18n.setDefaultLocale(defaultLocale);
	}
	
	/**
	 * FreeMarker template update delay for not devMode.
	 */
	public void setFreeMarkerTemplateUpdateDelay(int delayInSeconds) {
		if (delayInSeconds < 0) {
			throw new IllegalArgumentException("template_update_delay must more than -1.");
		}
		this.freeMarkerTemplateUpdateDelay = delayInSeconds;
	}
	
	public int getFreeMarkerTemplateUpdateDelay() {
		return freeMarkerTemplateUpdateDelay;
	}
	
	/**
	 * Set the base path for all views
	 */
	public void setBaseViewPath(String baseViewPath) {
		Routes.setBaseViewPath(baseViewPath);
	}
	
	/**
	 * Set the mainRenderFactory then your can use your custom render in controller as render(String).
	 */
	public void setMainRenderFactory(IMainRenderFactory mainRenderFactory) {
		if (mainRenderFactory == null) {
			throw new IllegalArgumentException("mainRenderFactory can not be null.");
		}
		this.viewType = ViewType.OTHER;
		RenderFactory.me().setMainRenderFactory(mainRenderFactory);
	}
	
	public void setLogFactory(ILogFactory logFactory) {
		if (logFactory == null) {
			throw new IllegalArgumentException("logFactory can not be null.");
		}
		LogManager.me().setDefaultLogFactory(logFactory);
	}
	
	public void setErrorRenderFactory(IErrorRenderFactory errorRenderFactory) {
		if (errorRenderFactory == null) {
			throw new IllegalArgumentException("errorRenderFactory can not be null.");
		}
		RenderFactory.me().setErrorRenderFactory(errorRenderFactory);
	}
	
	public void setXmlRenderFactory(IXmlRenderFactory xmlRenderFactory) {
		if (xmlRenderFactory == null) {
			throw new IllegalArgumentException("xmlRenderFactory can not be null.");
		}
		RenderFactory.me().setXmlRenderFactory(xmlRenderFactory);
	}
	
	/**
	 * 设置 Json 转换工厂实现类，目前支持：JFinalJsonFactory(默认)、JacksonFactory、FastJsonFactory
	 * 分别支持 JFinalJson、Jackson、FastJson
	 */
	public void setJsonFactory(IJsonFactory jsonFactory) {
		if (jsonFactory == null) {
			throw new IllegalArgumentException("jsonFactory can not be null.");
		}
		JsonManager.me().setDefaultJsonFactory(jsonFactory);
	}
	
	/**
	 * 设置json转换时日期格式，常用格式有："yyyy-MM-dd HH:mm:ss"、 "yyyy-MM-dd"
	 */
	public void setJsonDatePattern(String datePattern) {
		if (StrKit.isBlank(datePattern)) {
			throw new IllegalArgumentException("datePattern can not be blank.");
		}
		JsonManager.me().setDefaultDatePattern(datePattern);
	}
	
	/**
	 * 设置 devMode 之下的 action report 是否在 invocation 之后，默认值为 true
	 */
	public void setReportAfterInvocation(boolean reportAfterInvocation) {
		ActionReporter.setReportAfterInvocation(reportAfterInvocation);
	}
}





