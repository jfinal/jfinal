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

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import com.jfinal.template.Engine;

/**
 * TemplateRender
 */
public class TemplateRender extends Render {
	
	private static Engine engine;
	
	private static final String contentType = "text/html; charset=" + getEncoding();
	
	static void init(Engine engine) {
		if (engine == null) {
			throw new IllegalArgumentException("engine can not be null");
		}
		TemplateRender.engine = engine;
	}
	
	public TemplateRender(String view) {
		this.view = view;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void render() {
		response.setContentType(getContentType());
        
		Map<Object, Object> data = new HashMap<Object, Object>();
		for (Enumeration<String> attrs=request.getAttributeNames(); attrs.hasMoreElements();) {
			String attrName = attrs.nextElement();
			data.put(attrName, request.getAttribute(attrName));
		}
		
		PrintWriter writer = null;
        try {
        	writer = response.getWriter();
        	engine.getTemplate(view).render(data, writer);
		} catch (Exception e) {
			throw new RenderException(e);
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	public String toString() {
		return view;
	}
}








