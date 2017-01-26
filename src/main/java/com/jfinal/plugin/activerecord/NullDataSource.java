/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * NullDataSource.
 */
class NullDataSource implements DataSource {
	
	private String msg = "Can not invoke the method of NullDataSource";
	
	public PrintWriter getLogWriter() throws SQLException {
		throw new RuntimeException(msg);
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		throw new RuntimeException(msg);
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		throw new RuntimeException(msg);
	}

	public int getLoginTimeout() throws SQLException {
		throw new RuntimeException(msg);
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new RuntimeException(msg);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new RuntimeException(msg);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new RuntimeException(msg);
	}

	public Connection getConnection() throws SQLException {
		throw new RuntimeException(msg);
	}

	public Connection getConnection(String username, String password) throws SQLException {
		throw new RuntimeException(msg);
	}
}




