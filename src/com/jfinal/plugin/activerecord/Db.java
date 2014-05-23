/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
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
import java.sql.SQLException;
import java.util.List;

/**
 * Db. Powerful database query and update tool box.
 */
@SuppressWarnings("rawtypes")
public class Db {
	
	private static DbPro pro = null;
	
	static void init() {
		pro = DbPro.use();
	}
	
	public static DbPro use(String configName) {
		return DbPro.use(configName);
	}
	
	static <T> List<T> query(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		return pro.query(config, conn, sql, paras);
	}
	
	/**
	 * @see #query(String, String, Object...)
	 */
	public static <T> List<T> query(String sql, Object... paras) {
		return pro.query(sql, paras);
	}
	
	/**
	 * @see #query(String, Object...)
	 * @param sql an SQL statement
	 */
	public static <T> List<T> query(String sql) {
		return pro.query(sql);
	}
	
	/**
	 * Execute sql query and return the first result. I recommend add "limit 1" in your sql.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return Object[] if your sql has select more than one column,
	 * 			and it return Object if your sql has select only one column.
	 */
	public static <T> T queryFirst(String sql, Object... paras) {
		return pro.queryFirst(sql, paras);
	}
	
	/**
	 * @see #queryFirst(String, Object...)
	 * @param sql an SQL statement
	 */
	public static <T> T queryFirst(String sql) {
		return pro.queryFirst(sql);
	}
	
	// 26 queryXxx method below -----------------------------------------------
	/**
	 * Execute sql query just return one column.
	 * @param <T> the type of the column that in your sql's select statement
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return List<T>
	 */
	public static <T> T queryColumn(String sql, Object... paras) {
		return pro.queryColumn(sql, paras);
	}
	
	public static <T> T queryColumn(String sql) {
		return pro.queryColumn(sql);
	}
	
	public static String queryStr(String sql, Object... paras) {
		return pro.queryStr(sql, paras);
	}
	
	public static String queryStr(String sql) {
		return pro.queryStr(sql);
	}
	
	public static Integer queryInt(String sql, Object... paras) {
		return pro.queryInt(sql, paras);
	}
	
	public static Integer queryInt(String sql) {
		return pro.queryInt(sql);
	}
	
	public static Long queryLong(String sql, Object... paras) {
		return pro.queryLong(sql, paras);
	}
	
	public static Long queryLong(String sql) {
		return pro.queryLong(sql);
	}
	
	public static Double queryDouble(String sql, Object... paras) {
		return pro.queryDouble(sql, paras);
	}
	
	public static Double queryDouble(String sql) {
		return pro.queryDouble(sql);
	}
	
	public static Float queryFloat(String sql, Object... paras) {
		return pro.queryFloat(sql, paras);
	}
	
	public static Float queryFloat(String sql) {
		return pro.queryFloat(sql);
	}
	
	public static java.math.BigDecimal queryBigDecimal(String sql, Object... paras) {
		return pro.queryBigDecimal(sql, paras);
	}
	
	public static java.math.BigDecimal queryBigDecimal(String sql) {
		return pro.queryBigDecimal(sql);
	}
	
	public static byte[] queryBytes(String sql, Object... paras) {
		return pro.queryBytes(sql, paras);
	}
	
	public static byte[] queryBytes(String sql) {
		return pro.queryBytes(sql);
	}
	
	public static java.util.Date queryDate(String sql, Object... paras) {
		return pro.queryDate(sql, paras);
	}
	
	public static java.util.Date queryDate(String sql) {
		return pro.queryDate(sql);
	}
	
	public static java.sql.Time queryTime(String sql, Object... paras) {
		return pro.queryTime(sql, paras);
	}
	
	public static java.sql.Time queryTime(String sql) {
		return pro.queryTime(sql);
	}
	
	public static java.sql.Timestamp queryTimestamp(String sql, Object... paras) {
		return pro.queryTimestamp(sql, paras);
	}
	
	public static java.sql.Timestamp queryTimestamp(String sql) {
		return pro.queryTimestamp(sql);
	}
	
	public static Boolean queryBoolean(String sql, Object... paras) {
		return pro.queryBoolean(sql, paras);
	}
	
