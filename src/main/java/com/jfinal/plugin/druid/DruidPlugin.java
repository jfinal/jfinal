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

package com.jfinal.plugin.druid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.IDataSourceProvider;

/**
 * DruidPlugin.
 */
public class DruidPlugin implements IPlugin, IDataSourceProvider {
	//连接池的名称
	protected String name = null;
	
	// 基本属性 url、user、password
	protected String url;
	protected String username;
	protected String password;
	protected String publicKey;
	protected String driverClass = null;	// 由 "com.mysql.jdbc.Driver" 改为 null 让 druid 自动探测 driverClass 值
	
	// 初始连接池大小、最小空闲连接数、最大活跃连接数
	protected int initialSize = 1;
	protected int minIdle = 10;
	protected int maxActive = 32;
	
	// 配置获取连接等待超时的时间
	protected long maxWait = DruidDataSource.DEFAULT_MAX_WAIT;
	
	// 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
	protected long timeBetweenEvictionRunsMillis = DruidDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
	// 配置连接在池中最小生存的时间
	protected long minEvictableIdleTimeMillis = DruidDataSource.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
	// 配置发生错误时多久重连
	protected long timeBetweenConnectErrorMillis = DruidDataSource.DEFAULT_TIME_BETWEEN_CONNECT_ERROR_MILLIS;
	
	/**
	 * hsqldb - "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS"
	 * Oracle - "select 1 from dual"
	 * DB2 - "select 1 from sysibm.sysdummy1"
	 * mysql - "select 1"
	 */
	protected String validationQuery = "select 1";
	protected String  connectionInitSql = null;
	protected String connectionProperties = null;
	protected boolean testWhileIdle = true;
	protected boolean testOnBorrow = false;
	protected boolean testOnReturn = false;
	
	// 是否打开连接泄露自动检测
	protected boolean removeAbandoned = false;
	// 连接长时间没有使用，被认为发生泄露时长
	protected long removeAbandonedTimeoutMillis = 300 * 1000;
	// 发生泄露时是否需要输出 log，建议在开启连接泄露检测时开启，方便排错
	protected boolean logAbandoned = false;
	
	// 是否缓存preparedStatement，即PSCache，对支持游标的数据库性能提升巨大，如 oracle、mysql 5.5 及以上版本
	// protected boolean poolPreparedStatements = false;	// oracle、mysql 5.5 及以上版本建议为 true;
	
	// 只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，使用oracle时可以设定此值。
	protected int maxPoolPreparedStatementPerConnectionSize = -1;
	
	protected Integer defaultTransactionIsolation = null;
	protected Integer validationQueryTimeout = null;
	protected Integer timeBetweenLogStatsMillis = null;
	protected Boolean keepAlive = null;
	
	// 配置监控统计拦截的filters
	protected String filters;	// 监控统计："stat"    防SQL注入："wall"     组合使用： "stat,wall"
	protected List<Filter> filterList;
	
	protected DruidDataSource ds;
	protected volatile boolean isStarted = false;
	
	public DruidPlugin(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.validationQuery = autoCheckValidationQuery(url);
	}
	
