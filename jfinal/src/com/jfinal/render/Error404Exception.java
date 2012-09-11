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
 * Error404Exception.
 */
@SuppressWarnings("serial")
public class Error404Exception extends RuntimeException {
	
	private Render error404Render;
	
	public Error404Exception(Render error404Render) {
		this.error404Render = error404Render;
	}
	
	public Render getError404Render() {
		return error404Render;
	}
}