	public static Boolean queryBoolean(String sql) {
		return pro.queryBoolean(sql);
	}
	
	public static Number queryNumber(String sql, Object... paras) {
		return pro.queryNumber(sql, paras);
	}
	
	public static Number queryNumber(String sql) {
		return pro.queryNumber(sql);
	}
	// 26 queryXxx method under -----------------------------------------------
	
	/**
	 * Execute sql update
	 */
	static int update(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		return pro.update(config, conn, sql, paras);
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
		return pro.update(sql, paras);
	}
	
	/**
	 * @see #update(String, Object...)
	 * @param sql an SQL statement
	 */
	public static int update(String sql) {
		return pro.update(sql);
	}
	
	static List<Record> find(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		return pro.find(config, conn, sql, paras);
	}
	
	/**
	 * @see #find(String, String, Object...)
	 */
	public static List<Record> find(String sql, Object... paras) {
		return pro.find(sql, paras);
	}
	
	/**
	 * @see #find(String, String, Object...)
	 * @param sql the sql statement
	 */
	public static List<Record> find(String sql) {
		return pro.find(sql);
	}
	
	/**
	 * Find first record. I recommend add "limit 1" in your sql.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return the Record object
	 */
	public static Record findFirst(String sql, Object... paras) {
		return pro.findFirst(sql, paras);
	}
	
	/**
	 * @see #findFirst(String, Object...)
	 * @param sql an SQL statement
	 */
	public static Record findFirst(String sql) {
		return pro.findFirst(sql);
	}
	
	/**
	 * Find record by id.
	 * Example: Record user = Db.findById("user", 15);
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 */
	public static Record findById(String tableName, Object idValue) {
		return pro.findById(tableName, idValue);
	}
	
	/**
	 * Find record by id. Fetch the specific columns only.
	 * Example: Record user = Db.findById("user", 15, "name, age");
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 * @param columns the specific columns separate with comma character ==> ","
	 */
	public static Record findById(String tableName, Number idValue, String columns) {
		return pro.findById(tableName, idValue, columns);
	}
	
	/**
	 * Find record by id.
	 * Example: Record user = Db.findById("user", "user_id", 15);
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table
	 * @param idValue the id value of the record
	 */
	public static Record findById(String tableName, String primaryKey, Number idValue) {
		return pro.findById(tableName, primaryKey, idValue);
	}
	
	/**
	 * Find record by id. Fetch the specific columns only.
	 * Example: Record user = Db.findById("user", "user_id", 15, "name, age");
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table
	 * @param idValue the id value of the record
	 * @param columns the specific columns separate with comma character ==> ","
	 */
	public static Record findById(String tableName, String primaryKey, Object idValue, String columns) {
		return pro.findById(tableName, primaryKey, idValue, columns);
	}
	
	/**
	 * Delete record by id.
	 * Example: boolean succeed = Db.deleteById("user", 15);
	 * @param tableName the table name of the table
	 * @param id the id value of the record
	 * @return true if delete succeed otherwise false
	 */
	public static boolean deleteById(String tableName, Object id) {
		return pro.deleteById(tableName, id);
	}
	
	/**
	 * Delete record by id.
	 * Example: boolean succeed = Db.deleteById("user", "user_id", 15);
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table
	 * @param id the id value of the record
	 * @return true if delete succeed otherwise false
	 */
	public static boolean deleteById(String tableName, String primaryKey, Object id) {
		return pro.deleteById(tableName, primaryKey, id);
	}
	
	/**
	 * Delete record.
	 * Example: boolean succeed = Db.delete("user", "id", user);
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table
	 * @param record the record
	 * @return true if delete succeed otherwise false
	 */
	public static boolean delete(String tableName, String primaryKey, Record record) {
		return pro.delete(tableName, primaryKey, record);
	}
	
	/**
	 * Example: boolean succeed = Db.delete("user", user);
	 * @see #delete(String, String, Record)
	 */
	public static boolean delete(String tableName, Record record) {
		return pro.delete(tableName, record);
	}
	
	static Page<Record> paginate(Config config, Connection conn, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) throws SQLException {
		return pro.paginate(config, conn, pageNumber, pageSize, select, sqlExceptSelect, paras);
	}
	
