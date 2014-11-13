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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.jfinal.plugin.activerecord.CPI;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.ModelRecordElResolver;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * JspRender.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JspRender extends Render {
	
	private static boolean isSupportActiveRecord = false;
	
	static {
		try {
			com.jfinal.plugin.activerecord.ModelRecordElResolver.init();
		}
		catch (Exception e) {
			// System.out.println("Jsp or JSTL can not be supported!");
		}
	}
	
	@Deprecated
	public static void setSupportActiveRecord(boolean supportActiveRecord) {
		JspRender.isSupportActiveRecord = supportActiveRecord;
		ModelRecordElResolver.setWorking(JspRender.isSupportActiveRecord ? false : true);
	}
	
	public JspRender(String view) {
		this.view = view;
	}
	
	public void render() {
		// 在 jsp 页面使用如下指令则无需再指字符集, 否则是重复指定了,与页面指定的不一致时还会出乱码
		// <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
		// response.setContentType(contentType);
		// response.setCharacterEncoding(encoding);
		
		try {
			if (isSupportActiveRecord)
				supportActiveRecord(request);
			request.getRequestDispatcher(view).forward(request, response);
		} catch (Exception e) {
			throw new RenderException(e);
		}
	}
	
	private static int DEPTH = 8;
	
	private void supportActiveRecord(HttpServletRequest request) {
		for (Enumeration<String> attrs = request.getAttributeNames(); attrs.hasMoreElements();) {
			String key = attrs.nextElement();
			Object value = request.getAttribute(key);
			request.setAttribute(key, handleObject(value, DEPTH));
		}
	}
	
	private Object handleObject(Object value, int depth) {
		if(value == null || (depth--) <= 0)
			return value;
		
		if (value instanceof List)
			return handleList((List)value, depth);
		else if (value instanceof Model)
			return handleMap(CPI.getAttrs((Model)value), depth);
		else if (value instanceof Record)
			return handleMap(((Record)value).getColumns(), depth);
		else if(value instanceof Map)
			return handleMap((Map)value, depth);
		else if (value instanceof Page)
			return handlePage((Page)value, depth);
		else if (value instanceof Object[])
			return handleArray((Object[])value, depth);
		else
			return value;
	}
	
	private Map handleMap(Map map, int depth) {
		if (map == null || map.size() == 0)
			return map;
		
		Map<Object, Object> result = map;
		for (Map.Entry<Object, Object> e : result.entrySet()) {
			Object key = e.getKey();
			Object value = e.getValue();
			value = handleObject(value, depth);
			result.put(key, value);
		}
		return result;
	}
	
	private List handleList(List list, int depth) {
		if (list == null || list.size() == 0)
			return list;
		
		List result = new ArrayList(list.size());
		for (Object value : list)
			result.add(handleObject(value, depth));
		return result;
	}
	
	private Object handlePage(Page page, int depth) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("list", handleList(page.getList(), depth));
		result.put("pageNumber", page.getPageNumber());
		result.put("pageSize", page.getPageSize());
		result.put("totalPage", page.getTotalPage());
		result.put("totalRow", page.getTotalRow());
		return result;
	}
	
	private List handleArray(Object[] array, int depth) {
		if (array == null || array.length == 0)
			return new ArrayList(0);
		
		List result = new ArrayList(array.length);
		for (int i=0; i<array.length; i++)
			result.add(handleObject(array[i], depth));
		return result;
	}
}

/*
	private void handleGetterMethod(Map<String, Object> result, Method[] methods) {
		for (Method method : methods) {
			String methodName = method.getName();
			if (methodName.startsWith("get") && method.getParameterTypes().length == 0) {
				throw new RuntimeException("Not finished!");
			}
		}
	}
*/



