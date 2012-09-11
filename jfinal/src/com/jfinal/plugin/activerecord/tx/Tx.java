/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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

import java.sql.Connection;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.DbKit;

/**
 * ActiveRecord declare transaction.
 * Example: @Before(Tx.class)
 */
public class Tx implements Interceptor {
	
	protected int getTransactionLevel() {
		return DbKit.getTransactionLevel();
	}
	
	public void intercept(ActionInvocation invocation) {
		if (DbKit.isExistsThreadLocalConnection())
			throw new ActiveRecordException("Nested transaction can not be supported. You can't execute transaction inside another transaction.");
		
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = DbKit.getDataSource().getConnection();
			autoCommit = conn.getAutoCommit();
			DbKit.setThreadLocalConnection(conn);
			conn.setTransactionIsolation(getTransactionLevel());	// conn.setTransactionIsolation(transactionLevel);
			conn.setAutoCommit(false);
			invocation.invoke();
			conn.commit();
		} catch (Exception e) {
			if (conn != null)
				try {conn.rollback();} catch (Exception e1) {e1.printStackTrace();}
			throw new ActiveRecordException(e);
		}
		finally {
			try {
				if (conn != null) {
					if (autoCommit != null)
						conn.setAutoCommit(autoCommit);
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();	// can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
			}
			finally {
				DbKit.removeThreadLocalConnection();	// prevent memory leak
			}
		}
	}
}

/**
 * Reentrance transaction, nested transaction in other words.
 * JFinal decide not to support nested transaction.
 * The code below is help to support nested transact in the future.
private void reentryTx() {
	Connection oldConn = DbKit.getThreadLocalConnection());	// Get connection from threadLocal directly
	Connection conn = null;
	try {
		conn = DbKit.getDataSource().getConnection();
		DbKit.setThreadLocalConnection(conn);
		conn.setTransactionIsolation(getTransactionLevel());	// conn.setTransactionIsolation(transactionLevel);
		conn.setAutoCommit(false);
		// here is service code
		conn.commit();
	} catch (Exception e) {
		if (conn != null)
			try {conn.rollback();} catch (SQLException e1) {e1.printStackTrace();}
		throw new ActiveRecordException(e);
	}
	finally {
		try {
			if (conn != null) {
				conn.setAutoCommit(true);
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();	// can not throw exception here, otherwise the more important exception in catch block can not be throw.
		}
		finally {
			if (oldConn != null)
				DbKit.setThreadLocalConnection(oldConn);
			else
				DbKit.removeThreadLocalConnection();	// prevent memory leak
		}
	}
}*/



