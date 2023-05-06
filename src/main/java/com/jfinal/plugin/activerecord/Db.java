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

package com.jfinal.plugin.activerecord;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;
import com.jfinal.kit.SyncWriteMap;

/**
 * Db. Powerful database query and update tool box.
 */
@SuppressWarnings("rawtypes")
public class Db {
	
	private static DbPro MAIN = null;
	private static final Map<String, DbPro> cache = new SyncWriteMap<String, DbPro>(32, 0.25F);
	
	/**
	 * for DbKit.addConfig(configName)
	 */
	static void init(String configName) {
		MAIN = DbKit.getConfig(configName).dbProFactory.getDbPro(configName); // new DbPro(configName);
		cache.put(configName, MAIN);
	}
	
    /**
     * for DbKit.removeConfig(configName)
     */
    static void removeDbProWithConfig(String configName) {
    	if (MAIN != null && MAIN.config.getName().equals(configName)) {
    		MAIN = null;
    	}
    	cache.remove(configName);
    }
    
    public static DbPro use(String configName) {
		DbPro result = cache.get(configName);
		if (result == null) {
			Config config = DbKit.getConfig(configName);
			if (config == null) {
				throw new IllegalArgumentException("Config not found by configName: " + configName);
			}
			result = config.dbProFactory.getDbPro(configName);	// new DbPro(configName);
			cache.put(configName, result);
		}
		return result;
	}
	
	public static DbPro use() {
		return MAIN;
	}
	
	static <T> List<T> query(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		return MAIN.query(config, conn, sql, paras);
	}
	
	/**
	 * @see #query(String, String, Object...)
	 */
	public static <T> List<T> query(String sql, Object... paras) {
		return MAIN.query(sql, paras);
	}
	
	/**
	 * @see #query(String, Object...)
	 * @param sql an SQL statement
	 */
	public static <T> List<T> query(String sql) {
		return MAIN.query(sql);
	}
	
	/**
	 * Execute sql query and return the first result. I recommend add "limit 1" in your sql.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return Object[] if your sql has select more than one column,
	 * 			and it return Object if your sql has select only one column.
	 */
	public static <T> T queryFirst(String sql, Object... paras) {
		return MAIN.queryFirst(sql, paras);
	}
	
	/**
	 * @see #queryFirst(String, Object...)
	 * @param sql an SQL statement
	 */
	public static <T> T queryFirst(String sql) {
		return MAIN.queryFirst(sql);
	}
	
	// 26 queryXxx method below -----------------------------------------------
	/**
	 * Execute sql query just return one column.
	 * @param <T> the type of the column that in your sql's select statement
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return <T> T
	 */
	public static <T> T queryColumn(String sql, Object... paras) {
		return MAIN.queryColumn(sql, paras);
	}
	
	public static <T> T queryColumn(String sql) {
		return MAIN.queryColumn(sql);
	}
	
	public static String queryStr(String sql, Object... paras) {
		return MAIN.queryStr(sql, paras);
	}
	
	public static String queryStr(String sql) {
		return MAIN.queryStr(sql);
	}
	
	public static Integer queryInt(String sql, Object... paras) {
		return MAIN.queryInt(sql, paras);
	}
	
	public static Integer queryInt(String sql) {
		return MAIN.queryInt(sql);
	}
	
	public static Long queryLong(String sql, Object... paras) {
		return MAIN.queryLong(sql, paras);
	}
	
	public static Long queryLong(String sql) {
		return MAIN.queryLong(sql);
	}
	
	public static Double queryDouble(String sql, Object... paras) {
		return MAIN.queryDouble(sql, paras);
	}
	
	public static Double queryDouble(String sql) {
		return MAIN.queryDouble(sql);
	}
	
	public static Float queryFloat(String sql, Object... paras) {
		return MAIN.queryFloat(sql, paras);
	}
	
	public static Float queryFloat(String sql) {
		return MAIN.queryFloat(sql);
	}
	
