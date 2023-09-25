/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Path 注解用于配置 Controller 的 controllerPath 以及 viewPath
 * 搭配 PathScanner 实现路由扫描功能
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Path {
	
	/**
	 * 由于空字符串可以被用于配置 viewPath，所以使用如下字符串表示默认值
	 */
	String NULL_VIEW_PATH = "*";
	
	/**
	 * 配置 Controller 的访问路径 controllerPath，该路径与 Controller 中的方法名组合成 actionKey
	 */
	String value();
	
	/**
	 * 配置 Controller.render(String) 所使用模板文件的路径，省略该配置时默认使用 controllerPath
	 */
	String viewPath() default NULL_VIEW_PATH;
}








