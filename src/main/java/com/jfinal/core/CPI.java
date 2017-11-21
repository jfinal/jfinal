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

package com.jfinal.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cross Package Invoking pattern for package core.
 * 
 * <pre>
 * 有利于在自定义扩展的 ActionHandler 中调用 Controller.init(...)
 * 与 Controller.clear() 以及其它一切需要调用上面两个方法的场景
 * 
 * 示例：
 * CPI.init(controller, request, response, urlPara);
 * CPI.clear(controller);
 * </pre>
 */
public class CPI {
	
	public static void init(Controller controller, Action action, HttpServletRequest request, HttpServletResponse response, String urlPara) {
		controller.init(action, request, response, urlPara);
	}
	
	public static void clear(Controller controller) {
		controller.clear();
	}
}