	public static BigDecimal queryBigDecimal(String sql, Object... paras) {
		return MAIN.queryBigDecimal(sql, paras);
	}
	
	public static BigDecimal queryBigDecimal(String sql) {
		return MAIN.queryBigDecimal(sql);
	}
	
	public static BigInteger queryBigInteger(String sql, Object... paras) {
		return MAIN.queryBigInteger(sql, paras);
	}
	
	public static BigInteger queryBigInteger(String sql) {
		return MAIN.queryBigInteger(sql);
	}
	
	public static byte[] queryBytes(String sql, Object... paras) {
		return MAIN.queryBytes(sql, paras);
	}
	
	public static byte[] queryBytes(String sql) {
		return MAIN.queryBytes(sql);
	}
	
	public static java.util.Date queryDate(String sql, Object... paras) {
		return MAIN.queryDate(sql, paras);
	}
	
	public static java.util.Date queryDate(String sql) {
		return MAIN.queryDate(sql);
	}
	
	public static LocalDateTime queryLocalDateTime(String sql, Object... paras) {
		return MAIN.queryLocalDateTime(sql, paras);
	}
	
	public static LocalDateTime queryLocalDateTime(String sql) {
		return MAIN.queryLocalDateTime(sql);
	}
	
	public static java.sql.Time queryTime(String sql, Object... paras) {
		return MAIN.queryTime(sql, paras);
	}
	
	public static java.sql.Time queryTime(String sql) {
		return MAIN.queryTime(sql);
	}
	
	public static java.sql.Timestamp queryTimestamp(String sql, Object... paras) {
		return MAIN.queryTimestamp(sql, paras);
	}
	
	public static java.sql.Timestamp queryTimestamp(String sql) {
		return MAIN.queryTimestamp(sql);
	}
	
	public static Boolean queryBoolean(String sql, Object... paras) {
		return MAIN.queryBoolean(sql, paras);
	}
	
	public static Boolean queryBoolean(String sql) {
		return MAIN.queryBoolean(sql);
	}
	
	public static Short queryShort(String sql, Object... paras) {
		return MAIN.queryShort(sql, paras);
	}
	
	public static Short queryShort(String sql) {
		return MAIN.queryShort(sql);
	}
	
	public static Byte queryByte(String sql, Object... paras) {
		return MAIN.queryByte(sql, paras);
	}
	
	public static Byte queryByte(String sql) {
		return MAIN.queryByte(sql);
	}
	
	public static Number queryNumber(String sql, Object... paras) {
		return MAIN.queryNumber(sql, paras);
	}
	
	public static Number queryNumber(String sql) {
		return MAIN.queryNumber(sql);
	}
	// 26 queryXxx method under -----------------------------------------------
	
	/**
	 * Execute sql update
	 */
	static int update(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		return MAIN.update(config, conn, sql, paras);
	}
	
	/**
	 * Execute update, insert or delete sql statement.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return either the row count for <code>INSERT</code>, <code>UPDATE</code>,
     *         or <code>DELETE</code> statements, or 0 for SQL statements 
     *         that return nothing
	 */
	public static int update(String sql, Object... paras) {
		return MAIN.update(sql, paras);
	}
	
	/**
	 * @see #update(String, Object...)
	 * @param sql an SQL statement
	 */
	public static int update(String sql) {
		return MAIN.update(sql);
	}
	
	static List<Record> find(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		return MAIN.find(config, conn, sql, paras);
	}
	
	/**
	 * @see #find(String, String, Object...)
	 */
	public static List<Record> find(String sql, Object... paras) {
		return MAIN.find(sql, paras);
	}
	
	/**
	 * @see #find(String, String, Object...)
	 * @param sql the sql statement
	 */
	public static List<Record> find(String sql) {
		return MAIN.find(sql);
	}
	
	public static List<Record> findAll(String tableName) {
		return MAIN.findAll(tableName);
	}
	
