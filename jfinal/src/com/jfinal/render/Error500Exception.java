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

/**
 * Error500Exception.
 */
@SuppressWarnings("serial")
public class Error500Exception extends RuntimeException {
	
	private Render error500Render;
	
	public Error500Exception(Render error500Render) {
		this.error500Render = error500Render;
	}
	
	public Render getError500Render() {
		return error500Render;
	}
}