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

    @Override
    public void intercept(Invocation inv) {
        Db.transaction(tx -> {
            inv.invoke();

            // 此处返回值实现 TransactionRollbackDecision 接口可决定是否回滚事务
            return inv.getReturnValue();
        });
    }
}

