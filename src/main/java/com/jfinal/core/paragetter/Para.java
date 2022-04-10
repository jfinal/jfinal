/**
 * Copyright (c) 2011-2023, 玛雅牛 (myaniu AT gmail.com).
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface Para {
	
	/**
	 * 不能使用空字符串：
	 * 1: 对于 value() 早已用于无 modelName 前缀的场景：action(@Para("")User user)
	 * 2: 对于 defaultValue() 可用于指定默认值为空字符串：action(@Para(defaultValue = "")String email)
	 */
	String NULL_VALUE = "-NULL VALUE-";
	
    /**
     * 对应到 HTTP 参数里的参数名称
     */
    String value() default NULL_VALUE;
    
    /**
     * 默认值
     */
    String defaultValue() default NULL_VALUE;
}
