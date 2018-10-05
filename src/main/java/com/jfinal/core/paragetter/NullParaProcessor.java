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

package com.jfinal.core.paragetter;

import com.jfinal.core.Action;
import com.jfinal.core.Controller;

/**
 * 无参 action 共享同一个 NullParaProcessor 对象，节省空间
 * 
 * 其它所有 ParaProcessor 对象的 get(Action action, Controller c)
 * 内部不必进行 null 值判断，节省时间
 */
public class NullParaProcessor extends ParaProcessor {
	
	private static final Object[] NULL_ARGS = new Object[0];
	
	public static final NullParaProcessor me = new NullParaProcessor(0);
	
	private NullParaProcessor(int paraCount) {
		super(paraCount);
	}
	
	@Override
	public Object[] get(Action action, Controller c) {
		return NULL_ARGS;
	}
}