	/**
	 * Find first record. I recommend add "limit 1" in your sql.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return the Record object
	 */
	public static Record findFirst(String sql, Object... paras) {
		return MAIN.findFirst(sql, paras);
	}
	
	/**
	 * @see #findFirst(String, Object...)
	 * @param sql an SQL statement
	 */
	public static Record findFirst(String sql) {
		return MAIN.findFirst(sql);
	}
	
	/**
	 * Find record by id with default primary key.
	 * <pre>
	 * Example:
	 * Record user = Db.findById("user", 15);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 */
	public static Record findById(String tableName, Object idValue) {
		return MAIN.findById(tableName, idValue);
	}
	
	public static Record findById(String tableName, String primaryKey, Object idValue) {
		return MAIN.findById(tableName, primaryKey, idValue);
	}
	
	/**
	 * Find record by ids.
	 * <pre>
	 * Example:
	 * Record user = Db.findByIds("user", "user_id", 123);
	 * Record userRole = Db.findByIds("user_role", "user_id, role_id", 123, 456);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param idValues the id value of the record, it can be composite id values
	 */
	public static Record findByIds(String tableName, String primaryKey, Object... idValues) {
		return MAIN.findByIds(tableName, primaryKey, idValues);
	}
	
	/**
	 * Delete record by id with default primary key.
	 * <pre>
	 * Example:
	 * Db.deleteById("user", 15);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 * @return true if delete succeed otherwise false
	 */
	public static boolean deleteById(String tableName, Object idValue) {
		return MAIN.deleteById(tableName, idValue);
	}
	
	public static boolean deleteById(String tableName, String primaryKey, Object idValue) {
		return MAIN.deleteById(tableName, primaryKey, idValue);
	}
	
	/**
	 * Delete record by ids.
	 * <pre>
	 * Example:
	 * Db.deleteByIds("user", "user_id", 15);
	 * Db.deleteByIds("user_role", "user_id, role_id", 123, 456);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param idValues the id value of the record, it can be composite id values
	 * @return true if delete succeed otherwise false
	 */
	public static boolean deleteByIds(String tableName, String primaryKey, Object... idValues) {
		return MAIN.deleteByIds(tableName, primaryKey, idValues);
	}
	
	/**
	 * Delete record.
	 * <pre>
	 * Example:
	 * boolean succeed = Db.delete("user", "id", user);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param record the record
	 * @return true if delete succeed otherwise false
	 */
	public static boolean delete(String tableName, String primaryKey, Record record) {
		return MAIN.delete(tableName, primaryKey, record);
	}
	
	/**
	 * <pre>
	 * Example:
	 * boolean succeed = Db.delete("user", user);
	 * </pre>
	 * @see #delete(String, String, Record)
	 */
	public static boolean delete(String tableName, Record record) {
		return MAIN.delete(tableName, record);
	}
	
	/**
	 * Execute delete sql statement.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return the row count for <code>DELETE</code> statements, or 0 for SQL statements 
	 *         that return nothing
	 */
	public static int delete(String sql, Object... paras) {
		return MAIN.delete(sql, paras);
	}
	
	/**
	 * @see #delete(String, Object...)
	 * @param sql an SQL statement
	 */
	public static int delete(String sql) {
		return MAIN.delete(sql);
	}
	
	static Page<Record> paginate(Config config, Connection conn, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) throws SQLException {
		return MAIN.paginate(config, conn, pageNumber, pageSize, select, sqlExceptSelect, paras);
	}
	
