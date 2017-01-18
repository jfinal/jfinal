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

package com.jfinal.template.stat;

/**
 * Location
 * 生成异常发生的位置消息
 */
public class Location {
	
	private String templateFile;
	private int row;
	private String msg;
	
	public Location(String templateFile, int row) {
		this.templateFile = templateFile;
		this.row = row;
		this.msg = null;
	}
	
	public String toString() {
		if (msg == null) {
			StringBuilder buf = new StringBuilder();
			if (templateFile != null) {
				buf.append("\nTemplate: \"").append(templateFile).append("\". Line: ").append(row);
			} else {
				buf.append("\nString template line: ").append(row);
			}
			msg = buf.toString();
		}
		return msg;
	}
	
	public String getTemplateFile() {
		return templateFile;
	}
	
	public int getRow() {
		return row;
	}
}






