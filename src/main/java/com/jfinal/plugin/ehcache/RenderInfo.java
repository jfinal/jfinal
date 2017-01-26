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

package com.jfinal.plugin.ehcache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.jfinal.render.FreeMarkerRender;
import com.jfinal.render.JsonRender;
import com.jfinal.render.JspRender;
import com.jfinal.render.Render;
import com.jfinal.render.TemplateRender;
import com.jfinal.render.VelocityRender;
import com.jfinal.render.XmlRender;

/**
 * RenderInfo.
 */
public class RenderInfo implements Serializable {
	
	private static final long serialVersionUID = -7299875545092102194L;
	
	protected String view;
	protected Integer renderType;
	protected Map<String, Object> otherPara = null;
	
	public RenderInfo(Render render) {
		if (render == null) {
			throw new IllegalArgumentException("Render can not be null.");
		}
		
		view = render.getView();
		if (render instanceof TemplateRender) {
			renderType = RenderType.TEMPLATE_RENDER;
		} else if (render instanceof FreeMarkerRender) {
			renderType = RenderType.FREE_MARKER_RENDER;
		} else if (render instanceof JspRender) {
			renderType = RenderType.JSP_RENDER;
		} else if (render instanceof VelocityRender) {
			renderType = RenderType.VELOCITY_RENDER;
		} else if (render instanceof XmlRender) {
			renderType = RenderType.XML_RENDER;
		} else if(render instanceof JsonRender) {
			JsonRender jr = (JsonRender)render;
			renderType = RenderType.JSON_RENDER;
			otherPara = new HashMap<String, Object>();
			otherPara.put("jsonText", jr.getJsonText());
			otherPara.put("attrs", jr.getAttrs());
			otherPara.put("forIE", jr.getForIE());
		}
		else
			throw new IllegalArgumentException("CacheInterceptor can not support the render of the type : " + render.getClass().getName());
	}
	
	public Render createRender() {
		switch (renderType) {
		case RenderType.TEMPLATE_RENDER:
			return new TemplateRender(view);
		case RenderType.FREE_MARKER_RENDER:
			return new FreeMarkerRender(view);
		case RenderType.JSP_RENDER:
			return new JspRender(view);
		case RenderType.VELOCITY_RENDER:
			return new VelocityRender(view);
		case RenderType.XML_RENDER:
			return new XmlRender(view);
		case RenderType.JSON_RENDER:
			JsonRender jr;
			if (otherPara.get("jsonText") != null) {
				jr = new JsonRender((String)otherPara.get("jsonText"));
			} else if (otherPara.get("attrs") != null) {
				jr = new JsonRender((String[])otherPara.get("attrs"));
			} else {
				jr = new JsonRender();
			}
			
			if (Boolean.TRUE.equals(otherPara.get("forIE"))) {
				jr.forIE();
			}
			return jr;
		default :
			throw new IllegalArgumentException("CacheInterceptor can not support the renderType of the value : " + renderType);
		}
	}
}
