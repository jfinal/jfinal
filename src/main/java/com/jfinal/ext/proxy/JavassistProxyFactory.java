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

package com.jfinal.ext.proxy;

import java.util.HashMap;
import com.jfinal.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * JavassistProxyFactory 用于扩展 javassist 的代理模式，默认不使用
 * 
 * <pre>
 * 配置方法：
 * public void configConstant(Constants me) {
 *     ProxyManager.me().setProxyFactory(new JavassistProxyFactory());
 * }
 * </pre>
 */
public class JavassistProxyFactory extends ProxyFactory {
    
    protected HashMap<Class<?>, Class<?>> cache = new HashMap<>(1024, 0.25F);
    protected JavassistCallback callback = new JavassistCallback();
    
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> target) {
        // 被 javassist 代理过的类名包含 "_$$_"。不存在代理过两层的情况，仅需调用一次 getSuperclass() 即可
        if (target.getName().indexOf("_$$_") > -1) {
            target = (Class<T>) target.getSuperclass();
        }
        
        try {
            Class<T> clazz = (Class<T>) cache.get(target);
            if (clazz == null) {
                clazz = getProxyClass(target);
            }
            
            T ret = clazz.newInstance();
            ((ProxyObject) ret).setHandler(callback);
            return ret;
            
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T> Class<T> getProxyClass(Class<T> target) throws ReflectiveOperationException {
        synchronized(this) {
            return (Class<T>) cache.computeIfAbsent(target, key -> {
                javassist.util.proxy.ProxyFactory factory = new javassist.util.proxy.ProxyFactory();
                factory.setSuperclass(key);
                return factory.createClass();
            });
        }
    }
}










