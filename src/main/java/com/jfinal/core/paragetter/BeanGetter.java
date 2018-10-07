/**
 * Copyright (c) 2011-2017, 玛雅牛 (myaniu AT gmail.com).
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

public class BeanGetter<T> extends ParaGetter<T> {
	private final Class<T> modelClass;
	public BeanGetter(Class<T> modelClass, String parameterName) {
		super(parameterName, null);
		this.modelClass = modelClass;
	}
	@Override
	public T get(Action action, Controller c) {
		return c.getBean(modelClass, this.getParameterName(),true);
	}
	
	@Override
	protected T to(String v) {
		return null;
	}
}
