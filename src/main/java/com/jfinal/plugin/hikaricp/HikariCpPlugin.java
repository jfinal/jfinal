/**
 * Copyright (c) 2011-2017, myaniu 玛雅牛 (myaniu@gmail.com).
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
package com.jfinal.plugin.hikaricp;

import javax.sql.DataSource;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** 
 * Fast, simple, reliable. HikariCP is a "zero-overhead" production ready JDBC connection pool. 
 * At roughly 130Kb, the library is very light 
 * @ClassName: HikaricpPlugin  
 */
public class HikariCpPlugin implements IPlugin, IDataSourceProvider{
	/**
	 * jdbc Url
	 */
	private String jdbcUrl;
	
	/**
	 * username
	 */
	private String username;
	
	/**
	 * password
	 */
	private String password;
	
	/**
	 * default auto-commit behavior of connections returned from the pool
	 * Default：true
	 */
	private boolean autoCommit = true;
	
	/**
	 * the maximum number of milliseconds that a client (that's you) 
	 * will wait for a connection from the pool 
	 * Default: 30000 (30 seconds)
	 */
	private long connectionTimeout = 30000;
	
	/**
	 * the maximum amount of time that a connection is allowed to sit idle in the pool
	 * Default: 600000 (10 minutes)
	 */
	private long idleTimeout = 600000;
	
	/**
	 * the maximum lifetime of a connection in the pool
	 * Default: 1800000 (30 minutes)
	 */
	private long maxLifetime = 1800000;
	
	/**
	 * If your driver supports JDBC4 we strongly recommend not setting this property. 
	 * This is for "legacy" databases that do not support the JDBC4 Connection.isValid() API
	 * Default: none
	 */
	private String connectionTestQuery = null;
	
	/**
	 * the maximum size that the pool is allowed to reach, including both idle and in-use connections
	 * Default: 10
	 */
	private int maximumPoolSize = 10;
	
	/**
	 * user-defined name for the connection pool and appears mainly in logging
	 * Default: auto-generated
	 */
	private String poolName = null;
	
	/**
	 * This property controls whether Connections obtained from the pool are in read-only mode by default.
	 * Default: false
	 */
	private boolean readOnly = false;
	
	/**
	 * the default catalog for databases that support the concept of catalogs.
	 * Default: driver default
	 */
	private String catalog = null;
	
	/**
	 * a SQL statement that will be executed after every new connection creation before adding it to the pool
	 * Default: none
	 */
	private String connectionInitSql = null;
	
	/**
	 * HikariCP will attempt to resolve a driver through the DriverManager based solely on the jdbcUrl,
	 * but for some older drivers the driverClassName must also be specified
	 * Default: none
	 */
	private String driverClass = null;
	
	/**
	 * the default transaction isolation level of connections returned from the pool.
	 * Default: driver default
	 */
	private String transactionIsolation = null;
	
	/**
	 * the maximum amount of time that a connection will be tested for aliveness. 
	 * This value must be less than the connectionTimeout. Lowest acceptable validation timeout is 250 ms. 
	 * Default: 5000(5 seconds)
	 */
	private long validationTimeout = 5000;
	
	/**
	 * the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak.
	 * A value of 0 means leak detection is disabled. Lowest acceptable value for enabling leak detection is 2000 (2 seconds).
	 * Default: 0
	 */
	private long leakDetectionThreshold = 0;
	
	/**
	 * Hikari DataSource
	 */
	private HikariDataSource ds;
	
	public HikariCpPlugin(String jdbcUrl, String username, String password) {
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
	}
	
	public HikariCpPlugin(String jdbcUrl, String username, String password, String driverClass) {
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
		this.driverClass = driverClass;
	}
	

	@Override
	public DataSource getDataSource() {
		return ds;
	}

