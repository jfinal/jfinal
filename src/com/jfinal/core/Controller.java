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

package com.jfinal.core;

import java.io.File;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import static com.jfinal.core.Const.I18N_LOCALE;
import com.jfinal.i18n.I18N;
import com.jfinal.render.Render;
import com.jfinal.render.RenderFactory;
import com.jfinal.upload.MultipartRequest;
import com.jfinal.upload.UploadFile;
import com.jfinal.util.StringKit;

/**
 * Controller
 * <br>
 * 昨夜西风凋碧树。独上高楼，望尽天涯路。<br>
 * 衣带渐宽终不悔，为伊消得人憔悴。<br>
 * 众里寻她千百度，蓦然回首，那人却在灯火阑珊处。
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Controller {
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	private String urlPara;
	private String[] urlParaArray;
	
	private static final String[] NULL_URL_PARA_ARRAY = new String[0];
	private static final String URL_PARA_SEPARATOR = Config.getConstants().getUrlParaSeparator();
	
	void init(HttpServletRequest request, HttpServletResponse response, String urlPara) {
		this.request = request;
		this.response = response;
		this.urlPara = urlPara;
	}
	
	public void setUrlPara(String urlPara) {
		this.urlPara = urlPara;
		this.urlParaArray = null;
	}
	
	/**
	 * Stores an attribute in this request
	 * @param name a String specifying the name of the attribute
	 * @param value the Object to be stored
	 */
	public Controller setAttr(String name, Object value) {
		request.setAttribute(name, value);
		return this;
	}
	
	/**
	 * Removes an attribute from this request
	 * @param name a String specifying the name of the attribute to remove
	 */
	public Controller removeAttr(String name) {
		request.removeAttribute(name);
		return this;
	}
	
	/**
	 * Stores attributes in this request, key of the map as attribute name and value of the map as attribute value
	 * @param attrMap key and value as attribute of the map to be stored
	 */
	public Controller setAttrs(Map<String, Object> attrMap) {
		for (Map.Entry<String, Object> entry : attrMap.entrySet())
			request.setAttribute(entry.getKey(), entry.getValue());
		return this;
	}
	
	/**
	 * Returns the value of a request parameter as a String, or null if the parameter does not exist.
	 * <p>
	 * You should only use this method when you are sure the parameter has only one value. If the 
	 * parameter might have more than one value, use getParaValues(java.lang.String). 
	 * <p>
	 * If you use this method with a multivalued parameter, the value returned is equal to the first 
	 * value in the array returned by getParameterValues.
	 * @param name a String specifying the name of the parameter
	 * @return a String representing the single value of the parameter
	 */
	public String getPara(String name) {
		return request.getParameter(name);
	}
	
	/**
	 * Returns the value of a request parameter as a String, or default value if the parameter does not exist.
	 * @param name a String specifying the name of the parameter
	 * @param defaultValue a String value be returned when the value of parameter is null
	 * @return a String representing the single value of the parameter
	 */
	public String getPara(String name, String defaultValue) {
		String result = request.getParameter(name);
		return result != null && !"".equals(result) ? result : defaultValue;
	}
	
	/**
	 * Returns the values of the request parameters as a Map.
	 * @return a Map contains all the parameters name and value
	 */
	public Map<String, String[]> getParaMap() {
		return request.getParameterMap();
	}
	
	/**
	 * Returns an Enumeration of String objects containing the names of the parameters
	 * contained in this request. If the request has no parameters, the method returns
	 * an empty Enumeration.
	 * @return an Enumeration of String objects, each String containing the name of 
	 * 			a request parameter; or an empty Enumeration if the request has no parameters
	 */
	public Enumeration<String> getParaNames() {
		return request.getParameterNames();
	}
	
	/**
	 * Returns an array of String objects containing all of the values the given request 
	 * parameter has, or null if the parameter does not exist. If the parameter has a 
	 * single value, the array has a length of 1.
	 * @param name a String containing the name of the parameter whose value is requested
	 * @return an array of String objects containing the parameter's values
	 */
	public String[] getParaValues(String name) {
		return request.getParameterValues(name);
	}
	
	/**
	 * Returns an array of Integer objects containing all of the values the given request 
	 * parameter has, or null if the parameter does not exist. If the parameter has a 
	 * single value, the array has a length of 1.
	 * @param name a String containing the name of the parameter whose value is requested
	 * @return an array of Integer objects containing the parameter's values
	 */
	public Integer[] getParaValuesToInt(String name) {
		String[] values = request.getParameterValues(name);
		if (values == null)
			return null;
		Integer[] result = new Integer[values.length];
		for (int i=0; i<result.length; i++)
			result[i] = Integer.parseInt(values[i]);
		return result;
	}
	
	/**
	 * Returns an Enumeration containing the names of the attributes available to this request.
	 * This method returns an empty Enumeration if the request has no attributes available to it. 
	 * @return an Enumeration of strings containing the names of the request's attributes
	 */
	public Enumeration<String> getAttrNames() {
		return request.getAttributeNames();
	}
	
	/**
	 * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists.
	 * @param name a String specifying the name of the attribute
	 * @return an Object containing the value of the attribute, or null if the attribute does not exist
	 */
	public <T> T getAttr(String name) {
		return (T)request.getAttribute(name);
	}
	
	/**
	 * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists.
	 * @param name a String specifying the name of the attribute
	 * @return an String Object containing the value of the attribute, or null if the attribute does not exist
	 */
	public String getAttrForStr(String name) {
		return (String)request.getAttribute(name);
	}
	
	/**
	 * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists.
	 * @param name a String specifying the name of the attribute
	 * @return an Integer Object containing the value of the attribute, or null if the attribute does not exist
	 */
	public Integer getAttrForInt(String name) {
		return (Integer)request.getAttribute(name);
	}
	
	private Integer getParaToInt_(String result, Integer defaultValue) {
		if (result == null)
			return defaultValue;
		if (result.startsWith("N") || result.startsWith("n"))
			return -Integer.parseInt(result.substring(1));
		return Integer.parseInt(result);
	}
	
	/**
	 * Returns the value of a request parameter and convert to Integer.
	 * @param name a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Integer getParaToInt(String name) {
		return getParaToInt_(request.getParameter(name), null);
	}
	
	/**
	 * Returns the value of a request parameter and convert to Integer with a default value if it is null.
	 * @param name a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Integer getParaToInt(String name, Integer defaultValue) {
		return getParaToInt_(request.getParameter(name), defaultValue);
	}
	
	private Long getParaToLong_(String result, Long defaultValue) {
		if (result == null)
			return defaultValue;
		if (result.startsWith("N") || result.startsWith("n"))
			return -Long.parseLong(result.substring(1));
		return Long.parseLong(result);
	}
	
	/**
	 * Returns the value of a request parameter and convert to Long.
	 * @param name a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Long getParaToLong(String name) {
		return getParaToLong_(request.getParameter(name), null);
	}
	
	/**
	 * Returns the value of a request parameter and convert to Long with a default value if it is null.
	 * @param name a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Long getParaToLong(String name, Long defaultValue) {
		return getParaToLong_(request.getParameter(name), defaultValue);
	}
	
	/**
	 * Returns the value of a request parameter and convert to Boolean.
	 * @param name a String specifying the name of the parameter
	 * @return false if the value of the parameter is "false" or "0", true if it is "true" or "1", null if parameter is not exists
	 */
	public Boolean getParaToBoolean(String name) {
		String result = request.getParameter(name);
		if (result != null) {
			result = result.trim().toLowerCase();
			if (result.equals("1") || result.equals("true"))
				return Boolean.TRUE;
			else if (result.equals("0") || result.equals("false"))
				return Boolean.FALSE;
			// return Boolean.FALSE;	// if use this, delete 2 lines code under
		}
		return null;
	}
	
	/**
	 * Returns the value of a request parameter and convert to Boolean with a default value if it is null.
	 * @param name a String specifying the name of the parameter
	 * @return false if the value of the parameter is "false" or "0", true if it is "true" or "1", default value if it is null
	 */
	public Boolean getParaToBoolean(String name, Boolean defaultValue) {
		Boolean result = getParaToBoolean(name);
		return result != null ? result : defaultValue;
	}
	
	/**
	 * Return HttpServletRequest. Do not use HttpServletRequest Object in constructor of Controller
	 */
	public HttpServletRequest getRequest() {
		return request;
	}
	
	/**
	 * Return HttpServletResponse. Do not use HttpServletResponse Object in constructor of Controller
	 */
	public HttpServletResponse getResponse() {
		return response;
	}
	
	/**
	 * Return HttpSession.
	 */
	public HttpSession getSession() {
		return request.getSession();
	}
	
	/**
	 * Return HttpSession.
	 * @param create a boolean specifying create HttpSession if it not exists
	 */
	public HttpSession getSession(boolean create) {
		return request.getSession(create);
	}
	
	/**
	 * Return a Object from session.
	 * @param key a String specifying the key of the Object stored in session
	 */
	public <T> T getSessionAttr(String key) {
		HttpSession session = request.getSession(false);
		return session != null ? (T)session.getAttribute(key) : null;
	}
	
	/**
	 * Store Object to session.
	 * @param key a String specifying the key of the Object stored in session
	 * @param value a Object specifying the value stored in session
	 */
	public Controller setSessionAttr(String key, Object value) {
		request.getSession().setAttribute(key, value);
		return this;
	}
	
	/**
	 * Remove Object in session.
	 * @param key a String specifying the key of the Object stored in session
	 */
	public Controller removeSessionAttr(String key) {
		HttpSession session = request.getSession(false);
		if (session != null)
			session.removeAttribute(key);
		return this;
	}
	
	/**
	 * Get cookie value by cookie name.
	 */
	public String getCookie(String name, String defaultValue) {
		Cookie cookie = getCookieObject(name);
		return cookie != null ? cookie.getValue() : defaultValue;
	}
	
	/**
	 * Get cookie value by cookie name.
	 */
	public String getCookie(String name) {
		return getCookie(name, null);
	}
	
	/**
	 * Get cookie value by cookie name and convert to Integer.
	 */
	public Integer getCookieToInt(String name) {
		String result = getCookie(name);
		return result != null ? Integer.parseInt(result) : null;
	}
	
	/**
	 * Get cookie value by cookie name and convert to Integer.
	 */
	public Integer getCookieToInt(String name, Integer defaultValue) {
		String result = getCookie(name);
		return result != null ? Integer.parseInt(result) : defaultValue;
	}
	
	/**
	 * Get cookie value by cookie name and convert to Long.
	 */
	public Long getCookieToLong(String name) {
		String result = getCookie(name);
		return result != null ? Long.parseLong(result) : null;
	}
	
	/**
	 * Get cookie value by cookie name and convert to Long.
	 */
	public Long getCookieToLong(String name, Long defaultValue) {
		String result = getCookie(name);
		return result != null ? Long.parseLong(result) : defaultValue;
	}
	
	/**
	 * Get cookie object by cookie name.
	 */
	public Cookie getCookieObject(String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
			for (Cookie cookie : cookies)
				if (cookie.getName().equals(name))
					return cookie;
		return null;
	}
	
	/**
	 * Get all cookie objects.
	 */
	public Cookie[] getCookieObjects() {
		Cookie[] result = request.getCookies();
		return result != null ? result : new Cookie[0];
	}
	
	/**
	 * Set Cookie to response.
	 */
	public Controller setCookie(Cookie cookie) {
		response.addCookie(cookie);
		return this;
	}
	
	/**
	 * Set Cookie to response.
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
	 * @param path see Cookie.setPath(String)
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds, String path) {
		setCookie(name, value, maxAgeInSeconds, path, null);
		return this;
	}
	
	/**
	 * Set Cookie to response.
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
	 * @param path see Cookie.setPath(String)
	 * @param domain the domain name within which this cookie is visible; form is according to RFC 2109
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds, String path, String domain) {
		Cookie cookie = new Cookie(name, value);
		if (domain != null)
			cookie.setDomain(domain);
		cookie.setMaxAge(maxAgeInSeconds);
		cookie.setPath(path);
		response.addCookie(cookie);
		return this;
	}
	
	/**
	 * Set Cookie with path = "/".
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds) {
		setCookie(name, value, maxAgeInSeconds, "/", null);
		return this;
	}
	
	/**
	 * Remove Cookie with path = "/".
	 */
	public Controller removeCookie(String name) {
		setCookie(name, null, 0, "/", null);
		return this;
	}
	
	/**
	 * Remove Cookie.
	 */
	public Controller removeCookie(String name, String path) {
		setCookie(name, null, 0, path, null);
		return this;
	}
	
	/**
	 * Remove Cookie.
	 */
	public Controller removeCookie(String name, String path, String domain) {
		setCookie(name, null, 0, path, domain);
		return this;
	}
	
	// --------
	
	/**
	 * Get all para with separator char from url
	 */
	public String getPara() {
		if ("".equals(urlPara))	// urlPara maybe is "" see ActionMapping.getAction(String)
			urlPara = null;
		return urlPara;
	}
	
	/**
	 * Get para from url. The index of first url para is 0.
	 */
	public String getPara(int index) {
		if (index < 0)
			return getPara();
		
		if (urlParaArray == null) {
			if (urlPara == null || "".equals(urlPara))	// urlPara maybe is "" see ActionMapping.getAction(String)
				urlParaArray = NULL_URL_PARA_ARRAY;
			else
				urlParaArray = urlPara.split(URL_PARA_SEPARATOR);
			
			for (int i=0; i<urlParaArray.length; i++)
				if ("".equals(urlParaArray[i]))
					urlParaArray[i] = null;
		}
		return urlParaArray.length > index ? urlParaArray[index] : null;
	}
	
	/**
	 * Get para from url with default value if it is null or "".
	 */
	public String getPara(int index, String defaultValue) {
		String result = getPara(index);
		return result != null && !"".equals(result) ? result : defaultValue;
	}
	
	/**
	 * Get para from url and conver to Integer. The first index is 0
	 */
	public Integer getParaToInt(int index) {
		return getParaToInt_(getPara(index), null);
	}
	
	/**
	 * Get para from url and conver to Integer with default value if it is null.
	 */
	public Integer getParaToInt(int index, Integer defaultValue) {
		return getParaToInt_(getPara(index), defaultValue);
	}
	
	/**
	 * Get para from url and conver to Long.
	 */
	public Long getParaToLong(int index) {
		return getParaToLong_(getPara(index), null);
	}
	
	/**
	 * Get para from url and conver to Long with default value if it is null.
	 */
	public Long getParaToLong(int index, Long defaultValue) {
		return getParaToLong_(getPara(index), defaultValue);
	}
	
	/**
	 * Get all para from url and convert to Integer
	 */
	public Integer getParaToInt() {
		return getParaToInt_(getPara(), null);
	}
	
	/**
	 * Get all para from url and convert to Long
	 */
	public Long getParaToLong() {
		return getParaToLong_(getPara(), null);
	}
	
	/**
	 * Get model from http request.
	 */
	public <T> T getModel(Class<T> modelClass) {
		return (T)ModelInjector.inject(modelClass, request, false);
	}
	
	/**
	 * Get model from http request.
	 */
	public <T> T getModel(Class<T> modelClass, String modelName) {
		return (T)ModelInjector.inject(modelClass, modelName, request, false);
	}
	
	// TODO public <T> List<T> getModels(Class<T> modelClass, String modelName) {}
	
	// --------
	private MultipartRequest multipartRequest;
	
	/**
	 * Get upload file from multipart request.
	 */
	public List<UploadFile> getFiles(String saveDirectory, Integer maxPostSize, String encoding) {
		if (multipartRequest == null) {
			multipartRequest = new MultipartRequest(request, saveDirectory, maxPostSize, encoding);
			request = multipartRequest;
		}
		return multipartRequest.getFiles();
	}
	
	public UploadFile getFile(String parameterName, String saveDirectory, Integer maxPostSize, String encoding) {
		getFiles(saveDirectory, maxPostSize, encoding);
		return getFile(parameterName);
	}
	
	public List<UploadFile> getFiles(String saveDirectory, int maxPostSize) {
		if (multipartRequest == null) {
			multipartRequest = new MultipartRequest(request, saveDirectory, maxPostSize);
			request = multipartRequest;
		}
		return multipartRequest.getFiles();
	}
	
	public UploadFile getFile(String parameterName, String saveDirectory, int maxPostSize) {
		getFiles(saveDirectory, maxPostSize);
		return getFile(parameterName);
	}
	
	public List<UploadFile> getFiles(String saveDirectory) {
		if (multipartRequest == null) {
			multipartRequest = new MultipartRequest(request, saveDirectory);
			request = multipartRequest;
		}
		return multipartRequest.getFiles();
	}
	
	public UploadFile getFile(String parameterName, String saveDirectory) {
		getFiles(saveDirectory);
		return getFile(parameterName);
	}
	
	public List<UploadFile> getFiles() {
		if (multipartRequest == null) {
			multipartRequest = new MultipartRequest(request);
			request = multipartRequest;
		}
		return multipartRequest.getFiles();
	}
	
	public UploadFile getFile() {
		List<UploadFile> uploadFiles = getFiles();
		return uploadFiles.size() > 0 ? uploadFiles.get(0) : null;
	}
	
	public UploadFile getFile(String parameterName) {
		List<UploadFile> uploadFiles = getFiles();
		for (UploadFile uploadFile : uploadFiles) {
			if (uploadFile.getParameterName().equals(parameterName)) {
				return uploadFile;
			}
		}
		return null;
	}
	
	// i18n features --------
	/**
	 * Write Local to cookie
	 */
	public Controller setLocaleToCookie(Locale locale) {
		setCookie(I18N_LOCALE, locale.toString(), I18N.getI18nMaxAgeOfCookie());
		return this;
	}
	
	public Controller setLocaleToCookie(Locale locale, int maxAge) {
		setCookie(I18N_LOCALE, locale.toString(), maxAge);
		return this;
	}
	
	public String getText(String key) {
		return I18N.getText(key, getLocaleFromCookie());
	}
	
	public String getText(String key, String defaultValue) {
		return I18N.getText(key, defaultValue, getLocaleFromCookie());
	}
	
	private Locale getLocaleFromCookie() {
		Cookie cookie = getCookieObject(I18N_LOCALE);
		if (cookie != null) {
			return I18N.localeFromString(cookie.getValue());
		}
		else {
			Locale defaultLocale = I18N.getDefaultLocale();
			setLocaleToCookie(defaultLocale);
			return I18N.localeFromString(defaultLocale.toString());
		}
	}
	
	/**
	 * Keep all parameter's value except model value
	 */
	public Controller keepPara() {
		Map<String, String[]> map = request.getParameterMap();
		for (Entry<String, String[]> e: map.entrySet()) {
			String[] values = e.getValue();
			if (values.length == 1)
				request.setAttribute(e.getKey(), values[0]);
			else
				request.setAttribute(e.getKey(), values);
		}
		return this;
	}
	
	/**
	 * Keep parameter's value names pointed, model value can not be kept
	 */
	public Controller keepPara(String... names) {
		for (String name : names) {
			String[] values = request.getParameterValues(name);
			if (values != null) {
				if (values.length == 1)
					request.setAttribute(name, values[0]);
				else
					request.setAttribute(name, values);
			}
		}
		return this;
	}
	
	/**
	 * Convert para to special type and keep it
	 */
	public Controller keepPara(Class type, String name) {
		String[] values = request.getParameterValues(name);
		if (values != null) {
			if (values.length == 1)
				try {request.setAttribute(name, TypeConverter.convert(type, values[0]));} catch (ParseException e) {}
			else
				request.setAttribute(name, values);
		}
		return this;
	}
	
	public Controller keepPara(Class type, String... names) {
		if (type == String.class)
			return keepPara(names);
		
		if (names != null)
			for (String name : names)
				keepPara(type, name);
		return this;
	}
	
	public Controller keepModel(Class modelClass, String modelName) {
		Object model = ModelInjector.inject(modelClass, modelName, request, true);
		request.setAttribute(modelName, model);
		return this;
	}
	
	public Controller keepModel(Class modelClass) {
		String modelName = StringKit.firstCharToLowerCase(modelClass.getSimpleName());
		keepModel(modelClass, modelName);
		return this;
	}
	
	/**
	 * Create a token.
	 * @param tokenName the token name used in view
	 * @param secondsOfTimeOut the seconds of time out, secondsOfTimeOut >= Const.MIN_SECONDS_OF_TOKEN_TIME_OUT
	 */
	public void createToken(String tokenName, int secondsOfTimeOut) {
		com.jfinal.token.TokenManager.createToken(this, tokenName, secondsOfTimeOut);
	}
	
	/**
	 * Create a token with default token name ---> "token" and with default seconds of time out.
	 */
	public void createToken() {
		createToken(Const.DEFAULT_TOKEN_NAME, Const.DEFAULT_SECONDS_OF_TOKEN_TIME_OUT);
	}
	
	/**
	 * Check token to prevent resubmit.
	 * @param tokenName the token name used in view's form
	 * @return true if token is correct
	 */
	public boolean validateToken(String tokenName) {
		return com.jfinal.token.TokenManager.validateToken(this, tokenName);
	}
	
	/**
	 * Check token to prevent resubmit  with default token key ---> "JFINAL_TOKEN_KEY"
	 * @return true if token is correct
	 */
	public boolean validateToken() {
		return validateToken(Const.DEFAULT_TOKEN_NAME);
	}
	
	/**
	 * Return true if the para value is blank otherwise return false
	 */
	public boolean isParaBlank(String paraName) {
		String value = request.getParameter(paraName);
		return value == null || value.trim().length() == 0;
	}
	
	/**
	 * Return true if the urlPara value is blank otherwise return false
	 */
	public boolean isParaBlank(int index) {
		String value = getPara(index);
		return value == null || value.trim().length() == 0;
	}
	
	/**
	 * Return true if the para exists otherwise return false
	 */
	public boolean isParaExists(String paraName) {
		return request.getParameterMap().containsKey(paraName);
	}
	
	/**
	 * Return true if the urlPara exists otherwise return false
	 */
	public boolean isParaExists(int index) {
		return getPara(index) != null;
	}
	
	// ----------------
	// render below ---
	private static final RenderFactory renderFactory = RenderFactory.me();
	
	/**
	 * Hold Render object when invoke renderXxx(...)
	 */
	private Render render;
	
	public Render getRender() {
		return render;
	}
	
	/**
	 * Render with any Render which extends Render
	 */
	public void render(Render render) {
		this.render = render;
	}
	
	/**
	 * Render with view use default type Render configured in JFinalConfig
	 */
	public void render(String view) {
		render = renderFactory.getRender(view);
	}
	
	/**
	 * Render with jsp view
	 */
	public void renderJsp(String view) {
		render = renderFactory.getJspRender(view);
	}
	
	/**
	 * Render with freemarker view
	 */
	public void renderFreeMarker(String view) {
		render = renderFactory.getFreeMarkerRender(view);
	}
	
	/**
	 * Render with velocity view
	 */
	public void renderVelocity(String view) {
		render = renderFactory.getVelocityRender(view);
	}
	
	/**
	 * Render with json
	 * <p>
	 * Example:<br>
	 * renderJson("message", "Save successful");<br>
	 * renderJson("users", users);<br>
	 */
	public void renderJson(String key, Object value) {
		render = renderFactory.getJsonRender(key, value);
	}
	
	/**
	 * Render with json
	 */
	public void renderJson() {
		render = renderFactory.getJsonRender();
	}
	
	/**
	 * Render with attributes set by setAttr(...) before.
	 * <p>
	 * Example: renderJson(new String[]{"blogList", "user"});
	 */
	public void renderJson(String[] attrs) {
		render = renderFactory.getJsonRender(attrs);
	}
	
	/**
	 * Render with text. The contentType is: "text/plain".
	 */
	public void renderText(String text) {
		render = renderFactory.getTextRender(text);
	}
	
	/**
	 * Render with text and content type.
	 * <p>
	 * Example: renderText("<user id='5888'>James</user>", "application/xml");
	 */
	public void renderText(String text, String contentType) {
		render = renderFactory.getTextRender(text, contentType);
	}
	
	/**
	 * Forward to an action
	 */
	public void forwardAction(String actionUrl) {
		render = new ActionRender(actionUrl);
	}
	
	/**
	 * Render with file
	 */
	public void renderFile(String fileName) {
		render = renderFactory.getFileRender(fileName);
	}
	
	/**
	 * Render with file
	 */
	public void renderFile(File file) {
		render = renderFactory.getFileRender(file);
	}
	
	/**
	 * Redirect to url
	 */
	public void redirect(String url) {
		render = renderFactory.getRedirectRender(url);
	}
	
	/**
	 * Redirect to url
	 */
	public void redirect(String url, boolean withOutQueryString) {
		render = renderFactory.getRedirectRender(url, withOutQueryString);
	}
	
	/**
	 * Render with view and status use default type Render configured in JFinalConfig
	 */
	public void render(String view, int status) {
		render = renderFactory.getRender(view);
		response.setStatus(status);
	}
	
	/**
	 * Render with url and 301 status
	 */
	public void redirect301(String url) {
		render = renderFactory.getRedirect301Render(url);
	}
	
	/**
	 * Render with url and 301 status
	 */
	public void redirect301(String url, boolean withOutQueryString) {
		render = renderFactory.getRedirect301Render(url, withOutQueryString);
	}
	
	/**
	 * Render with view and 404 status
	 */
	public void renderError404(String view) {
		throw new com.jfinal.render.Error404Exception(renderFactory.getError404Render(view));
	}
	
	/**
	 * Render with view and 404 status configured in JFinalConfig
	 */
	public void renderError404() {
		throw new com.jfinal.render.Error404Exception(renderFactory.getError404Render());
	}
	
	/**
	 * Render with view and 500 status
	 */
	public void renderError500(String view) {
		throw new com.jfinal.render.Error500Exception(renderFactory.getError500Render(view));
	}
	
	/**
	 * Render with view and 500 status configured in JFinalConfig
	 */
	public void renderError500() {
		throw new com.jfinal.render.Error500Exception(renderFactory.getError500Render());
	}
	
	/**
	 * Render nothing, no response to browser
	 */
	public void renderNull() {
		render = renderFactory.getNullRender();
	}
	
	/**
	 * Render with javascript text. The contentType is: "text/javascript".
	 */
	public void renderJavascript(String javascriptText) {
		render = renderFactory.getJavascriptRender(javascriptText);
	}
	
	/**
	 * Render with html text. The contentType is: "text/html".
	 */
	public void renderHtml(String htmlText) {
		render = renderFactory.getHtmlRender(htmlText);
	}
}












