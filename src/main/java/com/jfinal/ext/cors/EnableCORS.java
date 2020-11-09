/**
 * Copyright (c) 2011-2021, James Zhan 詹波 (jfinal@126.com).
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
package com.jfinal.ext.cors;

import java.lang.annotation.*;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * 
 * 每个参数意义的详情 : https://developer.mozilla.org/en-US/docs/Glossary/CORS
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface EnableCORS {

    String allowOrigin() default "*";

    String allowCredentials() default "true";

    String allowHeaders() default "Origin,X-Requested-With,Content-Type,Accept,Authorization,Jwt";

    String allowMethods() default "GET,PUT,POST,DELETE,PATCH,OPTIONS";

    String exposeHeaders() default "";

    String requestHeaders() default "";

    String requestMethod() default "";

    String origin() default "";

    String maxAge() default "3600";
}
