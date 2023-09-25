/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

/**
 * TextRender.
 */
public class TextRender extends Render {
	
	// 与 encoding 与 contentType 在 render() 方法中分开设置，效果相同
	protected static final String DEFAULT_CONTENT_TYPE = "text/plain";
	
	protected String text;
	protected String contentType;
	
	public TextRender(String text) {
		this.text = text;
		this.contentType = DEFAULT_CONTENT_TYPE;
	}
	
	public TextRender(String text, String contentType) {
		this.text = text;
		
		// 支持简洁写法，例如：xml、js
		ContentType ct = ContentType.parse(contentType);
		this.contentType = (ct != null ? ct.value() : contentType);
	}
	
	public TextRender(String text, ContentType contentType) {
		this.text = text;
		this.contentType = contentType.value();
	}
	
	public void render() {
		PrintWriter writer = null;
		try {
			// response.setHeader("Cache-Control", "no-cache");
			
			
			String ct = getContentType();
			response.setContentType(ct);
			
			// 不包含 "charset" 时才调用该方法，否则该方法会覆盖掉 contentType 中的 "charset" 部分
			if (ct.indexOf("charset") == -1) {
				response.setCharacterEncoding(getEncoding());	// 与 contentType 分开设置
			}
			
			
			writer = response.getWriter();
			writer.write(text);
			writer.flush();
		} catch (Exception e) {
		    if (e instanceof IOException) {
		        close(writer);
		    }
			throw new RenderException(e);
		}
	}
	
	public String getText() {
		return text;
	}
	
	public String getContentType() {
		return contentType;
	}
}


