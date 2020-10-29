/**
 * Copyright (c) 2011-2021, James Zhan 詹波 (jfinal@126.com).
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
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.jfinal.core.converter.TypeConverter;
import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.jfinal.render.ContentType;
import com.jfinal.render.JsonRender;
import com.jfinal.render.Render;
import com.jfinal.render.RenderManager;
import com.jfinal.upload.MultipartRequest;
import com.jfinal.upload.UploadFile;

/**
 * Controller
 * <br>
 * 昨夜西风凋碧树。独上高楼，望尽天涯路。<br>
 * 衣带渐宽终不悔，为伊消得人憔悴。<br>
 * 众里寻她千百度，蓦然回首，那人却在灯火阑珊处。
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Controller {
	
	// 可通过 CPI.getAction(Controller) 获取
	Action action;
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	private String urlPara;
	private String[] urlParaArray;
	private String rawData;
	
	private Render render;
	
	private static final RenderManager renderManager = RenderManager.me();
	
	private static final String[] NULL_URL_PARA_ARRAY = new String[0];
	private static final String URL_PARA_SEPARATOR = Config.getConstants().getUrlParaSeparator();
	
	void _init_(Action action, HttpServletRequest request, HttpServletResponse response, String urlPara) {
		this.action = action;
		this.request = request;
		this.response = response;
		this.urlPara = urlPara;
		
		/**
		*把getPara(int index)方法里面的重复处理移到上面只处理一次，不必每次调用getPara(xx)的时候，都要重新赋值一遍
		*/
		urlParaArray = StrKit.isBlank(urlPara)?NULL_URL_PARA_ARRAY:urlPara.split(URL_PARA_SEPARATOR);
		for (int i=0; i<urlParaArray.length; i++){
			if ("".equals(urlParaArray[i])) urlParaArray[i] = null;	
		}	
		render = null;
	}
	
	/**
	 * 在对 Controller 回收使用场景下，如果继承类中声明了属性，则必须要
	 * 覆盖此方法，调用父类的 clear() 方法并清掉自身的属性，例如：
	 * 
	 * super._clear_();
	 * this.xxx = null;
	 */
	protected void _clear_() {
		action = null;
		request = null;
		response = null;
		urlPara = null;
		urlParaArray = null;
		render = null;
		rawData = null;
	}
	
	/**
	 * 获取 http 请求 body 中的原始数据，通常用于接收 json String 这类数据<br>
	 * 可多次调用此方法，避免掉了 HttpKit.readData(...) 方式获取该数据时多次调用
	 * 引发的异常
	 * @return http 请求 body 中的原始数据
	 */
	public String getRawData() {
		if (rawData == null) {
			rawData = com.jfinal.kit.HttpKit.readData(request);
		}
		return rawData;
	}
	
	public String getControllerKey() {
		return action.getControllerKey();
	}
	
	public String getViewPath() {
		return action.getViewPath();
	}
	
	public void setHttpServletRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	public void setHttpServletResponse(HttpServletResponse response) {
		this.response = response;
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
		// return request.getParameter(name);
		String result = request.getParameter(name);
		return result != null && result.length() != 0 ? result : null;
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
		if (values == null || values.length == 0) {
			return null;
		}
		Integer[] result = new Integer[values.length];
		for (int i=0; i<result.length; i++) {
			result[i] = StrKit.isBlank(values[i]) ? null : Integer.parseInt(values[i]);
		}
		return result;
	}
	
	public Long[] getParaValuesToLong(String name) {
		String[] values = request.getParameterValues(name);
		if (values == null || values.length == 0) {
			return null;
		}
		Long[] result = new Long[values.length];
		for (int i=0; i<result.length; i++) {
			result[i] = StrKit.isBlank(values[i]) ? null : Long.parseLong(values[i]);
		}
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
	
	public <T> T getAttr(String name, T defaultValue) {
		T result = (T)request.getAttribute(name);
		return result != null ? result : defaultValue;
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
	
	/**
	 * Returns the value of the specified request header as a String.
	 */
	public String getHeader(String name) {
		return request.getHeader(name);
	}
	
	private Integer toInt(String value, Integer defaultValue) {
		try {
			if (StrKit.isBlank(value))
				return defaultValue;
			value = value.trim();
			if (value.startsWith("N") || value.startsWith("n"))
				return -Integer.parseInt(value.substring(1));
			return Integer.parseInt(value);
		}
		catch (Exception e) {
			throw new ActionException(400, renderManager.getRenderFactory().getErrorRender(400),  "Can not parse the parameter \"" + value + "\" to Integer value.");
		}
	}
	
	/**
	 * Returns the value of a request parameter and convert to Integer.
	 * @param name a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Integer getParaToInt(String name) {
		return toInt(request.getParameter(name), null);
	}
	
	/**
	 * Returns the value of a request parameter and convert to Integer with a default value if it is null.
	 * @param name a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Integer getParaToInt(String name, Integer defaultValue) {
		return toInt(request.getParameter(name), defaultValue);
	}
	
	private Long toLong(String value, Long defaultValue) {
		try {
			if (StrKit.isBlank(value))
				return defaultValue;
			value = value.trim();
			if (value.startsWith("N") || value.startsWith("n"))
				return -Long.parseLong(value.substring(1));
			return Long.parseLong(value);
		}
		catch (Exception e) {
			throw new ActionException(400, renderManager.getRenderFactory().getErrorRender(400),  "Can not parse the parameter \"" + value + "\" to Long value.");
		}
	}
	
	/**
	 * Returns the value of a request parameter and convert to Long.
	 * @param name a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Long getParaToLong(String name) {
		return toLong(request.getParameter(name), null);
	}
	
	/**
	 * Returns the value of a request parameter and convert to Long with a default value if it is null.
	 * @param name a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Long getParaToLong(String name, Long defaultValue) {
		return toLong(request.getParameter(name), defaultValue);
	}
	
	private Boolean toBoolean(String value, Boolean defaultValue) {
		if (StrKit.isBlank(value))
			return defaultValue;
		value = value.trim().toLowerCase();
		if ("1".equals(value) || "true".equals(value))
			return Boolean.TRUE;
		else if ("0".equals(value) || "false".equals(value))
			return Boolean.FALSE;
		throw new ActionException(400, renderManager.getRenderFactory().getErrorRender(400), "Can not parse the parameter \"" + value + "\" to Boolean value.");
	}
	
	/**
	 * Returns the value of a request parameter and convert to Boolean.
	 * @param name a String specifying the name of the parameter
	 * @return true if the value of the parameter is "true" or "1", false if it is "false" or "0", null if parameter is not exists
	 */
	public Boolean getParaToBoolean(String name) {
		return toBoolean(request.getParameter(name), null);
	}
	
	/**
	 * Returns the value of a request parameter and convert to Boolean with a default value if it is null.
	 * @param name a String specifying the name of the parameter
	 * @return true if the value of the parameter is "true" or "1", false if it is "false" or "0", default value if it is null
	 */
	public Boolean getParaToBoolean(String name, Boolean defaultValue) {
		return toBoolean(request.getParameter(name), defaultValue);
	}
	
	/**
	 * Get all para from url and convert to Boolean
	 */
	public Boolean getParaToBoolean() {
		return toBoolean(getPara(), null);
	}
	
	/**
	 * Get para from url and conver to Boolean. The first index is 0
	 */
	public Boolean getParaToBoolean(int index) {
		return toBoolean(getPara(index), null);
	}
	
	/**
	 * Get para from url and conver to Boolean with default value if it is null.
	 */
	public Boolean getParaToBoolean(int index, Boolean defaultValue) {
		return toBoolean(getPara(index), defaultValue);
	}
	
	private Date toDate(String value, Date defaultValue) {
		try {
			if (StrKit.isBlank(value))
				return defaultValue;
			
			// return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(value.trim());
			return (Date)TypeConverter.me().convert(Date.class, value);
			
		} catch (Exception e) {
			throw new ActionException(400, renderManager.getRenderFactory().getErrorRender(400),  "Can not parse the parameter \"" + value + "\" to Date value.");
		}
	}
	
	/**
	 * Returns the value of a request parameter and convert to Date.
	 * @param name a String specifying the name of the parameter
	 * @return a Date representing the single value of the parameter
	 */
	public Date getParaToDate(String name) {
		return toDate(request.getParameter(name), null);
	}
	
	/**
	 * Returns the value of a request parameter and convert to Date with a default value if it is null.
	 * @param name a String specifying the name of the parameter
	 * @return a Date representing the single value of the parameter
	 */
	public Date getParaToDate(String name, Date defaultValue) {
		return toDate(request.getParameter(name), defaultValue);
	}
	
	/**
	 * Get all para from url and convert to Date
	 */
	public Date getParaToDate() {
		return toDate(getPara(), null);
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
	
	public <T> T getSessionAttr(String key, T defaultValue) {
		T result = getSessionAttr(key);
		return result != null ? result : defaultValue;
	}
	
	/**
	 * Store Object to session.
	 * @param key a String specifying the key of the Object stored in session
	 * @param value a Object specifying the value stored in session
	 */
	public Controller setSessionAttr(String key, Object value) {
		request.getSession(true).setAttribute(key, value);
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
	 * Set Cookie.
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
	 * @param isHttpOnly true if this cookie is to be marked as HttpOnly, false otherwise
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds, boolean isHttpOnly) {
		return doSetCookie(name, value, maxAgeInSeconds, null, null, isHttpOnly);
	}
	
	/**
	 * Set Cookie.
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds) {
		return doSetCookie(name, value, maxAgeInSeconds, null, null, null);
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
	 * @param isHttpOnly true if this cookie is to be marked as HttpOnly, false otherwise
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds, String path, boolean isHttpOnly) {
		return doSetCookie(name, value, maxAgeInSeconds, path, null, isHttpOnly);
	}
	
	/**
	 * Set Cookie to response.
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
	 * @param path see Cookie.setPath(String)
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds, String path) {
		return doSetCookie(name, value, maxAgeInSeconds, path, null, null);
	}
	
	/**
	 * Set Cookie to response.
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
	 * @param path see Cookie.setPath(String)
	 * @param domain the domain name within which this cookie is visible; form is according to RFC 2109
	 * @param isHttpOnly true if this cookie is to be marked as HttpOnly, false otherwise
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds, String path, String domain, boolean isHttpOnly) {
		return doSetCookie(name, value, maxAgeInSeconds, path, domain, isHttpOnly);
	}
	
	/**
	 * Remove Cookie.
	 */
	public Controller removeCookie(String name) {
		return doSetCookie(name, null, 0, null, null, null);
	}
	
	/**
	 * Remove Cookie.
	 */
	public Controller removeCookie(String name, String path) {
		return doSetCookie(name, null, 0, path, null, null);
	}
	
	/**
	 * Remove Cookie.
	 */
	public Controller removeCookie(String name, String path, String domain) {
		return doSetCookie(name, null, 0, path, domain, null);
	}
	
	private Controller doSetCookie(String name, String value, int maxAgeInSeconds, String path, String domain, Boolean isHttpOnly) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAgeInSeconds);
		// set the default path value to "/"
		if (path == null) {
			path = "/";
		}
		cookie.setPath(path);
		
		if (domain != null) {
			cookie.setDomain(domain);
		}
		if (isHttpOnly != null) {
			cookie.setHttpOnly(isHttpOnly);
		}
		response.addCookie(cookie);
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
		if (index < 0) return getPara();
		//把以前的重复处理split的过程移到到初始化函数里，避免重复操作
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
		return toInt(getPara(index), null);
	}
	
	/**
	 * Get para from url and conver to Integer with default value if it is null.
	 */
	public Integer getParaToInt(int index, Integer defaultValue) {
		return toInt(getPara(index), defaultValue);
	}
	
	/**
	 * Get para from url and conver to Long.
	 */
	public Long getParaToLong(int index) {
		return toLong(getPara(index), null);
	}
	
	/**
	 * Get para from url and conver to Long with default value if it is null.
	 */
	public Long getParaToLong(int index, Long defaultValue) {
		return toLong(getPara(index), defaultValue);
	}
	
	/**
	 * Get all para from url and convert to Integer
	 */
	public Integer getParaToInt() {
		return toInt(getPara(), null);
	}
	
	/**
	 * Get all para from url and convert to Long
	 */
	public Long getParaToLong() {
		return toLong(getPara(), null);
	}
	
	/**
	 * Get model from http request.
	 */
	public <T> T getModel(Class<T> modelClass) {
		return (T)Injector.injectModel(modelClass, request, false);
	}
	
	public <T> T getModel(Class<T> modelClass, boolean skipConvertError) {
		return (T)Injector.injectModel(modelClass, request, skipConvertError);
	}
	
	/**
	 * Get model from http request.
	 */
	public <T> T getModel(Class<T> modelClass, String modelName) {
		return (T)Injector.injectModel(modelClass, modelName, request, false);
	}
	
	public <T> T getModel(Class<T> modelClass, String modelName, boolean skipConvertError) {
		return (T)Injector.injectModel(modelClass, modelName, request, skipConvertError);
	}
	
	public <T> T getBean(Class<T> beanClass) {
		return (T)Injector.injectBean(beanClass, request, false);
	}
	
	public <T> T getBean(Class<T> beanClass, boolean skipConvertError) {
		return (T)Injector.injectBean(beanClass, request, skipConvertError);
	}
	
	public <T> T getBean(Class<T> beanClass, String beanName) {
		return (T)Injector.injectBean(beanClass, beanName, request, false);
	}
	
	public <T> T getBean(Class<T> beanClass, String beanName, boolean skipConvertError) {
		return (T)Injector.injectBean(beanClass, beanName, request, skipConvertError);
	}
	
	/**
	 * 获取被 Kv 封装后的参数，便于使用 Kv 中的一些工具方法
	 * 
	 * 由于 Kv 继承自 HashMap，也便于需要使用 HashMap 的场景，
	 * 例如：
	 * Record record = new Record().setColumns(getKv());
	 */
	public Kv getKv() {
		Kv kv = new Kv();
		Map<String, String[]> paraMap = request.getParameterMap();
		for (Entry<String, String[]> entry : paraMap.entrySet()) {
			String[] values = entry.getValue();
			String value = (values != null && values.length > 0) ? values[0] : null;
			kv.put(entry.getKey(), "".equals(value) ? null : value);
		}
		return kv;
	}
	
	// TODO public <T> List<T> getModels(Class<T> modelClass, String modelName) {}
	
	// --------
	
	/**
	 * Get upload file from multipart request.
	 */
	public List<UploadFile> getFiles(String uploadPath, Integer maxPostSize, String encoding) {
		if (request instanceof MultipartRequest == false)
			request = new MultipartRequest(request, uploadPath, maxPostSize, encoding);
		return ((MultipartRequest)request).getFiles();
	}
	
	public UploadFile getFile(String parameterName, String uploadPath, Integer maxPostSize, String encoding) {
		getFiles(uploadPath, maxPostSize, encoding);
		return getFile(parameterName);
	}
	
	public List<UploadFile> getFiles(String uploadPath, int maxPostSize) {
		if (request instanceof MultipartRequest == false)
			request = new MultipartRequest(request, uploadPath, maxPostSize);
		return ((MultipartRequest)request).getFiles();
	}
	
	public UploadFile getFile(String parameterName, String uploadPath, int maxPostSize) {
		getFiles(uploadPath, maxPostSize);
		return getFile(parameterName);
	}
	
	public List<UploadFile> getFiles(String uploadPath) {
		if (request instanceof MultipartRequest == false)
			request = new MultipartRequest(request, uploadPath);
		return ((MultipartRequest)request).getFiles();
	}
	
	public UploadFile getFile(String parameterName, String uploadPath) {
		getFiles(uploadPath);
		return getFile(parameterName);
	}
	
	public List<UploadFile> getFiles() {
		if (request instanceof MultipartRequest == false)
			request = new MultipartRequest(request);
		return ((MultipartRequest)request).getFiles();
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
				try {request.setAttribute(name, TypeConverter.me().convert(type, values[0]));} catch (ParseException e) {com.jfinal.kit.LogKit.logNothing(e);}
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
	
	public Controller keepModel(Class<? extends com.jfinal.plugin.activerecord.Model> modelClass, String modelName) {
		if (StrKit.notBlank(modelName)) {
			Object model = Injector.injectModel(modelClass, modelName, request, true);
			request.setAttribute(modelName, model);
		} else {
			keepPara();
		}
		return this;
	}
	
	public Controller keepModel(Class<? extends com.jfinal.plugin.activerecord.Model> modelClass) {
		String modelName = StrKit.firstCharToLowerCase(modelClass.getSimpleName());
		keepModel(modelClass, modelName);
		return this;
	}
	
	public Controller keepBean(Class<?> beanClass, String beanName) {
		if (StrKit.notBlank(beanName)) {
			Object bean = Injector.injectBean(beanClass, beanName, request, true);
			request.setAttribute(beanName, bean);
		} else {
			keepPara();
		}
		return this;
	}
	
	public Controller keepBean(Class<?> beanClass) {
		String beanName = StrKit.firstCharToLowerCase(beanClass.getSimpleName());
		keepBean(beanClass, beanName);
		return this;
	}
	
	/**
	 * Create a token.
	 * @param tokenName the token name used in view
	 * @param secondsOfTimeOut the seconds of time out, secondsOfTimeOut >= Const.MIN_SECONDS_OF_TOKEN_TIME_OUT
	 */
	public String createToken(String tokenName, int secondsOfTimeOut) {
		return com.jfinal.token.TokenManager.createToken(this, tokenName, secondsOfTimeOut);
	}
	
	/**
	 * Create a token with default token name and with default seconds of time out.
	 */
	public String createToken() {
		return createToken(Const.DEFAULT_TOKEN_NAME, Const.DEFAULT_SECONDS_OF_TOKEN_TIME_OUT);
	}
	
	/**
	 * Create a token with default seconds of time out.
	 * @param tokenName the token name used in view
	 */
	public String createToken(String tokenName) {
		return createToken(tokenName, Const.DEFAULT_SECONDS_OF_TOKEN_TIME_OUT);
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
		return StrKit.isBlank(request.getParameter(paraName));
	}
	
	/**
	 * Return true if the urlPara value is blank otherwise return false
	 */
	public boolean isParaBlank(int index) {
		return StrKit.isBlank(getPara(index));
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
		render = renderManager.getRenderFactory().getRender(view);
	}
	
	/**
	 * Render template to String content, it is useful for:
	 * 1: Generate HTML fragment for AJAX request
	 * 2: Generate email, short message and so on
	 */
	public String renderToString(String template, Map data) {
		if (template.charAt(0) != '/') {
			template = action.getViewPath() + template;
		}
		return renderManager.getEngine().getTemplate(template).renderToString(data);
	}
	
	/**
	 * Render with JFinal template
	 */
	public void renderTemplate(String template) {
		render = renderManager.getRenderFactory().getTemplateRender(template);
	}
	
	/**
	 * Render with jsp view
	 */
	public void renderJsp(String view) {
		render = renderManager.getRenderFactory().getJspRender(view);
	}
	
	/**
	 * Render with freemarker view
	 */
	public void renderFreeMarker(String view) {
		render = renderManager.getRenderFactory().getFreeMarkerRender(view);
	}
	
	/**
	 * Render with velocity view
	 */
	public void renderVelocity(String view) {
		render = renderManager.getRenderFactory().getVelocityRender(view);
	}
	
	/**
	 * Render with json
	 * <p>
	 * Example:<br>
	 * renderJson("message", "Save successful");<br>
	 * renderJson("users", users);<br>
	 */
	public void renderJson(String key, Object value) {
		render = renderManager.getRenderFactory().getJsonRender(key, value);
	}
	
	/**
	 * Render with json
	 */
	public void renderJson() {
		render = renderManager.getRenderFactory().getJsonRender();
	}
	
	/**
	 * Render with attributes set by setAttr(...) before.
	 * <p>
	 * Example: renderJson(new String[]{"blogList", "user"});
	 */
	public void renderJson(String[] attrs) {
		render = renderManager.getRenderFactory().getJsonRender(attrs);
	}
	
	/**
	 * Render with json text.
	 * <p>
	 * Example: renderJson("{\"message\":\"Please input password!\"}");
	 */
	public void renderJson(String jsonText) {
		render = renderManager.getRenderFactory().getJsonRender(jsonText);
	}
	
	/**
	 * Render json with object.
	 * <p>
	 * Example: renderJson(new User().set("name", "JFinal").set("age", 18));
	 */
	public void renderJson(Object object) {
		render = object instanceof JsonRender ? (JsonRender)object : renderManager.getRenderFactory().getJsonRender(object);
	}
	
	/**
	 * Render with text. The contentType is: "text/plain".
	 */
	public void renderText(String text) {
		render = renderManager.getRenderFactory().getTextRender(text);
	}
	
	/**
	 * 响应 text 文本并指定 content type，例如: "text/xml"、"application/javascript"
	 * 
	 * 其中 "text/xml"、"application/javascript"、"application/json"、"text/html"
	 * 从 jfinal 4.6 版本开始可以简写为 "xml"、"js"、"json"、"html"
	 * 
	 * <pre>
	 * 例子：
	 *    renderText("&lt;user id='5888'&gt;James&lt;/user&gt;", "xml");
	 * </pre>
	 */
	public void renderText(String text, String contentType) {
		render = renderManager.getRenderFactory().getTextRender(text, contentType);
	}
	
	/**
	 * Render with text and ContentType.
	 * <p>
	 * Example: renderText("&lt;html&gt;Hello James&lt;/html&gt;", ContentType.HTML);
	 */
	public void renderText(String text, ContentType contentType) {
		render = renderManager.getRenderFactory().getTextRender(text, contentType);
	}
	
	/**
	 * Forward to an action
	 */
	public void forwardAction(String actionUrl) {
		render = new ForwardActionRender(actionUrl);
	}
	
	/**
	 * Render with file
	 */
	public void renderFile(String fileName) {
		render = renderManager.getRenderFactory().getFileRender(fileName);
	}
	
	/**
	 * Render with file, using the new file name to the client
	 */
	public void renderFile(String fileName, String downloadFileName) {
		render = renderManager.getRenderFactory().getFileRender(fileName, downloadFileName);
	}
	
	/**
	 * Render with file
	 */
	public void renderFile(File file) {
		render = renderManager.getRenderFactory().getFileRender(file);
	}
	
	/**
	 * Render with file, using the new file name to the client
	 */
	public void renderFile(File file, String downloadFileName) {
		render = renderManager.getRenderFactory().getFileRender(file, downloadFileName);
	}
	
	/**
	 * Redirect to url
	 */
	public void redirect(String url) {
		render = renderManager.getRenderFactory().getRedirectRender(url);
	}
	
	/**
	 * Redirect to url
	 */
	public void redirect(String url, boolean withQueryString) {
		render = renderManager.getRenderFactory().getRedirectRender(url, withQueryString);
	}
	
	/**
	 * Render with view and status use default type Render configured in JFinalConfig
	 */
	public void render(String view, int status) {
		render = renderManager.getRenderFactory().getRender(view);
		response.setStatus(status);
	}
	
	/**
	 * Render with url and 301 status
	 */
	public void redirect301(String url) {
		render = renderManager.getRenderFactory().getRedirect301Render(url);
	}
	
	/**
	 * Render with url and 301 status
	 */
	public void redirect301(String url, boolean withQueryString) {
		render = renderManager.getRenderFactory().getRedirect301Render(url, withQueryString);
	}
	
	/**
	 * Render with view and errorCode status
	 */
	public void renderError(int errorCode, String view) {
		throw new ActionException(errorCode, renderManager.getRenderFactory().getErrorRender(errorCode, view));
	}
	
	/**
	 * Render with render and errorCode status
	 */
	public void renderError(int errorCode, Render render) {
		throw new ActionException(errorCode, render);
	}
	
	/**
	 * Render with view and errorCode status configured in JFinalConfig
	 */
	public void renderError(int errorCode) {
		throw new ActionException(errorCode, renderManager.getRenderFactory().getErrorRender(errorCode));
	}
	
	/**
	 * Render nothing, no response to browser
	 */
	public void renderNull() {
		render = renderManager.getRenderFactory().getNullRender();
	}
	
	/**
	 * Render with javascript text. The contentType is: "text/javascript".
	 */
	public void renderJavascript(String javascriptText) {
		render = renderManager.getRenderFactory().getJavascriptRender(javascriptText);
	}
	
	/**
	 * Render with html text. The contentType is: "text/html".
	 */
	public void renderHtml(String htmlText) {
		render = renderManager.getRenderFactory().getHtmlRender(htmlText);
	}
	
	/**
	 * Render with xml view using enjoy template.
	 */
	public void renderXml(String view) {
		render = renderManager.getRenderFactory().getXmlRender(view);
	}
	
	public void renderCaptcha() {
		render = renderManager.getRenderFactory().getCaptchaRender();
	}
	
	/**
	 * 渲染二维码
	 * @param content 二维码中所包含的数据内容
	 * @param width 二维码宽度，单位为像素
	 * @param height 二维码高度，单位为像素
	 */
	public void renderQrCode(String content, int width, int height) {
		render = renderManager.getRenderFactory().getQrCodeRender(content, width, height);
	}
	
	/**
	 * 渲染二维码，并指定纠错级别
	 * @param content 二维码中所包含的数据内容
	 * @param width 二维码宽度，单位为像素
	 * @param height 二维码高度，单位为像素
	 * @param errorCorrectionLevel 纠错级别，可设置的值从高到低分别为：'H'、'Q'、'M'、'L'，具体的纠错能力如下：
	 *  H = ~30% 
	 *  Q = ~25%
	 *  M = ~15%
	 *  L = ~7%
	 */
	public void renderQrCode(String content, int width, int height, char errorCorrectionLevel) {
		render = renderManager.getRenderFactory().getQrCodeRender(content, width, height, errorCorrectionLevel);
	}
	
	public boolean validateCaptcha(String paraName) {
		return com.jfinal.captcha.CaptchaRender.validate(this, getPara(paraName));
	}
	
	public void checkUrlPara(int minLength, int maxLength) {
		getPara(0);
		if (urlParaArray.length < minLength || urlParaArray.length > maxLength) {
			renderError(404);
		}
	}
	
	public void checkUrlPara(int length) {
		checkUrlPara(length, length);
	}
	
	// ---------
	
	/*
	 * 获取 Aop 代理对象，便于在 action 方法内部调用，可以取代 @Inject 注入
	 * 
	 * <pre>
	 * 如果控制器中使用了多个 @Inject 注入了多个业务对象，但在该控制器中的有些
	 * action 中，并没有用到或者用到少量的 @Inject 注入的业务对象，就会造成浪费，
	 * 因为控制器中的所有 @Inject 属性不管在 action 中有没有被使用，都会被注入
	 * 
	 * 
	 * 在上述场景下，可以使用 getAopProxy(...) 来取代 @Inject 注入，例如：
	 * public void action() {
	 *    Service service c = getAopProxy(Service.class);
	 *    service.justDoIt();
	 * }
	 * </pre>
	
	public <T> T getAopProxy(Class<T> targetClass) {
		return com.jfinal.aop.Aop.get(targetClass);
	} */
	
	// --------------------
	
	/**
	 * 为了进一步省代码，创建与 setAttr(...) 功能一模一样的缩短版本 set(...)
	 */
	public Controller set(String attributeName, Object attributeValue) {
		request.setAttribute(attributeName, attributeValue);
		return this;
	}
	
	// --- 以下是为了省代码，为 getPara 系列方法创建的缩短版本
	
	public String get(String name) {
		return getPara(name);
	}
	
	public String get(String name, String defaultValue) {
		return getPara(name, defaultValue);
	}
	
	public Integer getInt(String name) {
		return getParaToInt(name);
	}
	
	public Integer getInt(String name, Integer defaultValue) {
		return getParaToInt(name, defaultValue);
	}
	
	public Long getLong(String name) {
		return getParaToLong(name);
	}
	
	public Long getLong(String name, Long defaultValue) {
		return getParaToLong(name, defaultValue);
	}
	
	public Boolean getBoolean(String name) {
		return getParaToBoolean(name);
	}
	
	public Boolean getBoolean(String name, Boolean defaultValue) {
		return getParaToBoolean(name, defaultValue);
	}
	
	public Date getDate(String name) {
		return getParaToDate(name);
	}
	
	public Date getDate(String name, Date defaultValue) {
		return getParaToDate(name, defaultValue);
	}
	
	// --- 以下是 getPara 系列中获取 urlPara 的缩短版本
	
	/* 为了让继承类可以使用名为 get 的 action 注掉此方法，可使用 get(-1) 来实现本方法的功能
	public String get() {
		return getPara();
	} */
	
	public String get(int index) {
		return getPara(index);
	}
	
	public String get(int index, String defaultValue) {
		return getPara(index, defaultValue);
	}
	
	public Integer getInt() {
		return getParaToInt();
	}
	
	public Integer getInt(int index) {
		return getParaToInt(index);
	}
	
	public Integer getInt(int index, Integer defaultValue) {
		return getParaToInt(index, defaultValue);
	}
	
	public Long getLong() {
		return getParaToLong();
	}
	
	public Long getLong(int index) {
		return getParaToLong(index);
	}
	
	public Long getLong(int index, Long defaultValue) {
		return getParaToLong(index, defaultValue);
	}
	
	public Boolean getBoolean() {
		return getParaToBoolean();
	}
	
	public Boolean getBoolean(int index) {
		return getParaToBoolean(index);
	}
	
	public Boolean getBoolean(int index, Boolean defaultValue) {
		return getParaToBoolean(index, defaultValue);
	}
}


