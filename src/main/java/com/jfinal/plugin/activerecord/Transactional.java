/**
 * Copyright (c) 2011-2025, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.plugin.activerecord;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * Transactional 拦截器支持新版本事务方法 Db.transaction(...)
 * 注意：支持被拦截的目标方法返回值决定是否回滚事务
 */
public class Transactional implements Interceptor {

    static final ThreadLocal<Transaction<?>> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取当前 Transactional 拦截器开启的事务。
     *
     * @return 当前事务，未处于 Transactional 拦截器中时返回 null
     */
    public static Transaction<?> getTransaction() {
        return THREAD_LOCAL.get();
    }

    @Override
    public void intercept(Invocation inv) {
        Object returnValue = Db.transaction(tx -> {
            Transaction<?> previous = THREAD_LOCAL.get();
            try {
                THREAD_LOCAL.set(tx);
                inv.invoke();
                return inv.getReturnValue();    // 若返回 TransactionRollbackDecision 实例，则会参与事务回滚
            } finally {
                if (previous == null) {
                    THREAD_LOCAL.remove();
                } else {
                    THREAD_LOCAL.set(previous);
                }
            }
        });

        // 用于注入 onException 回调返回值，其它流程已在 Invocation.invoke() 中赋值
        inv.setReturnValue(returnValue);
    }
}