	/**
	 * Paginate.
	 * @param pageNumber the page number
	 * @param pageSize the page size
	 * @param select the select part of the sql statement
	 * @param sqlExceptSelect the sql statement excluded select part
	 * @param paras the parameters of sql
	 * @return the Page object
	 */
	public static Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		return MAIN.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
	}
	
	public static Page<Record> paginate(int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		return MAIN.paginate(pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
	}
	
	/**
	 * @see #paginate(String, int, int, String, String, Object...)
	 */
	public static Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return MAIN.paginate(pageNumber, pageSize, select, sqlExceptSelect);
	}
	
	public static Page<Record> paginateByFullSql(int pageNumber, int pageSize, String totalRowSql, String findSql, Object... paras) {
		return MAIN.paginateByFullSql(pageNumber, pageSize, totalRowSql, findSql, paras);
	}
	
	public static Page<Record> paginateByFullSql(int pageNumber, int pageSize, boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
		return MAIN.paginateByFullSql(pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
	}
	
	static boolean save(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		return MAIN.save(config, conn, tableName, primaryKey, record);
	}
	
	/**
	 * Save record.
	 * <pre>
	 * Example:
	 * Record userRole = new Record().set("user_id", 123).set("role_id", 456);
	 * Db.save("user_role", "user_id, role_id", userRole);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param record the record will be saved
	 * @param true if save succeed otherwise false
	 */
	public static boolean save(String tableName, String primaryKey, Record record) {
		return MAIN.save(tableName, primaryKey, record);
	}
	
	/**
	 * @see #save(String, String, Record)
	 */
	public static boolean save(String tableName, Record record) {
		return MAIN.save(tableName, record);
	}
	
	static boolean update(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		return MAIN.update(config, conn, tableName, primaryKey, record);
	}
	
	/**
	 * Update Record.
	 * <pre>
	 * Example:
	 * Db.update("user_role", "user_id, role_id", record);
	 * </pre>
	 * @param tableName the table name of the Record save to
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param record the Record object
	 * @param true if update succeed otherwise false
	 */
	public static boolean update(String tableName, String primaryKey, Record record) {
		return MAIN.update(tableName, primaryKey, record);
	}
	
	/**
	 * Update record with default primary key.
	 * <pre>
	 * Example:
	 * Db.update("user", record);
	 * </pre>
	 * @see #update(String, String, Record)
	 */
	public static boolean update(String tableName, Record record) {
		return MAIN.update(tableName, record);
	}
	
	/**
	 * @see #execute(String, ICallback)
	 */
	public static Object execute(ICallback callback) {
		return MAIN.execute(callback);
	}
	
	/**
	 * Execute callback. It is useful when all the API can not satisfy your requirement.
	 * @param config the Config object
	 * @param callback the ICallback interface
	 */
	static Object execute(Config config, ICallback callback) {
		return MAIN.execute(config, callback);
	}
	
	/**
	 * Execute transaction.
	 * @param config the Config object
	 * @param transactionLevel the transaction level
	 * @param atom the atom operation
	 * @return true if transaction executing succeed otherwise false
	 */
	static boolean tx(Config config, int transactionLevel, IAtom atom) {
		return MAIN.tx(config, transactionLevel, atom);
	}
	
	/**
	 * Execute transaction with default transaction level.
	 * @see #tx(int, IAtom)
	 */
	public static boolean tx(IAtom atom) {
		return MAIN.tx(atom);
	}
	
	public static boolean tx(int transactionLevel, IAtom atom) {
		return MAIN.tx(transactionLevel, atom);
	}
	
	/**
	 * 主要用于嵌套事务场景
	 * 
	 * 实例：https://jfinal.com/feedback/4008
	 * 
	 * 默认情况下嵌套事务会被合并成为一个事务，那么内层与外层任何地方回滚事务
	 * 所有嵌套层都将回滚事务，也就是说嵌套事务无法独立提交与回滚
	 * 
	 * 使用 txInNewThread(...) 方法可以实现层之间的事务控制的独立性
	 * 由于事务处理是将 Connection 绑定到线程上的，所以 txInNewThread(...)
	 * 通过建立新线程来实现嵌套事务的独立控制
	 */
	public static Future<Boolean> txInNewThread(IAtom atom) {
		return MAIN.txInNewThread(atom);
	}
	
	public static Future<Boolean> txInNewThread(int transactionLevel, IAtom atom) {
		return MAIN.txInNewThread(transactionLevel, atom);
	}
	
	/**
	 * Find Record by cache.
	 * @see #find(String, Object...)
	 * @param cacheName the cache name
	 * @param key the key used to get date from cache
	 * @return the list of Record
	 */
	public static List<Record> findByCache(String cacheName, Object key, String sql, Object... paras) {
		return MAIN.findByCache(cacheName, key, sql, paras);
	}
	
	/**
	 * @see #findByCache(String, Object, String, Object...)
	 */
	public static List<Record> findByCache(String cacheName, Object key, String sql) {
		return MAIN.findByCache(cacheName, key, sql);
	}
	
	/**
	 * Find first record by cache. I recommend add "limit 1" in your sql.
	 * @see #findFirst(String, Object...)
	 * @param cacheName the cache name
	 * @param key the key used to get date from cache
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return the Record object
	 */
	public static Record findFirstByCache(String cacheName, Object key, String sql, Object... paras) {
		return MAIN.findFirstByCache(cacheName, key, sql, paras);
	}
	
	/**
	 * @see #findFirstByCache(String, Object, String, Object...)
	 */
	public static Record findFirstByCache(String cacheName, Object key, String sql) {
		return MAIN.findFirstByCache(cacheName, key, sql);
	}
	
	/**
	 * Paginate by cache.
	 * @see #paginate(int, int, String, String, Object...)
	 * @return Page
	 */
	public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		return MAIN.paginateByCache(cacheName, key, pageNumber, pageSize, select, sqlExceptSelect, paras);
	}
	
	public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		return MAIN.paginateByCache(cacheName, key, pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
	}
	
	/**
	 * @see #paginateByCache(String, Object, int, int, String, String, Object...)
	 */
	public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return MAIN.paginateByCache(cacheName, key, pageNumber, pageSize, select, sqlExceptSelect);
	}
	
	/**
	 * @see DbPro#batch(String, Object[][], int)
     */
    public static int[] batch(String sql, Object[][] paras, int batchSize) {
    	return MAIN.batch(sql, paras, batchSize);
    }
	
	/**
	 * @see DbPro#batch(String, String, List, int)
     */
	public static int[] batch(String sql, String columns, List modelOrRecordList, int batchSize) {
		return MAIN.batch(sql, columns, modelOrRecordList, batchSize);
	}
	
	/**
	 * @see DbPro#batch(List, int)
     */
    public static int[] batch(List<String> sqlList, int batchSize) {
    	return MAIN.batch(sqlList, batchSize);
    }
    
    /**
	 * @see DbPro#batchSave(List, int)
     */
    public static int[] batchSave(List<? extends Model> modelList, int batchSize) {
    	return MAIN.batchSave(modelList, batchSize);
    }
    
    /**
	 * @see DbPro#batchSave(String, List, int)
     */
    public static int[] batchSave(String tableName, List<? extends Record> recordList, int batchSize) {
    	return MAIN.batchSave(tableName, recordList, batchSize);
    }
    
    /**
	 * @see DbPro#batchUpdate(List, int)
     */
    public static int[] batchUpdate(List<? extends Model> modelList, int batchSize) {
    	return MAIN.batchUpdate(modelList, batchSize);
    }
    
    /**
	 * @see DbPro#batchUpdate(String, String, List, int)
     */
    public static int[] batchUpdate(String tableName, String primaryKey, List<? extends Record> recordList, int batchSize) {
    	return MAIN.batchUpdate(tableName, primaryKey, recordList, batchSize);
    }
    
    /**
	 * @see DbPro#batchUpdate(String, List, int)
     */
    public static int[] batchUpdate(String tableName, List<? extends Record> recordList, int batchSize) {
    	return MAIN.batchUpdate(tableName, recordList, batchSize);
    }
    
    public static String getSql(String key) {
    	return MAIN.getSql(key);
    }
    
    // 支持传入变量用于 sql 生成。为了避免用户将参数拼接在 sql 中引起 sql 注入风险，只在 SqlKit 中开放该功能
    // public static String getSql(String key, Map data) {
    //     return MAIN.getSql(key, data);
    // }
    
    public static SqlPara getSqlPara(String key, Record record) {
    	return MAIN.getSqlPara(key, record);
    }
    
    public static SqlPara getSqlPara(String key, Model model) {
    	return MAIN.getSqlPara(key, model);
    }
    
    public static SqlPara getSqlPara(String key, Map data) {
    	return MAIN.getSqlPara(key, data);
    }
    
    public static SqlPara getSqlPara(String key, Object... paras) {
    	return MAIN.getSqlPara(key, paras);
    }
	
	public static SqlPara getSqlParaByString(String content, Map data) {
		return MAIN.getSqlParaByString(content, data);
	}
	
	public static SqlPara getSqlParaByString(String content, Object... paras) {
		return MAIN.getSqlParaByString(content, paras);
	}
	
    public static List<Record> find(SqlPara sqlPara) {
    	return MAIN.find(sqlPara);
    }
    
    public static Record findFirst(SqlPara sqlPara) {
    	return MAIN.findFirst(sqlPara);
    }
    
    public static int update(SqlPara sqlPara) {
    	return MAIN.update(sqlPara);
    }
    
    public static Page<Record> paginate(int pageNumber, int pageSize, SqlPara sqlPara) {
    	return MAIN.paginate(pageNumber, pageSize, sqlPara);
    }
	
	public static Page<Record> paginate(int pageNumber, int pageSize, boolean isGroupBySql, SqlPara sqlPara) {
		return MAIN.paginate(pageNumber, pageSize, isGroupBySql, sqlPara);
	}
	
	// ---------
	
	/**
	 * 迭代处理每一个查询出来的 Record 对象
	 * <pre>
	 * 例子：
	 * Db.each(record -> {
	 *    // 处理 record 的代码在此
	 *    
	 *    // 返回 true 继续循环处理下一条数据，返回 false 立即终止循环
	 *    return true;
	 * }, sql, paras);
	 * </pre>
	 */
	public static void each(Function<Record, Boolean> func, String sql, Object... paras) {
		MAIN.each(func, sql, paras);
	}
	
	// ---------
	
	/**
	 * 使用 sql 模板进行查询，可以省去 Db.getSqlPara(...) 调用
	 * 
	 * <pre>
	 * 例子：
	 * Db.template("blog.find", Kv.by("id", 123).find();
	 * </pre>
	 */
	public static DbTemplate template(String key, Map data) {
		return MAIN.template(key, data);
	}
	
	/**
	 * 使用 sql 模板进行查询，可以省去 Db.getSqlPara(...) 调用
	 * 
	 * <pre>
	 * 例子：
	 * Db.template("blog.find", 123).find();
	 * </pre>
	 */
	public static DbTemplate template(String key, Object... paras) {
		return MAIN.template(key, paras);
	}
	
	// ---------
	
	/**
	 * 使用字符串变量作为 sql 模板进行查询，可省去外部 sql 文件来使用
	 * sql 模板功能
	 * 
	 * <pre>
	 * 例子：
	 * String sql = "select * from blog where id = #para(id)";
	 * Db.templateByString(sql, Kv.by("id", 123).find();
	 * </pre>
	 */
	public static DbTemplate templateByString(String content, Map data) {
		return MAIN.templateByString(content, data);
	}
	
	/**
	 * 使用字符串变量作为 sql 模板进行查询，可省去外部 sql 文件来使用
	 * sql 模板功能
	 * 
	 * <pre>
	 * 例子：
	 * String sql = "select * from blog where id = #para(0)";
	 * Db.templateByString(sql, 123).find();
	 * </pre>
	 */
	public static DbTemplate templateByString(String content, Object... paras) {
		return MAIN.templateByString(content, paras);
	}
}



