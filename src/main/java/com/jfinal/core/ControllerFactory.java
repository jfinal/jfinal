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
	
	public Controller getController(Class<? extends Controller> controllerClass) throws ReflectiveOperationException {
		return controllerClass.newInstance();
	}
	
	/**
	 * 回收利用 Controller，参考 FastControllerFactory，大致步骤如下：
	 * 
	 * 1：在控制器中覆盖 Controller 的 _clear_() 方法，先清除自身状态，再调用
	 *    super._clear_() 清除父类状态
	 * 
	 * 2：继承 ControllerFactory 并覆盖其中的 recycle() 方法，调用 controller._clear_()
	 * 
	 * 3：配置实现类：me.setControllerFactory(...)
	 */
	public void recycle(Controller controller) {
		
	}
}







