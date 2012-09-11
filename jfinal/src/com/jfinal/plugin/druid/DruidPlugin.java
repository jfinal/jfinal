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

package com.jfinal.plugin.druid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import com.jfinal.util.StringKit;

/**
 * DruidPlugin.
 */
public class DruidPlugin implements IPlugin, IDataSourceProvider {
	
	// 基本属性 url、user、password
	private String url;
	private String username;
	private String password;
	private String driverClass = "com.mysql.jdbc.Driver";
	
	// 配置初始化大小、最小、最大
	private int initialSize = 10;
	private int minIdle = 10;
	private int maxActive = 100;
	
	// 配置获取连接等待超时的时间
	private long maxWait = 60000;
	
	// 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
	private long timeBetweenEvictionRunsMillis = 60000;
	
	// 配置一个连接在池中最小生存的时间，单位是毫秒
	private long minEvictableIdleTimeMillis = 300000;
	
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
	
	// 打开PSCache，并且指定每个连接上PSCache的大小
	private boolean poolPreparedStatements = false;	// mysql 建议为 false, oracle 建议为 true;
	private int maxPoolPreparedStatementPerConnectionSize = 20;
	
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
		ds.setDriverClassName(driverClass);
		ds.setInitialSize(initialSize);
		ds.setMinIdle(minIdle);
		ds.setMaxActive(maxActive);
		ds.setMaxWait(maxWait);
		ds.setTimeBetweenConnectErrorMillis(timeBetweenEvictionRunsMillis);
		ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		
		ds.setValidationQuery(validationQuery);
		ds.setTestWhileIdle(testWhileIdle);
		ds.setTestOnBorrow(testOnBorrow);
		ds.setTestOnReturn(testOnReturn);
		
		ds.setPoolPreparedStatements(poolPreparedStatements);
		if (poolPreparedStatements == true)
			ds.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
		
		if (StringKit.notBlank(filters))
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
	
	public DruidPlugin setPoolPreparedStatements(boolean poolPreparedStatements) {
		this.poolPreparedStatements = poolPreparedStatements;
		return this;
	}
	
	public DruidPlugin setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
		this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
		return this;
	}
}
