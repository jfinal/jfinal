/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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
import java.sql.SQLException;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.NestedTransactionHelpException;

/**
 * ActiveRecord declare transaction.
 * Example: @Before(Tx.class)
 */
public class Tx implements Interceptor {
	
	static Config getConfigWithTxConfig(ActionInvocation ai) {
		TxConfig txConfig = ai.getMethod().getAnnotation(TxConfig.class);
		if (txConfig == null)
			txConfig = ai.getController().getClass().getAnnotation(TxConfig.class);
		
		if (txConfig != null) {
			Config config = DbKit.getConfig(txConfig.value());
			if (config == null)
				throw new RuntimeException("Config not found with TxConfig");
			return config;
		}
		return null;
	}
	
	protected int getTransactionLevel(Config config) {
		return config.getTransactionLevel();
	}
	
	public void intercept(ActionInvocation ai) {
		Config config = getConfigWithTxConfig(ai);
		if (config == null)
			config = DbKit.getConfig();
		
		Connection conn = config.getThreadLocalConnection();
		if (conn != null) {	// Nested transaction support
			try {
				if (conn.getTransactionIsolation() < getTransactionLevel(config))
					conn.setTransactionIsolation(getTransactionLevel(config));
				ai.invoke();
				return ;
			} catch (SQLException e) {
				throw new ActiveRecordException(e);
			}
		}
		
		Boolean autoCommit = null;
		try {
			conn = config.getConnection();
			autoCommit = conn.getAutoCommit();
			config.setThreadLocalConnection(conn);
			conn.setTransactionIsolation(getTransactionLevel(config));	// conn.setTransactionIsolation(transactionLevel);
			conn.setAutoCommit(false);
			ai.invoke();
			conn.commit();
		} catch (NestedTransactionHelpException e) {
			if (conn != null) try {conn.rollback();} catch (Exception e1) {e1.printStackTrace();}
		} catch (Throwable t) {
			if (conn != null) try {conn.rollback();} catch (Exception e1) {e1.printStackTrace();}
			throw new ActiveRecordException(t);
		}
		finally {
			try {
				if (conn != null) {
					if (autoCommit != null)
						conn.setAutoCommit(autoCommit);
					conn.close();
				}
			} catch (Throwable t) {
				t.printStackTrace();	// can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
			}
			finally {
				config.removeThreadLocalConnection();	// prevent memory leak
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



