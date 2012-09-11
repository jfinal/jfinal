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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.jfinal.plugin.activerecord.CPI;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * JspRender.
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
class JspRender extends Render {
	
	private transient static boolean isSupportActiveRecord = true;
	
	static void setSupportActiveRecord(boolean supportActiveRecord) {
		JspRender.isSupportActiveRecord = supportActiveRecord;
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
	
	private void supportActiveRecord(HttpServletRequest request) {
		for (Enumeration<String> attrs = request.getAttributeNames(); attrs.hasMoreElements();) {
			String key = attrs.nextElement();
			Object value = request.getAttribute(key);
			if (value instanceof Model) {
				request.setAttribute(key, handleModel((Model)value));
			}
			else if (value instanceof Record) {
				request.setAttribute(key, handleRecord((Record)value));
			}
			else if (value instanceof List) {
				request.setAttribute(key, handleList((List)value));
			}
			else if (value instanceof Page) {
				request.setAttribute(key, handlePage((Page)value));
			}
			else if (value instanceof Model[]) {
				request.setAttribute(key, handleModelArray((Model[])value));
			}
			else if (value instanceof Record[]) {
				request.setAttribute(key, handleRecordArray((Record[])value)); 
			}
		}
	}
	
	private List handleList(List list) {
		if (list != null && list.size() > 0) {
			Object o = list.get(0);
			if (o instanceof Model)
				return handleModelList((List<Model>)list);
			else if (o instanceof Record)
				return handleRecordList((List<Record>)list);
		}
		return list;
	}
	
	private Object handlePage(Page page) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("list", handleList(page.getList()));
		result.put("pageNumber", page.getPageNumber());
		result.put("pageSize", page.getPageSize());
		result.put("totalPage", page.getTotalPage());
		result.put("totalRow", page.getTotalRow());
		return result;
	}
	
	private Map<String, Object> handleModel(Model model) {
		// handleGetterMethod(CPI.getAttrs(model), model.getClass().getMethods());
		return CPI.getAttrs(model);
	}
	
	private Map<String, Object> handleRecord(Record record) {
		return record.getColumns();
	}
	
	private List<Map<String, Object>> handleModelList(List<Model> list) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(list.size());
		for (Model model : list)
			result.add(CPI.getAttrs(model));
		return result;
	}
	
	private List<Map<String, Object>> handleRecordList(List<Record> list) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(list.size());
		for (Record record : list)
			result.add(record.getColumns());
		return result;
	}
	
	private List<Map<String, Object>> handleModelArray(Model[] array) {	// should be? : Map<String, Object>[]
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(array.length);
		for (Model model : array)
			result.add(CPI.getAttrs(model));
		return result;
	}
	
	private List<Map<String, Object>> handleRecordArray(Record[] array) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(array.length);
		for (Record record : array)
			result.add(record.getColumns());
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



