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

package com.jfinal.config;

import java.util.ArrayList;
import java.util.List;
import com.jfinal.handler.Handler;

/**
 * Handlers.
 */
final public class Handlers {
	
	private final List<Handler> handlerList = new ArrayList<Handler>();
	
	public Handlers add(Handler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("handler can not be null");
		}
		handlerList.add(handler);
		return this;
	}
	
	public List<Handler> getHandlerList() {
		return handlerList;
	}
}
