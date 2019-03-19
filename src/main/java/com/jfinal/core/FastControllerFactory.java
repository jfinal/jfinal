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

import java.util.HashMap;
import java.util.Map;

/**
 * FastControllerFactory 用于回收使用 Controller 对象，提升性能
 * 
 * 由于 Controller 会被回收利用，所以使用之前一定要确保 controller
 * 对象中的属性值没有线程安全问题
 * 
 * 警告：如果用户自己的 Controller 或者 BaseController 之中声明了属性，
 *      并且这些属性不能被多线程共享，则不能直接使用 FastControllerFactory，
 *      否则会有线程安全问题
 *      
 *      jfinal 3.5 版本可以通过覆盖 Controller._clear_() 方法来消除这个限制，
 *      大至代码如下：
 *      protected void _clear_() {
 *          super._clear_();		// 调用父类的清除方法清掉父类中的属性值
 *          this.xxx = null;		// 清除本类中声明的属性的值
 *      }
 *      
 */
public class FastControllerFactory extends ControllerFactory {
	
	private ThreadLocal<Map<Class<? extends Controller>, Controller>> buffers = new ThreadLocal<Map<Class<? extends Controller>, Controller>>() {
		protected Map<Class<? extends Controller>, Controller> initialValue() {
			return new HashMap<Class<? extends Controller>, Controller>();
		}
	};
	
	public Controller getController(Class<? extends Controller> controllerClass) throws ReflectiveOperationException {
		Controller ret = buffers.get().get(controllerClass);
		if (ret == null) {
			ret = controllerClass.newInstance();
			buffers.get().put(controllerClass, ret);
		}
		return ret;
	}
	
	/**
	 * 清除 controller 状态，回收利用
	 */
	public void recycle(Controller controller) {
		if (controller != null) {
			controller._clear_();
		}
	}
}







