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
	
	// 基本属性 url、user、password
	private String url;
	private String username;
	private String password;
	private String driverClass = null;	// 由 "com.mysql.jdbc.Driver" 改为 null 让 druid 自动探测 driverClass 值
	
	// 初始连接池大小、最小空闲连接数、最大活跃连接数
	private int initialSize = 10;
	private int minIdle = 10;
	private int maxActive = 100;
	
	// 配置获取连接等待超时的时间
	private long maxWait = DruidDataSource.DEFAULT_MAX_WAIT;
	
	// 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
	private long timeBetweenEvictionRunsMillis = DruidDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
	// 配置连接在池中最小生存的时间
	private long minEvictableIdleTimeMillis = DruidDataSource.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
	// 配置发生错误时多久重连
	private long timeBetweenConnectErrorMillis = DruidDataSource.DEFAULT_TIME_BETWEEN_CONNECT_ERROR_MILLIS;
	
	/**
	 * hsqldb - "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS"
	 * Oracle - "select 1 from dual"
	 * DB2 - "select 1 from sysibm.sysdummy1"
	 * mysql - "select 1"
	 */
	private String validationQuery = "select 1";
	private boolean testWhileIdle = true;
	private boolean testOnBorrow = false;
	private boolean testOnReturn = false;
	
	// 是否打开连接泄露自动检测
	private boolean removeAbandoned = false;
	// 连接长时间没有使用，被认为发生泄露时长
	private long removeAbandonedTimeoutMillis = 300 * 1000;
	// 发生泄露时是否需要输出 log，建议在开启连接泄露检测时开启，方便排错
	private boolean logAbandoned = false;
	
	// 是否缓存preparedStatement，即PSCache，对支持游标的数据库性能提升巨大，如 oracle、mysql 5.5 及以上版本
	// private boolean poolPreparedStatements = false;	// oracle、mysql 5.5 及以上版本建议为 true;
	
	// 只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，使用oracle时可以设定此值。
	private int maxPoolPreparedStatementPerConnectionSize = -1;
	
	// 配置监控统计拦截的filters
	private String filters;	// 监控统计："stat"    防SQL注入："wall"     组合使用： "stat,wall"
	private List<Filter> filterList;
	
	private DruidDataSource ds;
	
	public DruidPlugin(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}
	
	public DruidPlugin(String url, String username, String password, String driverClass) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.driverClass = driverClass;
	}
	
	public DruidPlugin(String url, String username, String password, String driverClass, String filters) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.driverClass = driverClass;
		this.filters = filters;
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
		ds = new DruidDataSource();
		
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
		ds.setTestWhileIdle(testWhileIdle);
		ds.setTestOnBorrow(testOnBorrow);
		ds.setTestOnReturn(testOnReturn);
		
		ds.setRemoveAbandoned(removeAbandoned);
		ds.setRemoveAbandonedTimeoutMillis(removeAbandonedTimeoutMillis);
		ds.setLogAbandoned(logAbandoned);
		
		//只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，参照druid的源码
		ds.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
		
		if (StrKit.notBlank(filters))
			try {ds.setFilters(filters);} catch (SQLException e) {throw new RuntimeException(e);}
		
		addFilterList(ds);
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
		return true;
	}
	
	public DataSource getDataSource() {
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
	
	public final void setTimeBetweenConnectErrorMillis(long timeBetweenConnectErrorMillis) {
		this.timeBetweenConnectErrorMillis = timeBetweenConnectErrorMillis;
	}
	
	public final void setRemoveAbandoned(boolean removeAbandoned) {
		this.removeAbandoned = removeAbandoned;
	}
	
	public final void setRemoveAbandonedTimeoutMillis(long removeAbandonedTimeoutMillis) {
		this.removeAbandonedTimeoutMillis = removeAbandonedTimeoutMillis;
	}
	
	public final void setLogAbandoned(boolean logAbandoned) {
		this.logAbandoned = logAbandoned;
	}
}
