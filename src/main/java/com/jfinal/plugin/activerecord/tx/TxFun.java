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

package com.jfinal.plugin.activerecord.tx;

import com.jfinal.aop.Invocation;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 支持定制事务行为，否则 Tx 拦截器只会在抛出异常时回滚事务
 *
 * <pre>
 * 例如通过返回值 Ret 对象来决定事务的提交与回滚：
 *   Tx.setTxFun((inv, conn) -> {
 *       inv.invoke();
 *
 *       // 根据业务层返回值 Ret 对象的状态决定提交与回滚
 *       Object retValue = inv.getReturnValue();
 *       if (retValue instanceof Ret) {
 *           Ret ret = (Ret)retValue;
 *           if (ret.isOk()) {
 *               conn.commit();
 *           } else {
 *               conn.rollback();
 *           }
 *           return ;
 *       }
 *
 *       // 返回其它类型值的情况
 *       conn.commit();
 *    });
 * </pre>
 */
@FunctionalInterface
public interface TxFun {
    void call(Invocation inv, Connection conn) throws SQLException;
}



