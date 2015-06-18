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
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.cache.ICache;
import com.jfinal.plugin.activerecord.dialect.Dialect;

/**
 * ActiveRecord plugin.
 * <br>
 * ActiveRecord plugin not support mysql type year, you can use int instead of year. 
 * Mysql error message for type year when insert a record: Data truncated for column 'xxx' at row 1
 */
public class ActiveRecordPlugin implements IPlugin {
	
	private String configName = DbKit.MAIN_CONFIG_NAME;
	private Config config = null;
	
	private DataSource dataSource;
	private IDataSourceProvider dataSourceProvider;
	private Integer transactionLevel = null;
	private ICache cache = null;
	private Boolean showSql = null;
	private Boolean devMode = null;
	private Dialect dialect = null;
	private IContainerFactory containerFactory = null;
	
	private boolean isStarted = false;
	private List<Table> tableList = new ArrayList<Table>();
	
	public ActiveRecordPlugin(Config config) {
		if (config == null)
			throw new IllegalArgumentException("Config can not be null");
		this.config = config;
	}
	
	public ActiveRecordPlugin(DataSource dataSource) {
		this(DbKit.MAIN_CONFIG_NAME, dataSource);
	}
	
	public ActiveRecordPlugin(String configName, DataSource dataSource) {
		this(configName, dataSource, Connection.TRANSACTION_READ_COMMITTED);
	}
	
	public ActiveRecordPlugin(DataSource dataSource, int transactionLevel) {
		this(DbKit.MAIN_CONFIG_NAME, dataSource, transactionLevel);
	}
	
	public ActiveRecordPlugin(String configName, DataSource dataSource, int transactionLevel) {
		if (StrKit.isBlank(configName))
			throw new IllegalArgumentException("configName can not be blank");
		if (dataSource == null)
			throw new IllegalArgumentException("dataSource can not be null");
		this.configName = configName.trim();
		this.dataSource = dataSource;
		this.setTransactionLevel(transactionLevel);
	}
	
	public ActiveRecordPlugin(IDataSourceProvider dataSourceProvider) {
		this(DbKit.MAIN_CONFIG_NAME, dataSourceProvider);
	}
	
	public ActiveRecordPlugin(String configName, IDataSourceProvider dataSourceProvider) {
		this(configName, dataSourceProvider, Connection.TRANSACTION_READ_COMMITTED);
	}
	
	public ActiveRecordPlugin(IDataSourceProvider dataSourceProvider, int transactionLevel) {
		this(DbKit.MAIN_CONFIG_NAME, dataSourceProvider, transactionLevel);
	}
	
	public ActiveRecordPlugin(String configName, IDataSourceProvider dataSourceProvider, int transactionLevel) {
		if (StrKit.isBlank(configName))
			throw new IllegalArgumentException("configName can not be blank");
		if (dataSourceProvider == null)
			throw new IllegalArgumentException("dataSourceProvider can not be null");
		this.configName = configName.trim();
		this.dataSourceProvider = dataSourceProvider;
		this.setTransactionLevel(transactionLevel);
	}
	
	public ActiveRecordPlugin addMapping(String tableName, String primaryKey, Class<? extends Model<?>> modelClass) {
		tableList.add(new Table(tableName, primaryKey, modelClass));
		return this;
	}
	
	public ActiveRecordPlugin addMapping(String tableName, Class<? extends Model<?>> modelClass) {
		tableList.add(new Table(tableName, modelClass));
		return this;
	}
	
	/**
	 * Set transaction level define in java.sql.Connection
	 * @param transactionLevel only be 0, 1, 2, 4, 8
	 */
	public ActiveRecordPlugin setTransactionLevel(int transactionLevel) {
		int t = transactionLevel;
		if (t != 0 && t != 1  && t != 2  && t != 4  && t != 8)
			throw new IllegalArgumentException("The transactionLevel only be 0, 1, 2, 4, 8");
		this.transactionLevel = transactionLevel;
		return this;
	}
	
	public ActiveRecordPlugin setCache(ICache cache) {
		if (cache == null)
			throw new IllegalArgumentException("cache can not be null");
		this.cache = cache;
		return this;
	}
	
	public ActiveRecordPlugin setShowSql(boolean showSql) {
		this.showSql = showSql;
		return this;
	}
	
	public ActiveRecordPlugin setDevMode(boolean devMode) {
		this.devMode = devMode;
		return this;
	}
	
	public Boolean getDevMode() {
		return devMode;
	}
	
	public ActiveRecordPlugin setDialect(Dialect dialect) {
		if (dialect == null)
			throw new IllegalArgumentException("dialect can not be null");
		this.dialect = dialect;
		return this;
	}
	
	public ActiveRecordPlugin setContainerFactory(IContainerFactory containerFactory) {
		if (containerFactory == null)
			throw new IllegalArgumentException("containerFactory can not be null");
		this.containerFactory = containerFactory;
		return this;
	}
	
	public boolean start() {
		if (isStarted)
			return true;
		
		if (dataSourceProvider != null)
			dataSource = dataSourceProvider.getDataSource();
		if (dataSource == null)
			throw new RuntimeException("ActiveRecord start error: ActiveRecordPlugin need DataSource or DataSourceProvider");
		
		if (config == null)
			config = new Config(configName, dataSource, dialect, showSql, devMode, transactionLevel, containerFactory, cache);
		DbKit.addConfig(config);
		
		TableBuilder.build(tableList, config);
		Db.init();
		isStarted = true;
		return true;
	}
	
	public boolean stop() {
		isStarted = false;
		return true;
	}
}







