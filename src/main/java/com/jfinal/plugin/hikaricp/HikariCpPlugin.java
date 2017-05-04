/**
 * Copyright (C) 2015 wellbole
 * @Package com.jfinal.plugin.hikaricp  
 * @Title: HikaricpPlugin.java  
 * @Description: Hikaricp(A high-performance JDBC connection pool) JFinal plugin.
 * @author 李飞 (lifei@wellbole.com)    
 * @date 2015年8月9日  上午10:04:06  
 * @since V1.0.0 
 *
 * Modification History:
 * Date         Author      Version     Description
 * -------------------------------------------------------------
 * 2015年8月9日      李飞                       V1.0.0        新建文件   
 */
package com.jfinal.plugin.hikaricp;

import javax.sql.DataSource;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**  
 * @ClassName: HikaricpPlugin  
 * @Description:Hikaricp(A high-performance JDBC connection pool) JFinal plugin. 
 * @author 李飞 (lifei@wellbole.com)   
 * @date 2015年8月9日 上午10:04:06
 * @since V1.0.0  
 */
public class HikariCpPlugin implements IPlugin, IDataSourceProvider{
	/**
	 * 驱动类名
	 */
	private String driverClass;
	
	/**
	 * jdbc Url
	 */
	private String url;
	
	/**
	 * 用户名
	 */
	private String username;
	
	/**
	 * 密码
	 */
	private String password;
	
	/**
	 * 控制自动提交行为，默认：true
	 */
	private boolean autoCommit = true;
	
	/**
	 * 只读连接，默认：false
	 */
	private boolean readOnly = false;
	
	/**
	 * 连接超时时间(单位毫秒) 默认：30秒
	 */
	private long connectionTimeout = 30000;
	
	/**
	 * 空闲超时时间(单位毫秒) 默认：10分钟
	 */
	private long idleTimeout = 600000;
	
	/**
	 * 一个连接的最大生命周期/最大存活时间(单位毫秒)，强烈建议设定此值 默认：30分钟
	 */
	private long maxLifetime = 1800000;
	
	/**
	 * 连接池最大连接数
	 */
	private int maximumPoolSize = 10;
	
	/**
	 * 连接池名，未指定时自动生成
	 */
	private String poolName = null;
	
	/**
	 * 新连接生成后，添加到连接池前执行的初始化sql
	 */
	private String connectionInitSql = null;
	
	/**
	 * 老版本的连接池需要设定这个参数
	 */
	private String connectionTestQuery = null;
	
	/**
	 * Hikari DataSourc 实现
	 */
	private HikariDataSource ds;
	
	
	/**
	 * <p>Title: HikaricpPlugin</p>  
	 * <p>Description: 构造函数</p>  
	 * @param url jdbcUrl串
	 * @param username 用户名
	 * @param password 密码
	 * @param driverClass 驱动类名
	 * @since V1.0.0
	 */
	public HikariCpPlugin(String url, String username, String password, String driverClass) {
		this.url = url;
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
		config.setDriverClassName(driverClass);
		config.setJdbcUrl(url);
		config.setUsername(username);
		config.setPassword(password);
		
		//设定额外参数
		config.setAutoCommit(autoCommit);
		config.setReadOnly(readOnly);
	    
		config.setConnectionTimeout(connectionTimeout);
		config.setIdleTimeout(idleTimeout);
		config.setMaxLifetime(maxLifetime);
		config.setMaximumPoolSize(maximumPoolSize);
		
		//老版本的jdbc驱动需要设定此参数。
		if(StrKit.notBlank(connectionTestQuery)){
			config.setConnectionTestQuery(connectionTestQuery);
		}
		
		//指定连接池名称，方便多数据库时区分。
		if(StrKit.notBlank(poolName)){
			config.setPoolName(poolName);
		}
		
		//指定 新连接生成后，添加到连接池前执行的初始化sql
		if(StrKit.notBlank(connectionInitSql)){
			config.setConnectionInitSql(connectionInitSql);
		}
		
		//探测是否是mysql
		if(url.toLowerCase().contains(":mysql:")){
			//性能优化。
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("useServerPrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "256");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		}
		//探测是否是postgres驱动
		if(url.toLowerCase().contains(":postgresql:")){
			if(this.readOnly){
				config.addDataSourceProperty("readOnly", "true");
			}
			//postgresql不支持这个
			config.setConnectionTimeout(0);
			//性能优化。
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
	 * @param driverClass  驱动类名  
	 * @since V1.0.0
	 */
	public final void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	/**  
	 * @param url  jdbc Url  
	 * @since V1.0.0
	 */
	public final void setUrl(String url) {
		this.url = url;
	}

	/**  
	 * @param username  用户名  
	 * @since V1.0.0
	 */
	public final void setUsername(String username) {
		this.username = username;
	}

	/**  
	 * @param password  密码  
	 * @since V1.0.0
	 */
	public final void setPassword(String password) {
		this.password = password;
	}

	/**  
	 * @param autoCommit  是否自动提交  
	 * @since V1.0.0
	 */
	public final void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	/**  
	 * @param readOnly  是否是只读连接  
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
	 * @param idleTimeoutMs  空闲超时时间(单位：毫秒)    
	 * @since V1.0.0
	 */
	public final void setIdleTimeout(long idleTimeoutMs) {
		this.idleTimeout = idleTimeoutMs;
	}

	/**  
	 * @param maxLifetime  最大生命周期/最大存活时间(单位：毫秒)  
	 * @since V1.0.0
	 */
	public final void setMaxLifetime(long maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	/**  
	 * @param maximumPoolSize  连接池最大连接数  
	 * @since V1.0.0
	 */
	public final void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	/**  
	 * @param poolName  连接池名  
	 * @since V1.0.0
	 */
	public final void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	/**  
	 * @param connectionInitSql  新连接生成后，添加到连接池前执行的初始化sql  
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

}
