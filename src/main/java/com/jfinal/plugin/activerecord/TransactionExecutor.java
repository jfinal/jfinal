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

import com.jfinal.kit.LogKit;
import java.sql.Connection;
import java.util.function.BiConsumer;

/**
 * TransactionExecutor 支持新版本事务方法 transaction(...)，独立于原有事务方法 tx(...)
 */
public class TransactionExecutor {

    @SuppressWarnings("unchecked")
    public <R> R execute(Config config, int transactionLevel, TransactionAtom<R> atom) {
        Connection conn = config.getThreadLocalConnection();
        Transaction<R> tx = config.getThreadLocalTransaction();
        BiConsumer<Transaction<?>, Object> onBeforeCommit = config.getOnBeforeTransactionCommit();

        if (conn != null) {	// Nested transaction support
            return handleNestedTransaction(conn, transactionLevel, tx, atom, onBeforeCommit);
        }

        Boolean autoCommit = null;
        try {
            conn = config.getConnection();
            autoCommit = conn.getAutoCommit();
            config.setThreadLocalConnection(conn);
            conn.setTransactionIsolation(transactionLevel);
            conn.setAutoCommit(false);

            tx = new Transaction<>();
            config.setThreadLocalTransaction(tx);

            R ret = atom.run(tx);
            if (ret instanceof TransactionRollbackDecision && ((TransactionRollbackDecision)ret).shouldRollback()) {
                tx.rollback();
            }
            // 内层、外层调用 onBeforeCommit 处理各自的 ret 返回值
            if (!tx.shouldRollback() && onBeforeCommit != null) {
                onBeforeCommit.accept(tx, ret);
            }

            if (tx.shouldRollback()) {
                conn.rollback();
            } else {
                conn.commit();
                tx.executeOnAfterCommit();                  // 用于新版本事务方法 transaction(...)
                // config.executeCallbackAfterTxCommit();   // 仅用于老版本事务方法 tx(...)
            }

            return ret;

        } catch (Exception e) {
            if (conn != null) try {conn.rollback();} catch (Exception e1) {LogKit.error(e1.getMessage(), e1);}

            // 异常回调，局部回调优先级高于全局回调
            if (tx.getOnException() != null) {
                return tx.getOnException().apply(e);
            } else if (config.getOnTransactionException() != null) {
                return (R) config.getOnTransactionException().apply(e);
            }

            throw e instanceof RuntimeException ? (RuntimeException)e : new ActiveRecordException(e);

        } finally {
            try {
                if (conn != null) {
                    if (autoCommit != null) {
                        conn.setAutoCommit(autoCommit);
                    }
                    conn.close();
                }
            } catch (Throwable t) {
                LogKit.error(t.getMessage(), t);	// can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
            } finally {
                config.removeThreadLocalConnection();	// prevent memory leak
                config.removeTransaction();
                // config.removeCallbackAfterTxCommit();    // 仅用于老版本事务方法 tx(...)
            }
        }
    }

    private <R> R handleNestedTransaction(Connection conn, int transactionLevel, Transaction<R> tx, TransactionAtom<R> atom, BiConsumer<Transaction<?>, Object> onBeforeCommit) {
        try {
            if (conn.getTransactionIsolation() < transactionLevel) {
                conn.setTransactionIsolation(transactionLevel);
            }

            R ret = atom.run(tx);
            if (ret instanceof TransactionRollbackDecision && ((TransactionRollbackDecision)ret).shouldRollback()) {
                tx.rollback();
            }
            // 内层、外层调用 onBeforeCommit 处理各自的 ret 返回值
            if (!tx.shouldRollback() && onBeforeCommit != null) {
                onBeforeCommit.accept(tx, ret);
            }
            return ret;

        } catch (Exception e) {
            tx.rollback();
            throw new ActiveRecordException(e);
        }
    }
}


