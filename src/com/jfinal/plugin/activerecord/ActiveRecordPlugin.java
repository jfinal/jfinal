/**
 * Copyright (c) 2011-2016, James Zhan 詹波 (jfinal@126.com).
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
	
	private String configName = null;
	private DataSource dataSource = null;
	private IDataSourceProvider dataSourceProvider = null;
	private Integer transactionLevel = null;
	private ICache cache = null;
	private Boolean showSql = null;
	private Boolean devMode = null;
	private Dialect dialect = null;
	private IContainerFactory containerFactory = null;
	
	private Config config = null;
	
	private boolean isStarted = false;
	private List<Table> tableList = new ArrayList<Table>();
	
	public ActiveRecordPlugin(String configName, DataSource dataSource, int transactionLevel) {
		if (StrKit.isBlank(configName)) {
			throw new IllegalArgumentException("configName can not be blank");
		}
		if (dataSource == null) {
			throw new IllegalArgumentException("dataSource can not be null");
		}
		this.configName = configName.trim();
		this.dataSource = dataSource;
		this.setTransactionLevel(transactionLevel);
	}
	
	public ActiveRecordPlugin(DataSource dataSource) {
		this(DbKit.MAIN_CONFIG_NAME, dataSource);
	}
	
	public ActiveRecordPlugin(String configName, DataSource dataSource) {
		this(configName, dataSource, DbKit.DEFAULT_TRANSACTION_LEVEL);
	}
	
	public ActiveRecordPlugin(DataSource dataSource, int transactionLevel) {
		this(DbKit.MAIN_CONFIG_NAME, dataSource, transactionLevel);
	}
	
	public ActiveRecordPlugin(String configName, IDataSourceProvider dataSourceProvider, int transactionLevel) {
		if (StrKit.isBlank(configName)) {
			throw new IllegalArgumentException("configName can not be blank");
		}
		if (dataSourceProvider == null) {
			throw new IllegalArgumentException("dataSourceProvider can not be null");
		}
		this.configName = configName.trim();
		this.dataSourceProvider = dataSourceProvider;
		this.setTransactionLevel(transactionLevel);
	}
	
	public ActiveRecordPlugin(IDataSourceProvider dataSourceProvider) {
		this(DbKit.MAIN_CONFIG_NAME, dataSourceProvider);
	}
	
	public ActiveRecordPlugin(String configName, IDataSourceProvider dataSourceProvider) {
		this(configName, dataSourceProvider, DbKit.DEFAULT_TRANSACTION_LEVEL);
	}
	
	public ActiveRecordPlugin(IDataSourceProvider dataSourceProvider, int transactionLevel) {
		this(DbKit.MAIN_CONFIG_NAME, dataSourceProvider, transactionLevel);
	}
	
	public ActiveRecordPlugin(Config config) {
		if (config == null) {
			throw new IllegalArgumentException("Config can not be null");
		}
		this.config = config;
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
		if (t != 0 && t != 1  && t != 2  && t != 4  && t != 8) {
			throw new IllegalArgumentException("The transactionLevel only be 0, 1, 2, 4, 8");
		}
		this.transactionLevel = transactionLevel;
		return this;
	}
	
	public ActiveRecordPlugin setCache(ICache cache) {
		if (cache == null) {
			throw new IllegalArgumentException("cache can not be null");
		}
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
		if (dialect == null) {
			throw new IllegalArgumentException("dialect can not be null");
		}
		this.dialect = dialect;
		return this;
	}
	
	public ActiveRecordPlugin setContainerFactory(IContainerFactory containerFactory) {
		if (containerFactory == null) {
			throw new IllegalArgumentException("containerFactory can not be null");
		}
		this.containerFactory = containerFactory;
		return this;
	}
	
	/**
	 * 当使用 create table 语句创建用于开发使用的数据表副本时，假如create table 中使用的
	 * 复合主键次序不同，那么MappingKitGeneretor 反射生成的复合主键次序也会不同。
	 * 
	 * 而程序中类似于 model.deleteById(id1, id2) 方法中复合主键次序与需要与映射时的次序
	 * 保持一致，可以在MappingKit 映射完成以后通过调用此方法再次强制指定复合主键次序
	 * 
	 * <pre>
	 * Example:
	 * ActiveRecrodPlugin arp = new ActiveRecordPlugin(...);
	 * _MappingKit.mapping(arp);
	 * arp.setPrimaryKey("account_role", "account_id, role_id");
	 * me.add(arp);
	 * </pre>
	 */
	public void setPrimaryKey(String tableName, String primaryKey) {
		for (Table table : tableList) {
			if (table.getName().equalsIgnoreCase(tableName.trim())) {
				table.setPrimaryKey(primaryKey);
			}
		}
	}
	
	public boolean start() {
		if (isStarted) {
			return true;
		}
		if (configName == null) {
			configName = DbKit.MAIN_CONFIG_NAME;
		}
		if (dataSource == null && dataSourceProvider != null) {
			dataSource = dataSourceProvider.getDataSource();
		}
		if (dataSource == null) {
			throw new RuntimeException("ActiveRecord start error: ActiveRecordPlugin need DataSource or DataSourceProvider");
		}
		if (config == null) {
			config = new Config(configName, dataSource);
		}
		
		if (dialect != null) {
			config.dialect = dialect;
		}
		if (showSql != null) {
			config.showSql = showSql;
		}
		if (devMode != null) {
			config.devMode = devMode;
		}
		if (transactionLevel != null) {
			config.transactionLevel = transactionLevel;
		}
		if (containerFactory != null) {
			config.containerFactory = containerFactory;
		}
		if (cache != null) {
			config.cache = cache;
		}
		
		new TableBuilder().build(tableList, config);
		DbKit.addConfig(config);
		Db.init();
		isStarted = true;
		return true;
	}
	
	public boolean stop() {
		isStarted = false;
		return true;
	}
	
	/**
	 * 用于分布式场景，当某个分布式节点只需要用 Model 承载和传输数据，而不需要实际操作数据库时
	 * 调用本方法可保障 IContainerFactory、Dialect、ICache 的一致性
	 * 
	 * 本用法更加适用于 Generator 生成的继承自 base model的 Model，更加便于传统第三方工具对
	 * 带有 getter、setter 的 model 进行各种处理
	 * 
	 * <pre>
	 * 警告：Dialect、IContainerFactory、ICache 三者一定要与集群中其它节点保持一致，
	 *     以免程序出现不一致行为
	 * </pre>
	 */
	public static void useAsDataTransfer(Dialect dialect, IContainerFactory containerFactory, ICache cache) {
		if (dialect == null) {
			throw new IllegalArgumentException("dialect can not be null");
		}
		if (containerFactory == null) {
			throw new IllegalArgumentException("containerFactory can not be null");
		}
		if (cache == null) {
			throw new IllegalArgumentException("cache can not be null");
		}
		ActiveRecordPlugin arp = new ActiveRecordPlugin(new NullDataSource());
		arp.setDialect(dialect);
		arp.setContainerFactory(containerFactory);
		arp.setCache(cache);
		arp.start();
		DbKit.brokenConfig = arp.config;
	}
	
	/**
	 * 分布式场景下指定 IContainerFactory，并默认使用 MysqlDialect、EhCache
	 * @see #useAsDataTransfer(Dialect, IContainerFactory, ICache)
	 */
	public static void useAsDataTransfer(IContainerFactory containerFactory) {
		useAsDataTransfer(new com.jfinal.plugin.activerecord.dialect.MysqlDialect(), containerFactory, new com.jfinal.plugin.activerecord.cache.EhCache());
	}
	
	/**
	 * 分布式场景下指定 Dialect、IContainerFactory，并默认使用 EhCache
	 * @see #useAsDataTransfer(Dialect, IContainerFactory, ICache)
	 */
	public static void useAsDataTransfer(Dialect dialect, IContainerFactory containerFactory) {
		useAsDataTransfer(dialect, containerFactory, new com.jfinal.plugin.activerecord.cache.EhCache());
	}
	
	/**
	 * 分布式场景下指定 Dialect、 并默认使用 IContainerFactory.defaultContainerFactory、EhCache
	 * @see #useAsDataTransfer(Dialect, IContainerFactory, ICache)
	 */
	public static void useAsDataTransfer(Dialect dialect) {
		useAsDataTransfer(dialect, IContainerFactory.defaultContainerFactory, new com.jfinal.plugin.activerecord.cache.EhCache());
	}
	
	/**
	 * 分布式场景下默认使用 MysqlDialect、 IContainerFactory.defaultContainerFactory、EhCache
	 * @see #useAsDataTransfer(Dialect, IContainerFactory, ICache)
	 */
	public static void useAsDataTransfer() {
		useAsDataTransfer(new com.jfinal.plugin.activerecord.dialect.MysqlDialect(), IContainerFactory.defaultContainerFactory, new com.jfinal.plugin.activerecord.cache.EhCache());
	}
}






