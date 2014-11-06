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

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JavascriptRender.
 */
public class JavascriptRender extends Render {
	
	private static final String contentType = "application/javascript; charset=" + getEncoding();
	private String jsText;
	
	public JavascriptRender(String jsText) {
		this.jsText = jsText;
	}
	
	public void render() {
		PrintWriter writer = null;
		try {
			response.setContentType(contentType);
	        writer = response.getWriter();
	        writer.write(jsText);
	        writer.flush();
		} catch (IOException e) {
			throw new RenderException(e);
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}
}





