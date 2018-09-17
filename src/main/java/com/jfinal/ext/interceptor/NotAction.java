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

package com.jfinal.ext.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * NotAction
 * 
 * 自 jfinal 3.5 开始，不建议使用 NotAction 拦截器，而是使用
 * com.jfinal.core 包下面的 @NotAction 注解来取代，具体
 * 用法是:
 *    @Before(NotAction.class) 改成 @NotAction
 * 
 * 
 * 注意: 这两个文件名都是 NotAction，但后者在 com.jfinal.core 包下面
 * 
 */
@Deprecated
public class NotAction implements Interceptor {
	public void intercept(Invocation inv) {
		inv.getController().renderError(404);
	}
}