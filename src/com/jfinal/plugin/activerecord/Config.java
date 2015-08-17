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

package com.jfinal.plugin.activerecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.cache.EhCache;
import com.jfinal.plugin.activerecord.cache.ICache;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;

public class Config {
	
	String name;
	
	private final ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();
	
	DataSource dataSource;
	int transactionLevel = Connection.TRANSACTION_READ_COMMITTED;
	
	ICache cache = new EhCache();
	boolean showSql = false;
	boolean devMode = false;
	Dialect dialect = new MysqlDialect();
	
	IContainerFactory containerFactory = new IContainerFactory(){
		public Map<String, Object> getAttrsMap() {return new HashMap<String, Object>();}
		public Map<String, Object> getColumnsMap() {return new HashMap<String, Object>();}
		public Set<String> getModifyFlagSet() {return new HashSet<String>();}
	};
	
	/**
	 * For DbKit.brokenConfig = new Config();
	 */
	Config() {
		
	}
	
	/**
	 * Constructor with DataSource
	 * @param dataSource the dataSource, can not be null
	 */
	public Config(String name, DataSource dataSource) {
		if (StrKit.isBlank(name))
			throw new IllegalArgumentException("Config name can not be blank");
		if (dataSource == null)
			throw new IllegalArgumentException("DataSource can not be null");
		
		this.name = name.trim();
		this.dataSource = dataSource;
	}
	
	/**
	 * Constructor with DataSource and Dialect
	 * @param dataSource the dataSource, can not be null
	 * @param dialect the dialect, can not be null
	 */
	public Config(String name, DataSource dataSource, Dialect dialect) {
		if (StrKit.isBlank(name))
			throw new IllegalArgumentException("Config name can not be blank");
		if (dataSource == null)
			throw new IllegalArgumentException("DataSource can not be null");
		if (dialect == null)
			throw new IllegalArgumentException("Dialect can not be null");
		
		this.name = name.trim();
		this.dataSource = dataSource;
		this.dialect = dialect;
	}
	
	/**
	 * Constructor with full parameters
	 * @param dataSource the dataSource, can not be null
	 * @param dialect the dialect, set null with default value: new MysqlDialect()
	 * @param showSql the showSql,set null with default value: false
	 * @param devMode the devMode, set null with default value: false
	 * @param transactionLevel the transaction level, set null with default value: Connection.TRANSACTION_READ_COMMITTED
	 * @param containerFactory the containerFactory, set null with default value: new IContainerFactory(){......}
	 * @param cache the cache, set null with default value: new EhCache()
	 */
	public Config(String name,
				  DataSource dataSource,
				  Dialect dialect,
				  Boolean showSql,
				  Boolean devMode,
				  Integer transactionLevel,
				  IContainerFactory containerFactory,
				  ICache cache) {
		if (StrKit.isBlank(name))
			throw new IllegalArgumentException("Config name can not be blank");
		if (dataSource == null)
			throw new IllegalArgumentException("DataSource can not be null");
		
		this.name = name.trim();
		this.dataSource = dataSource;
		
		if (dialect != null)
			this.dialect = dialect;
		if (showSql != null)
			this.showSql = showSql;
		if (devMode != null)
			this.devMode = devMode;
		if (transactionLevel != null)
			this.transactionLevel = transactionLevel;
		if (containerFactory != null)
			this.containerFactory = containerFactory;
		if (cache != null)
			this.cache = cache;
	}
	
	public String getName() {
		return name;
	}
	
	public Dialect getDialect() {
		return dialect;
	}
	
	public ICache getCache() {
		return cache;
	}
	
	public int getTransactionLevel() {
		return transactionLevel;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public IContainerFactory getContainerFactory() {
		return containerFactory;
	}
	
	public boolean isShowSql() {
		return showSql;
	}
	
	public boolean isDevMode() {
		return devMode;
	}
	
	// --------
	
	/**
	 * Support transaction with Transaction interceptor
	 */
	public final void setThreadLocalConnection(Connection connection) {
		threadLocal.set(connection);
	}
	
	public final void removeThreadLocalConnection() {
		threadLocal.remove();
	}
	
	/**
	 * Get Connection. Support transaction if Connection in ThreadLocal
	 */
	public final Connection getConnection() throws SQLException {
		Connection conn = threadLocal.get();
		if (conn != null)
			return conn;
		return showSql ? new SqlReporter(dataSource.getConnection()).getConnection() : dataSource.getConnection();
	}
	
	/**
	 * Helps to implement nested transaction.
	 * Tx.intercept(...) and Db.tx(...) need this method to detected if it in nested transaction.
	 */
	public final Connection getThreadLocalConnection() {
		return threadLocal.get();
	}
	
	/**
	 * Return true if current thread in transaction.
	 */
	public final boolean isInTransaction() {
		return threadLocal.get() != null;
	}
	
	/**
	 * Close ResultSet、Statement、Connection
	 * ThreadLocal support declare transaction.
	 */
	public final void close(ResultSet rs, Statement st, Connection conn) {
		if (rs != null) {try {rs.close();} catch (SQLException e) {}}
		if (st != null) {try {st.close();} catch (SQLException e) {}}
		
		if (threadLocal.get() == null) {	// in transaction if conn in threadlocal
			if (conn != null) {try {conn.close();}
			catch (SQLException e) {throw new ActiveRecordException(e);}}
		}
	}
	
	public final void close(Statement st, Connection conn) {
		if (st != null) {try {st.close();} catch (SQLException e) {}}
		
		if (threadLocal.get() == null) {	// in transaction if conn in threadlocal
			if (conn != null) {try {conn.close();}
			catch (SQLException e) {throw new ActiveRecordException(e);}}
		}
	}
	
	public final void close(Connection conn) {
		if (threadLocal.get() == null)		// in transaction if conn in threadlocal
			if (conn != null)
				try {conn.close();} catch (SQLException e) {throw new ActiveRecordException(e);}
	}
}



