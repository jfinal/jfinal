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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Clear is used to clear all interceptors or the specified interceptors,
 * it can not clear the interceptor which declare on method.
 * 
 * <pre>
 * Example:
 * 1: clear all interceptors but InterA and InterB will be kept, because InterA and InterB declare on method
 * @Clear
 * @Before({InterA.class, InterB.class})
 * public void method(...)
 * 
 * 2: clear InterA and InterB, InterC and InterD will be kept
 * @Clear({InterA.class, InterB.class})
 * @Before({InterC.class, InterD.class})
 * public void method(...)
 * </pre>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Clear {
	Class<? extends Interceptor>[] value() default {};
}

