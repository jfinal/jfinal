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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import com.jfinal.util.JsonBuilder;

/**
 * JsonRender.
 */
@SuppressWarnings("serial")
class JsonRender extends Render {
	
	/**
	 * http://zh.wikipedia.org/zh/MIME
	 * 在wiki中查到: 尚未被接受为正式数据类型的subtype，可以使用x-开始的独立名称（例如application/x-gzip）
	 * 所以以下可能要改成 application/x-json
	 * 
	 * 通过使用firefox测试,struts2-json-plugin返回的是 application/json, 所以暂不改为 application/x-json
	 * 1: 官方的 MIME type为application/json, 见 http://en.wikipedia.org/wiki/MIME_type
	 * 2: ie 不支持 application/json, 在 ajax 上传文件完成后返回 json时 ie 提示下载文件
	 */
	private static final String contentType = "application/json;charset=" + getEncoding();
	
	private String key;
	private Object value;
	private String[] attrs;
	
	public JsonRender() {
		
	}
	
	public JsonRender(String key, Object value) {
		this.key = key;
		this.value = value;
	}
	
	public JsonRender(String[] attrs) {
		this.attrs = attrs;
	}
	
	public void render() {
		String jsonText = buildJsonText();
		
		PrintWriter writer = null;
		try {
			response.setHeader("Pragma", "no-cache");	// HTTP/1.0 caches might not implement Cache-Control and might only implement Pragma: no-cache
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			
			response.setContentType(contentType);
			writer = response.getWriter();
	        writer.write(jsonText);
	        writer.flush();
		} catch (IOException e) {
			throw new RenderException(e);
		}
		finally {
			writer.close();
		}
	}
	
	private static final int depth = 8;
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private String buildJsonText() {
		Map map = new HashMap();
		if (key != null) {
			map.put(key, value);
		}
		else if (attrs != null) {
			for (String key : attrs)
				map.put(key, request.getAttribute(key));
		}
		else {
			Enumeration<String> attrs = request.getAttributeNames();
			while (attrs.hasMoreElements()) {
				String key = attrs.nextElement();
				Object value = request.getAttribute(key);
				map.put(key, value);
			}
		}
		
		return JsonBuilder.mapToJson(map, depth);
	}
}



