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
	JAVASCRIPT("application/javascript");
	
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
}

