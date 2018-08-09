/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
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

/**
 * ControllerFactory
 */
public class ControllerFactory {
	
	public Controller getController(Class<? extends Controller> controllerClass) throws InstantiationException, IllegalAccessException {
		return controllerClass.newInstance();
	}
	
	/**
	 * 判断是否回收 Controller 对象，如果回收的话就需要 return true，
	 * 那么 ActionHandler 就会调用 controller._clear_() 用于
	 * 清除属性，可以回收使用 Controller 对象
	 * 
	 * 如果用户自已的 controller 或者 BaseController 中声明了属性，
	 * 并且希望回收使用 controller 对象，那么就必须覆盖 controller
	 * 的 _clear_() 方法，大致方法如下：
	 * 
	 * protected void _clear_() {
	 *    super._clear_();	// 清除父类属性中的值
	 *    xxx = null;		// 清除自身属性中的值
	 * }
	 * 
	 * 回收使用 Controller 除了要注意上述说明中的 _clear_() 用法以外，
	 * 其实现方式见 FastControllerFactory
	 */
	public boolean recycleController() {
		return false;
	}
}







