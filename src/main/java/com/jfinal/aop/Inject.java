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

package com.jfinal.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject is used to inject dependent object
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Inject {

	/**
	 * 被注入类的类型
 	 */
	Class<?> value() default Void.class;

	/**
	 * 可用于配置远程服务名称，然后扩展 AopFactory 生成代理，以调用方法的方式来访问远程服务
	 * <pre>
	 * 例如：@Inject(remote = "orderService")
	 *      OrderService service;
	 *
	 *      public void index(Integer orderId) {
	 *      	// 调用名为 orderService 的远程服务
	 *          renderJson(service.getById(orderId));
	 *      }
	 * </pre>
	 */
	String remote() default "";
}

