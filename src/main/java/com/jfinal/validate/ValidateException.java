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

package com.jfinal.validate;

/**
 * ValidateException support short circuit implementation.
 */
public class ValidateException extends RuntimeException {
	
	private static final long serialVersionUID = -6240972331557944766L;
	
	/**
	 * 异常构造函数会调用 fillInStackTrace() 构建整个调用栈，消耗较大
	 * 而 ValidateException 无需使用调用栈信息，覆盖此方法用于提升性能
	 */
	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}




