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

package com.jfinal.ext.render;

import com.jfinal.render.RenderException;
import com.jfinal.render.TemplateRender;
import com.jfinal.template.Template;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成静态 html
 */
public class StaticHtmlRender extends TemplateRender {
	protected File file;

	/**
	 * @param view 模板路径
	 * @param file 生成的静态html文件路径
	 */
	public StaticHtmlRender(String view, File file) {
		super(view);
		this.file = file;
	}

	@Override
	public void render() {
		response.setContentType(getContentType());

		Map<Object, Object> data = new HashMap<>();
		for (Enumeration<String> attrs = request.getAttributeNames(); attrs.hasMoreElements();) {
			String attrName = attrs.nextElement();
			data.put(attrName, request.getAttribute(attrName));
		}

		OutputStream os = null;
		try {
			os = response.getOutputStream();
			Template template = engine.getTemplate(view);
			template.render(data, os);
			os.flush();

			File parentFile = file.getParentFile();
			if (! parentFile.exists()) {
				parentFile.mkdirs();
			}
			template.render(data, file);

		} catch (RuntimeException e) {	// 捕获 ByteWriter.close() 抛出的 RuntimeException
			Throwable cause = e.getCause();
			if (cause instanceof IOException) {	// ClientAbortException、EofException 直接或间接继承自 IOException
				close(os);
				String name = cause.getClass().getSimpleName();
				if ("ClientAbortException".equals(name) || "EofException".equals(name)) {
					return ;
				}
			}

			throw e;
		} catch (Exception e) {
			if (e instanceof IOException) {
				close(os);
			}
			throw new RenderException(e);
		}
	}
}





