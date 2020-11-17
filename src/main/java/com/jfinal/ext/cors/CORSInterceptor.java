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

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.StrKit;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * 
 * 使用方法:
 * 1、在 JFInalConfig 的 在 configInterceptor(Interceptors me) 中添加全局拦截器（也可以不是全局拦截器）：me.add(new CORSInterceptor());
 * 2、在需要支持跨域的 Action 方法中添加 @EnableCORS
 * 
 * PS：
 * 1、如果在 Controller 中添加 @EnableCORS，则该 Controller 下的所有方法都支持跨域
 * 2、通过 @EnableCORS 的参数可以对 Action 进行更加详细的配置，@EnableCORS 每个参数的配置，请参考 https://developer.mozilla.org/en-US/docs/Glossary/CORS
 */
public class CORSInterceptor implements Interceptor {

    private static final String METHOD_OPTIONS = "OPTIONS";

    @Override
    public void intercept(Invocation inv) {

        EnableCORS enableCORS = getAnnotation(inv);
        
        if (enableCORS == null) {
            inv.invoke();
            return;
        }

        doConfigCORS(inv, enableCORS);

        String method = inv.getController().getRequest().getMethod();
        if (METHOD_OPTIONS.equals(method)) {
            inv.getController().renderText("");
        } else {
            inv.invoke();
        }
    }
    
    private EnableCORS getAnnotation(Invocation inv) {
    	 EnableCORS enableCORS = inv.getController().getClass().getAnnotation(EnableCORS.class);
    	 return enableCORS != null ? enableCORS : inv.getMethod().getAnnotation(EnableCORS.class);
    }

    private void doConfigCORS(Invocation inv, EnableCORS enableCORS) {

        HttpServletResponse response = inv.getController().getResponse();

        String allowOrigin = enableCORS.allowOrigin();
        String allowCredentials = enableCORS.allowCredentials();
        String allowHeaders = enableCORS.allowHeaders();
        String allowMethods = enableCORS.allowMethods();
        String exposeHeaders = enableCORS.exposeHeaders();
        String requestHeaders = enableCORS.requestHeaders();
        String requestMethod = enableCORS.requestMethod();
        String origin = enableCORS.origin();
        String maxAge = enableCORS.maxAge();

        response.setHeader("Access-Control-Allow-Origin", allowOrigin);
        response.setHeader("Access-Control-Allow-Methods", allowMethods);
        response.setHeader("Access-Control-Allow-Headers", allowHeaders);
        response.setHeader("Access-Control-Max-Age", maxAge);
        response.setHeader("Access-Control-Allow-Credentials", allowCredentials);

        if (StrKit.notBlank(exposeHeaders)) {
            response.setHeader("Access-Control-Expose-Headers", exposeHeaders);
        }

        if (StrKit.notBlank(requestHeaders)) {
            response.setHeader("Access-Control-Request-Headers", requestHeaders);
        }

        if (StrKit.notBlank(requestMethod)) {
            response.setHeader("Access-Control-Request-Method", requestMethod);
        }

        if (StrKit.notBlank(origin)) {
            response.setHeader("Origin", origin);
        }

    }
}
