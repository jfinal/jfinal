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

import java.util.HashMap;
import java.util.Map;

/**
 * ContentType
 * <br>
 * TOMCAT-HOME/conf/web.xml
 * <br>
 * http://tool.oschina.net/commons
 */
public enum ContentType {

	TEXT("text/plain"),
	HTML("text/html"),
	XML("text/xml"),
	JSON("application/json"),
	JAVASCRIPT("application/javascript"),
	EVENTSTREAM("text/event-stream");

	private final String value;

	private ContentType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public String toString() {
		return value;
	}

	// ---------

	private static final Map<String, ContentType> mapping = initMapping();

	/**
	 * 将简写的文本射到 context type，方便在 Controller.renderText(String, String)
	 * 之中取用，例如：
	 *   renderText(..., "xml")
	 *   比下面的用法要省代码
	 *   renderText(..., "text/xml")
	 */
	private static Map<String, ContentType> initMapping() {
		Map<String, ContentType> ret = new HashMap<>();

		ret.put("text", TEXT);
		ret.put("plain", TEXT);
		ret.put("html", HTML);
		ret.put("xml", XML);
		ret.put("json", JSON);
		ret.put("javascript", JAVASCRIPT);
		ret.put("js", JAVASCRIPT);
		ret.put("eventStream", EVENTSTREAM);

		ret.put("TEXT", TEXT);
		ret.put("PLAIN", TEXT);
		ret.put("HTML", HTML);
		ret.put("XML", XML);
		ret.put("JSON", JSON);
		ret.put("JAVASCRIPT", JAVASCRIPT);
		ret.put("JS", JAVASCRIPT);
		ret.put("EVENTSTREAM", EVENTSTREAM);

		return ret;
	}

	public static ContentType parse(String str) {
		return mapping.get(str);
	}
}