	public DruidPlugin(String url, String username, String password, String driverClass) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.driverClass = driverClass;
		this.validationQuery = autoCheckValidationQuery(url);
	}
	
	public DruidPlugin(String url, String username, String password, String driverClass, String filters) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.driverClass = driverClass;
		this.filters = filters;
		this.validationQuery = autoCheckValidationQuery(url);
	}
	
	/**
	 * @Title: autoCheckValidationQuery  
	 * @Description: 自动设定探测sql 
	 * @param url
	 * @return 
	 * @since V1.0.0
	 */
	private static String  autoCheckValidationQuery(String url){
		if(url.startsWith("jdbc:oracle")){
			return "select 1 from dual";
		}else if(url.startsWith("jdbc:db2")){
			return "select 1 from sysibm.sysdummy1";
		}else if(url.startsWith("jdbc:hsqldb")){
			return "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS";
		}else if(url.startsWith("jdbc:derby")){
			return "values (1)";
		}
		return "select 1";
	}
	
	/**
	 * 添加连接时的初始化sql。可以添加多次，在初次连接时使用，比如指定编码或者默认scheme等
	 * @param sql
	 */
	public void setConnectionInitSql(String sql){
		this.connectionInitSql = sql;
	}
	
	public final String getName() {
		return name;
	}

	/**
	 * 连接池名称
	 *
	 * @param name
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * 设置过滤器，如果要开启监控统计需要使用此方法或在构造方法中进行设置
	 * <p>
	 * 监控统计："stat"
	 * 防SQL注入："wall"
	 * 组合使用： "stat,wall"
	 * </p>
	 */
	public DruidPlugin setFilters(String filters) {
		this.filters = filters;
		return this;
	}
	
	public synchronized DruidPlugin addFilter(Filter filter) {
		if (filterList == null)
			filterList = new ArrayList<Filter>();
		filterList.add(filter);
		return this;
	}
	
	public boolean start() {
		if (isStarted)
			return true;
		
		ds = new DruidDataSource();
		if(this.name != null){
			ds.setName(this.name);
		}
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		if (driverClass != null)
			ds.setDriverClassName(driverClass);
		ds.setInitialSize(initialSize);
		ds.setMinIdle(minIdle);
		ds.setMaxActive(maxActive);
		ds.setMaxWait(maxWait);
		ds.setTimeBetweenConnectErrorMillis(timeBetweenConnectErrorMillis);
		ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		
		ds.setValidationQuery(validationQuery);
		if(StrKit.notBlank(connectionInitSql)){
			List<String> connectionInitSqls = new ArrayList<String>();
			connectionInitSqls.add(this.connectionInitSql);
			ds.setConnectionInitSqls(connectionInitSqls);
		}
		ds.setTestWhileIdle(testWhileIdle);
		ds.setTestOnBorrow(testOnBorrow);
		ds.setTestOnReturn(testOnReturn);
		
		ds.setRemoveAbandoned(removeAbandoned);
		ds.setRemoveAbandonedTimeoutMillis(removeAbandonedTimeoutMillis);
		ds.setLogAbandoned(logAbandoned);
		
		//只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，参照druid的源码
		ds.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
		
		if (defaultTransactionIsolation != null) {
			ds.setDefaultTransactionIsolation(defaultTransactionIsolation);
		}
		if (validationQueryTimeout != null) {
			ds.setValidationQueryTimeout(validationQueryTimeout);
		}
		if (timeBetweenLogStatsMillis != null) {
			ds.setTimeBetweenLogStatsMillis(timeBetweenLogStatsMillis);
		}
		if (keepAlive != null) {
			ds.setKeepAlive(keepAlive);
		}
		
		boolean hasSetConnectionProperties = false;
		if (StrKit.notBlank(filters)){
			try {
				ds.setFilters(filters);
				//支持加解密数据库
				if(filters.contains("config")){
					//判断是否设定了公钥
					if(StrKit.isBlank(this.publicKey)){
						throw new RuntimeException("Druid连接池的filter设定了config时，必须设定publicKey");
					}
					String decryptStr = "config.decrypt=true;config.decrypt.key="+this.publicKey;
					String cp = this.connectionProperties;
					if(StrKit.isBlank(cp)){
						cp = decryptStr;
					}else{
						cp = cp + ";" + decryptStr;
					}
					ds.setConnectionProperties(cp);
					hasSetConnectionProperties = true;
				}
			} catch (SQLException e) {throw new RuntimeException(e);}
		}
		//确保setConnectionProperties被调用过一次
		if(!hasSetConnectionProperties && StrKit.notBlank(this.connectionProperties)){
			ds.setConnectionProperties(this.connectionProperties);
		}
		addFilterList(ds);
		
		isStarted = true;
		return true;
	}
	
	private void addFilterList(DruidDataSource ds) {
		if (filterList != null) {
			List<Filter> targetList = ds.getProxyFilters();
			for (Filter add : filterList) {
				boolean found = false;
				for (Filter target : targetList) {
					if (add.getClass().equals(target.getClass())) {
						found = true;
						break;
					}
				}
				if (! found)
					targetList.add(add);
			}
		}
	}
	
	public boolean stop() {
		if (ds != null)
			ds.close();
		
		ds = null;
		isStarted = false;
		return true;
	}
	
	public DataSource getDataSource() {
		return ds;
	}
	
	/**
	 * 支持高版本 druid 下配置 connectTimeout、socketTimeout。使用方法如下：
	 *    druidPlugin.getDruidDataSource().setConnectTimeout(xxx);
	 *    druidPlugin.getDruidDataSource().setSocketTimeout(xxx);
	 */
	public DruidDataSource getDruidDataSource() {
	    return ds;
	}
	
	public DruidPlugin set(int initialSize, int minIdle, int maxActive) {
		this.initialSize = initialSize;
		this.minIdle = minIdle;
		this.maxActive = maxActive;
		return this;
	}
	
	public DruidPlugin setDriverClass(String driverClass) {
		this.driverClass = driverClass;
		return this;
	}
	
	public DruidPlugin setInitialSize(int initialSize) {
		this.initialSize = initialSize;
		return this;
	}
	
	public DruidPlugin setMinIdle(int minIdle) {
		this.minIdle = minIdle;
		return this;
	}
	
	public DruidPlugin setMaxActive(int maxActive) {
		this.maxActive = maxActive;
		return this;
	}
	
	public DruidPlugin setMaxWait(long maxWait) {
		this.maxWait = maxWait;
		return this;
	}
	
	public DruidPlugin setDefaultTransactionIsolation(int defaultTransactionIsolation) {
		this.defaultTransactionIsolation = defaultTransactionIsolation;
		return this;
	}
	
	public DruidPlugin setValidationQueryTimeout(int validationQueryTimeout) {
		this.validationQueryTimeout = validationQueryTimeout;
		return this;
	}
	
	public DruidPlugin setTimeBetweenLogStatsMillis(int timeBetweenLogStatsMillis) {
		this.timeBetweenLogStatsMillis = timeBetweenLogStatsMillis;
		return this;
	}
	
	public DruidPlugin setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
		return this;
	}
	
	public DruidPlugin setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
		return this;
	}
	
	public DruidPlugin setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		return this;
	}
	
	/**
	 * hsqldb - "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS"
	 * Oracle - "select 1 from dual"
	 * DB2 - "select 1 from sysibm.sysdummy1"
	 * mysql - "select 1"
	 */
	public DruidPlugin setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
		return this;
	}
	
	public DruidPlugin setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
		return this;
	}
	
	public DruidPlugin setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
		return this;
	}
	
	public DruidPlugin setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
		return this;
	}
	
	public DruidPlugin setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
		this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
		return this;
	}
	
	public final DruidPlugin setTimeBetweenConnectErrorMillis(long timeBetweenConnectErrorMillis) {
		this.timeBetweenConnectErrorMillis = timeBetweenConnectErrorMillis;
		return this;
	}
	
	public final DruidPlugin setRemoveAbandoned(boolean removeAbandoned) {
		this.removeAbandoned = removeAbandoned;
		return this;
	}
	
	public final DruidPlugin setRemoveAbandonedTimeoutMillis(long removeAbandonedTimeoutMillis) {
		this.removeAbandonedTimeoutMillis = removeAbandonedTimeoutMillis;
		return this;
	}
	
	public final DruidPlugin setLogAbandoned(boolean logAbandoned) {
		this.logAbandoned = logAbandoned;
		return this;
	}

	public final DruidPlugin setConnectionProperties(String connectionProperties) {
		this.connectionProperties = connectionProperties;
		return this;
	}

	public final DruidPlugin setPublicKey(String publicKey) {
		this.publicKey = publicKey;
		return this;
	}
}
