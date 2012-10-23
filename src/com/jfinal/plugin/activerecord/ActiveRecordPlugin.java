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

package com.jfinal.plugin.activerecord;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.cache.ICache;
import com.jfinal.plugin.activerecord.dialect.Dialect;

/**
 * ActiveRecord plugin.
 * 
 * ActiveRecord plugin not support mysql type year, you can use int instead of year. 
 * Mysql error message for type year when insert a record: Data truncated for column 'xxx' at row 1
 */
public class ActiveRecordPlugin implements IPlugin {
	
	private static boolean isStarted = false;
	private static DataSource dataSource;
	private static IDataSourceProvider dataSourceProvider;
	private static final List<TableInfo> tableMappings = new ArrayList<TableInfo>();
	
	public static void setDevMode(boolean devMode) {
		DbKit.setDevMode(devMode);
	}
	
	public ActiveRecordPlugin setCache(ICache cache) {
		if (cache != null)
			DbKit.setCache(cache);
		return this;
	}
	
	public ActiveRecordPlugin setDialect(Dialect dialect) {
		if (dialect != null)
			DbKit.setDialect(dialect);
		return this;
	}
	
	public ActiveRecordPlugin setShowSql(boolean showSql) {
		DbKit.setShowSql(showSql);
		return this;
	}
	
	public ActiveRecordPlugin setMapFactory(IMapFactory mapFactory) {
		DbKit.setMapFactory(mapFactory);
		return this;
	}
	
	public ActiveRecordPlugin(IDataSourceProvider dataSourceProvider) {
		ActiveRecordPlugin.dataSourceProvider = dataSourceProvider;
	}
	
	public ActiveRecordPlugin(IDataSourceProvider dataSourceProvider, int transactionLevel) {
		ActiveRecordPlugin.dataSourceProvider = dataSourceProvider;
		DbKit.setTransactionLevel(transactionLevel);
	}
	
	public ActiveRecordPlugin(DataSource dataSource) {
		ActiveRecordPlugin.dataSource = dataSource;
	}
	
	public ActiveRecordPlugin(DataSource dataSource, int transactionLevel) {
		ActiveRecordPlugin.dataSource = dataSource;
		DbKit.setTransactionLevel(transactionLevel);
	}
	
	public ActiveRecordPlugin addMapping(String tableName, String primaryKey, Class<? extends Model<?>> modelClass) {
		tableMappings.add(new TableInfo(tableName, primaryKey, modelClass));
		return this;
	}
	
	public ActiveRecordPlugin addMapping(String tableName, Class<? extends Model<?>> modelClass) {
		tableMappings.add(new TableInfo(tableName, modelClass));
		return this;
	}
	
	public boolean start() {
		if (isStarted)
			return true;
		
		if (dataSourceProvider != null)
			dataSource = dataSourceProvider.getDataSource();
		
		if (dataSource == null)
			throw new RuntimeException("ActiveRecord start error: ActiveRecordPlugin need DataSource or DataSourceProvider");
		
		DbKit.setDataSource(dataSource);
		
		isStarted = true;
		return TableInfoBuilder.buildTableInfo(tableMappings);
	}
	
	public boolean stop() {
		isStarted = false;
		return true;
	}
}
