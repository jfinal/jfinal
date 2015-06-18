/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.aop;

import java.util.ArrayList;
import java.util.List;

/**
 * InterceptorStack.
 */
public abstract class InterceptorStack implements Interceptor {
	
	private Interceptor[] inters;
	private List<Interceptor> interList;
	
	public InterceptorStack() {
 		config();
 		
		if (interList == null)
			throw new RuntimeException("You must invoke addInterceptors(...) to config your InterceptorStack");
		
		inters = interList.toArray(new Interceptor[interList.size()]);
		interList.clear();
		interList = null;
	}
	
	protected final InterceptorStack addInterceptors(Interceptor... interceptors) {
		if (interceptors == null || interceptors.length == 0)
			throw new IllegalArgumentException("Interceptors can not be null");
		
		if (interList == null)
			interList = new ArrayList<Interceptor>();
		
		for (Interceptor ref : interceptors)
			interList.add(ref);
		
		return this;
	}
	
	public final void intercept(Invocation inv) {
		new InvocationWrapper(inv, inters).invoke();
	}
	
	public abstract void config();
}



