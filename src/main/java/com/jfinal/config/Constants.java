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

package com.jfinal.config;

import java.util.HashMap;
import java.util.Map;
import com.jfinal.captcha.CaptchaManager;
import com.jfinal.captcha.ICaptchaCache;
import com.jfinal.core.ActionReporter;
import com.jfinal.core.Const;
import com.jfinal.i18n.I18n;
import com.jfinal.json.IJsonFactory;
import com.jfinal.json.JsonManager;
import com.jfinal.kit.StrKit;
import com.jfinal.log.ILogFactory;
import com.jfinal.log.LogManager;
import com.jfinal.render.IRenderFactory;
import com.jfinal.render.RenderManager;
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
	private String viewExtension = Const.DEFAULT_VIEW_EXTENSION;
	private int maxPostSize = Const.DEFAULT_MAX_POST_SIZE;
	private int freeMarkerTemplateUpdateDelay = Const.DEFAULT_FREEMARKER_TEMPLATE_UPDATE_DELAY;	// just for not devMode
	
	private ITokenCache tokenCache = null;
	
	/**
	 * Set development mode.
	 * @param devMode the development mode
	 */
	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
	
	public boolean getDevMode() {
		return devMode;
	}
	
	/**
	 * Set the renderFactory
	 */
	public void setRenderFactory(IRenderFactory renderFactory) {
		if (renderFactory == null) {
			throw new IllegalArgumentException("renderFactory can not be null.");
		}
		RenderManager.me().setRenderFactory(renderFactory);
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
	
	public void setCaptchaCache(ICaptchaCache captchaCache) {
		CaptchaManager.me().setCaptchaCache(captchaCache);
	}
	
	public void setLogFactory(ILogFactory logFactory) {
		if (logFactory == null) {
			throw new IllegalArgumentException("logFactory can not be null.");
		}
		LogManager.me().setDefaultLogFactory(logFactory);
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
	
	public String getUrlParaSeparator() {
		return urlParaSeparator;
	}
	
	public ViewType getViewType() {
		return viewType;
	}
	
	/**
	 * Set view type. The default value is ViewType.JFINAL_TEMPLATE
	 * Controller.render(String view) will use the view type to render the view.
	 * @param viewType the view type 
	 */
	public void setViewType(ViewType viewType) {
		if (viewType == null) {
			throw new IllegalArgumentException("viewType can not be null");
		}
		this.viewType = viewType;
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
	
	public String getViewExtension() {
		return viewExtension;
	}
	
	/**
	 * Set view extension for the IRenderFactory.getDefaultRender(...)
	 * The default value is ".html"
	 * 
	 * Example: ".html" or ".ftl"
	 * @param viewExtension the extension of the view, it must start with dot char "."
	 */
	public void setViewExtension(String viewExtension) {
		this.viewExtension = viewExtension.startsWith(".") ? viewExtension : "." + viewExtension;
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
	 * 设置 devMode 之下的 action report 是否在 invocation 之后，默认值为 true
	 */
	public void setReportAfterInvocation(boolean reportAfterInvocation) {
		ActionReporter.setReportAfterInvocation(reportAfterInvocation);
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
}





