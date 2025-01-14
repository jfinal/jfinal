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

/**
 * TransactionRollbackDecision 在事务方法 transaction(...) 内的返回值类型
 * 实现该接口用于决定在什么情况下回滚事务
 *
 * 该接口支持新版本事务方法 transaction(...)，独立于原有事务方法 tx(...)
 */
public interface TransactionRollbackDecision {

    /**
     * 决定事务是否应该回滚。
     *
     * @return 如果应该回滚，返回 true；否则返回 false。
     */
    boolean shouldRollback();
}

