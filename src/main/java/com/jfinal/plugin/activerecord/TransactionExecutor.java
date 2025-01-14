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
import java.sql.SQLException;

/**
 * TransactionExecutor 支持新版本事务方法 transaction(...)，独立于原有事务方法 tx(...)
 */
public class TransactionExecutor {

    public <R> R execute(Config config, int transactionLevel, TransactionAtom<R> atom) {
        Connection conn = config.getThreadLocalConnection();
        Transaction<R> tx = config.getThreadLocalTransaction();

        if (conn != null) {	// Nested transaction support
            return handleNestedTransaction(conn, transactionLevel, tx, atom);
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

            if (tx.shouldRollback()) {
                conn.rollback();
            } else {
                conn.commit();
                tx.executeOnAfterCommit();
                // config.executeCallbackAfterTxCommit();   // 支持 tx 中的 onAfterCommit 即可满足需求
            }

            return ret;

        } catch (Exception e) {
            if (conn != null) try {conn.rollback();} catch (Exception e1) {LogKit.error(e1.getMessage(), e1);}

            if (tx.getOnException() != null) {
                return tx.getOnException().apply(e);
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
                // config.removeCallbackAfterTxCommit();    // 支持 tx 中的 onAfterCommit 即可满足需求
            }
        }
    }

    private <R> R handleNestedTransaction(Connection conn, int transactionLevel, Transaction<R> tx, TransactionAtom<R> atom) {
        try {
            if (conn.getTransactionIsolation() < transactionLevel) {
                conn.setTransactionIsolation(transactionLevel);
            }

            R ret = atom.run(tx);
            if (ret instanceof TransactionRollbackDecision && ((TransactionRollbackDecision)ret).shouldRollback()) {
                tx.rollback();
            }
            return ret;

        } catch (SQLException e) {
            tx.rollback();
            throw new ActiveRecordException(e);
        }
    }
}