	/**
	 * @see #paginate(String, int, int, String, String, Object...)
	 */
	public static Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		return pro.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
	}
	
	/**
	 * @see #paginate(String, int, int, String, String, Object...)
	 */
	public static Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return pro.paginate(pageNumber, pageSize, select, sqlExceptSelect);
	}
	
	static boolean save(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		return pro.save(config, conn, tableName, primaryKey, record);
	}
	
	/**
	 * Save record.
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table
	 * @param record the record will be saved
	 * @param true if save succeed otherwise false
	 */
	public static boolean save(String tableName, String primaryKey, Record record) {
		return pro.save(tableName, primaryKey, record);
	}
	
	/**
	 * @see #save(String, String, Record)
	 */
	public static boolean save(String tableName, Record record) {
		return pro.save(tableName, record);
	}
	
	static boolean update(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		return pro.update(config, conn, tableName, primaryKey, record);
	}
	
	/**
	 * Update Record.
	 * @param tableName the table name of the Record save to
	 * @param primaryKey the primary key of the table
	 * @param record the Record object
	 * @param true if update succeed otherwise false
	 */
	public static boolean update(String tableName, String primaryKey, Record record) {
		return pro.update(tableName, primaryKey, record);
	}
	
	/**
	 * Update Record. The primary key of the table is: "id".
	 * @see #update(String, String, Record)
	 */
	public static boolean update(String tableName, Record record) {
		return pro.update(tableName, record);
	}
	
	/**
	 * @see #execute(String, ICallback)
	 */
	public static Object execute(ICallback callback) {
		return pro.execute(callback);
	}
	
	/**
	 * Execute callback. It is useful when all the API can not satisfy your requirement.
	 * @param config the Config object
	 * @param callback the ICallback interface
	 */
	static Object execute(Config config, ICallback callback) {
		return pro.execute(config, callback);
	}
	
	/**
	 * Execute transaction.
	 * @param config the Config object
	 * @param transactionLevel the transaction level
	 * @param atom the atom operation
	 * @return true if transaction executing succeed otherwise false
	 */
	static boolean tx(Config config, int transactionLevel, IAtom atom) {
		return pro.tx(config, transactionLevel, atom);
	}
	
	public static boolean tx(int transactionLevel, IAtom atom) {
		return pro.tx(transactionLevel, atom);
	}
	
	/**
	 * Execute transaction with default transaction level.
	 * @see #tx(int, IAtom)
	 */
	public static boolean tx(IAtom atom) {
		return pro.tx(atom);
	}
	
	/**
	 * Find Record by cache.
	 * @see #find(String, Object...)
	 * @param cacheName the cache name
	 * @param key the key used to get date from cache
	 * @return the list of Record
	 */
	public static List<Record> findByCache(String cacheName, Object key, String sql, Object... paras) {
		return pro.findByCache(cacheName, key, sql, paras);
	}
	
	/**
	 * @see #findByCache(String, Object, String, Object...)
	 */
	public static List<Record> findByCache(String cacheName, Object key, String sql) {
		return pro.findByCache(cacheName, key, sql);
	}
	
	/**
	 * Paginate by cache.
	 * @see #paginate(int, int, String, String, Object...)
	 * @return Page
	 */
	public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		return pro.paginateByCache(cacheName, key, pageNumber, pageSize, select, sqlExceptSelect, paras);
	}
	
	/**
	 * @see #paginateByCache(String, Object, int, int, String, String, Object...)
	 */
	public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return pro.paginateByCache(cacheName, key, pageNumber, pageSize, select, sqlExceptSelect);
	}
	
	/**
	 * @see #batch(String, String, Object[][], int)
     */
    public static int[] batch(String sql, Object[][] paras, int batchSize) {
    	return pro.batch(sql, paras, batchSize);
    }
	
	/**
	 * @see #batch(String, String, String, List, int)
     */
	public static int[] batch(String sql, String columns, List modelOrRecordList, int batchSize) {
		return pro.batch(sql, columns, modelOrRecordList, batchSize);
	}
	
	/**
	 * @see #batch(String, List, int)
     */
    public static int[] batch(List<String> sqlList, int batchSize) {
    	return pro.batch(sqlList, batchSize);
    }
}