	@Override
	public boolean start() {
		HikariConfig config = new HikariConfig();
		//设定基本参数
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(username);
		config.setPassword(password);
		
		//设定额外参数
		config.setAutoCommit(autoCommit);
		config.setReadOnly(readOnly);
	    
		config.setConnectionTimeout(connectionTimeout);
		config.setIdleTimeout(idleTimeout);
		config.setMaxLifetime(maxLifetime);
		config.setMaximumPoolSize(maximumPoolSize);
		config.setValidationTimeout(validationTimeout);
		
		if(StrKit.notBlank(driverClass)){
			config.setDriverClassName(driverClass);
		}
		
		if(StrKit.notBlank(transactionIsolation)){
			config.setTransactionIsolation(transactionIsolation);
		}

		if(this.leakDetectionThreshold != 0){
			config.setLeakDetectionThreshold(leakDetectionThreshold);
		}
		
		if(StrKit.notBlank(catalog)){
			config.setCatalog(catalog);
		}
		
		if(StrKit.notBlank(connectionTestQuery)){
			config.setConnectionTestQuery(connectionTestQuery);
		}
		
		if(StrKit.notBlank(poolName)){
			config.setPoolName(poolName);
		}
		
		if(StrKit.notBlank(connectionInitSql)){
			config.setConnectionInitSql(connectionInitSql);
		}
		
		if(jdbcUrl.toLowerCase().contains(":mysql:")){
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("useServerPrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "256");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		}
		if(jdbcUrl.toLowerCase().contains(":postgresql:")){
			if(this.readOnly){
				config.addDataSourceProperty("readOnly", "true");
			}
			config.setConnectionTimeout(0);
			config.addDataSourceProperty("prepareThreshold", "3");
			config.addDataSourceProperty("preparedStatementCacheQueries", "128");
			config.addDataSourceProperty("preparedStatementCacheSizeMiB", "4");
		}
		
		ds = new HikariDataSource(config);
		return true;
	}

	@Override
	public boolean stop() {
		if (ds != null)
			ds.close();
		return true;
	}

	/**  
	 * 驱动类名
	 * @param driverClass  
	 * @since V1.0.0
	 */
	public final void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	/**  
	 * 数据库类型
	 * @param username  
	 * @since V1.0.0
	 */
	public final void setUsername(String username) {
		this.username = username;
	}

	/**  
	 * 数据库密码
	 * @param password    
	 * @since V1.0.0
	 */
	public final void setPassword(String password) {
		this.password = password;
	}

	/**  
	 * 是否自动提交
	 * @param autoCommit  
	 * @since V1.0.0
	 */
	public final void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	/**  
	 * 是否是只读连接 ，是否有效取决于相应的数据库是否支持
	 * @param readOnly 
	 * @since V1.0.0
	 */
	public final void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**  
	 * @param connectionTimeoutMs  连接超时时间(单位：毫秒)  
	 * @since V1.0.0
	 */
	public final void setConnectionTimeout(long connectionTimeoutMs) {
		this.connectionTimeout = connectionTimeoutMs;
	}

	/**  
	 * 空闲超时时间(单位：毫秒)，默认600000 (10 分钟)
	 * @param idleTimeoutMs    
	 * @since V1.0.0
	 */
	public final void setIdleTimeout(long idleTimeoutMs) {
		this.idleTimeout = idleTimeoutMs;
	}

	/** 
	 * 最大生命周期/最大存活时间(单位：毫秒) ，默认1800000 (30 分钟)
	 * @param maxLifetime   
	 * @since V1.0.0
	 */
	public final void setMaxLifetime(long maxLifetimeMs) {
		this.maxLifetime = maxLifetimeMs;
	}

	/**  
	 * 连接池最大连接数 默认10
	 * @param maximumPoolSize 
	 * @since V1.0.0
	 */
	public final void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	/**  
	 * 用户指定的连接池名
	 * @param poolName  
	 * @since V1.0.0
	 */
	public final void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	/**  
	 * 新连接生成后，添加到连接池前执行的初始化sql
	 * @param connectionInitSql    
	 * @since V1.0.0
	 */
	public final void setConnectionInitSql(String connectionInitSql) {
		this.connectionInitSql = connectionInitSql;
	}


	/**
	 * JDBC4以下版本数据库驱动需要设定此参数
	 * @param connectionTestQuery 连接时测试sql
	 * @since V1.0.0
	 */
	public final void setConnectionTestQuery(String connectionTestQuery) {
		this.connectionTestQuery = connectionTestQuery;
	}

	/**
	 * jdbc连接url
	 * @param jdbcUrl
	 */
	public final void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	/**
	 * 支持 catalog 概念的数据库可以设定该参数
	 * @param catalog
	 */
	public final void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	/**
	 * 事物等级
	 * @param isolationLevel
	 */
	public final void setTransactionIsolation(String isolationLevel) {
		this.transactionIsolation = isolationLevel;
	}

	/**
	 * 连接是否存活测试周期，默认5000（5秒）
	 * @param validationTimeoutMs
	 */
	public final void setValidationTimeout(long validationTimeoutMs) {
		this.validationTimeout = validationTimeoutMs;
	}

	/**
	 * 内存泄露侦测周期，最小为2000（2秒）
	 * @param leakDetectionThresholdMs
	 */
	public final void setLeakDetectionThreshold(long leakDetectionThresholdMs) {
		this.leakDetectionThreshold = leakDetectionThresholdMs;
	}
}

