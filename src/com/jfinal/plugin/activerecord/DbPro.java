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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jfinal.plugin.activerecord.cache.ICache;
import static com.jfinal.plugin.activerecord.DbKit.NULL_PARA_ARRAY;

/**
 * DbPro. Professional database query and update tool.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DbPro {
	
	private final Config config;
	private static final Map<String, DbPro> map = new HashMap<String, DbPro>();
	
	public DbPro() {
		if (DbKit.config == null)
			throw new RuntimeException("The main config is null, initialize ActiveRecordPlugin first");
		this.config = DbKit.config;
	}
	
	public DbPro(String configName) {
		this.config = DbKit.getConfig(configName);
		if (this.config == null)
			throw new IllegalArgumentException("Config not found by configName: " + configName);
	}
	
	public static DbPro use(String configName) {
		DbPro result = map.get(configName);
		if (result == null) {
			result = new DbPro(configName);
			map.put(configName, result);
		}
		return result;
	}
	
	public static DbPro use() {
		return use(DbKit.config.name);
	}
	
	<T> List<T> query(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		List result = new ArrayList();
		PreparedStatement pst = conn.prepareStatement(sql);
		config.dialect.fillStatement(pst, paras);
		ResultSet rs = pst.executeQuery();
		int colAmount = rs.getMetaData().getColumnCount();
		if (colAmount > 1) {
			while (rs.next()) {
				Object[] temp = new Object[colAmount];
				for (int i=0; i<colAmount; i++) {
					temp[i] = rs.getObject(i + 1);
				}
				result.add(temp);
			}
		}
		else if(colAmount == 1) {
			while (rs.next()) {
				result.add(rs.getObject(1));
			}
		}
		DbKit.closeQuietly(rs, pst);
		return result;
	}
	
	/**
	 * @see #query(String, String, Object...)
	 */
	public <T> List<T> query(String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			return query(config, conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * @see #query(String, Object...)
	 * @param sql an SQL statement
	 */
	public <T> List<T> query(String sql) {		// return  List<object[]> or List<object>
		return query(sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Execute sql query and return the first result. I recommend add "limit 1" in your sql.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return Object[] if your sql has select more than one column,
	 * 			and it return Object if your sql has select only one column.
	 */
	public <T> T queryFirst(String sql, Object... paras) {
		List<T> result = query(sql, paras);
		return (result.size() > 0 ? result.get(0) : null);
	}
	
	/**
	 * @see #queryFirst(String, Object...)
	 * @param sql an SQL statement
	 */
	public <T> T queryFirst(String sql) {
		// return queryFirst(sql, NULL_PARA_ARRAY);
		List<T> result = query(sql, NULL_PARA_ARRAY);
		return (result.size() > 0 ? result.get(0) : null);
	}
	
	// 26 queryXxx method below -----------------------------------------------
	/**
	 * Execute sql query just return one column.
	 * @param <T> the type of the column that in your sql's select statement
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return List<T>
	 */
	public <T> T queryColumn(String sql, Object... paras) {
		List<T> result = query(sql, paras);
		if (result.size() > 0) {
			T temp = result.get(0);
			if (temp instanceof Object[])
				throw new ActiveRecordException("Only ONE COLUMN can be queried.");
			return temp;
		}
		return null;
	}
	
	public <T> T queryColumn(String sql) {
		return (T)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public String queryStr(String sql, Object... paras) {
		return (String)queryColumn(sql, paras);
	}
	
	public String queryStr(String sql) {
		return (String)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public Integer queryInt(String sql, Object... paras) {
		return (Integer)queryColumn(sql, paras);
	}
	
	public Integer queryInt(String sql) {
		return (Integer)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public Long queryLong(String sql, Object... paras) {
		return (Long)queryColumn(sql, paras);
	}
	
	public Long queryLong(String sql) {
		return (Long)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public Double queryDouble(String sql, Object... paras) {
		return (Double)queryColumn(sql, paras);
	}
	
	public Double queryDouble(String sql) {
		return (Double)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public Float queryFloat(String sql, Object... paras) {
		return (Float)queryColumn(sql, paras);
	}
	
	public Float queryFloat(String sql) {
		return (Float)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public java.math.BigDecimal queryBigDecimal(String sql, Object... paras) {
		return (java.math.BigDecimal)queryColumn(sql, paras);
	}
	
	public java.math.BigDecimal queryBigDecimal(String sql) {
		return (java.math.BigDecimal)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public byte[] queryBytes(String sql, Object... paras) {
		return (byte[])queryColumn(sql, paras);
	}
	
	public byte[] queryBytes(String sql) {
		return (byte[])queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public java.util.Date queryDate(String sql, Object... paras) {
		return (java.util.Date)queryColumn(sql, paras);
	}
	
	public java.util.Date queryDate(String sql) {
		return (java.util.Date)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public java.sql.Time queryTime(String sql, Object... paras) {
		return (java.sql.Time)queryColumn(sql, paras);
	}
	
	public java.sql.Time queryTime(String sql) {
		return (java.sql.Time)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public java.sql.Timestamp queryTimestamp(String sql, Object... paras) {
		return (java.sql.Timestamp)queryColumn(sql, paras);
	}
	
	public java.sql.Timestamp queryTimestamp(String sql) {
		return (java.sql.Timestamp)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public Boolean queryBoolean(String sql, Object... paras) {
		return (Boolean)queryColumn(sql, paras);
	}
	
	public Boolean queryBoolean(String sql) {
		return (Boolean)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public Number queryNumber(String sql, Object... paras) {
		return (Number)queryColumn(sql, paras);
	}
	
	public Number queryNumber(String sql) {
		return (Number)queryColumn(sql, NULL_PARA_ARRAY);
	}
	// 26 queryXxx method under -----------------------------------------------
	
	/**
	 * Execute sql update
	 */
	int update(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		PreparedStatement pst = conn.prepareStatement(sql);
		config.dialect.fillStatement(pst, paras);
		int result = pst.executeUpdate();
		DbKit.closeQuietly(pst);
		return result;
	}
	
	/**
	 * Execute update, insert or delete sql statement.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return either the row count for <code>INSERT</code>, <code>UPDATE</code>,
     *         or <code>DELETE</code> statements, or 0 for SQL statements 
     *         that return nothing
	 */
	public int update(String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			return update(config, conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * @see #update(String, Object...)
	 * @param sql an SQL statement
	 */
	public int update(String sql) {
		return update(sql, NULL_PARA_ARRAY);
	}
	
	List<Record> find(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		PreparedStatement pst = conn.prepareStatement(sql);
		config.dialect.fillStatement(pst, paras);
		ResultSet rs = pst.executeQuery();
		List<Record> result = RecordBuilder.build(config, rs);
		DbKit.closeQuietly(rs, pst);
		return result;
	}
	
	/**
	 * @see #find(String, String, Object...)
	 */
	public List<Record> find(String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			return find(config, conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * @see #find(String, String, Object...)
	 * @param sql the sql statement
	 */
	public List<Record> find(String sql) {
		return find(sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Find first record. I recommend add "limit 1" in your sql.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return the Record object
	 */
	public Record findFirst(String sql, Object... paras) {
		List<Record> result = find(sql, paras);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * @see #findFirst(String, Object...)
	 * @param sql an SQL statement
	 */
	public Record findFirst(String sql) {
		List<Record> result = find(sql, NULL_PARA_ARRAY);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Find record by id with default primary key.
	 * <pre>
	 * Example:
	 * Record user = DbPro.use().findById("user", 15);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 */
	public Record findById(String tableName, Object idValue) {
		return findById(tableName, config.dialect.getDefaultPrimaryKey(), idValue);
	}
	
	/**
	 * Find record by id.
	 * <pre>
	 * Example:
	 * Record user = DbPro.use().findById("user", "user_id", 123);
	 * Record userRole = DbPro.use().findById("user_role", "user_id, role_id", 123, 456);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param idValue the id value of the record, it can be composite id values
	 */
	public Record findById(String tableName, String primaryKey, Object... idValue) {
		String[] pKeys = primaryKey.split(",");
		if (pKeys.length != idValue.length)
			throw new IllegalArgumentException("primary key number must equals id value number");
		
		String sql = config.dialect.forDbFindById(tableName, pKeys);
		List<Record> result = find(sql, idValue);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Delete record by id with default primary key.
	 * <pre>
	 * Example:
	 * DbPro.use().deleteById("user", 15);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 * @return true if delete succeed otherwise false
	 */
	public boolean deleteById(String tableName, Object idValue) {
		return deleteById(tableName, config.dialect.getDefaultPrimaryKey(), idValue);
	}
	
	/**
	 * Delete record by id.
	 * <pre>
	 * Example:
	 * DbPro.use().deleteById("user", "user_id", 15);
	 * DbPro.use().deleteById("user_role", "user_id, role_id", 123, 456);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param idValue the id value of the record, it can be composite id values
	 * @return true if delete succeed otherwise false
	 */
	public boolean deleteById(String tableName, String primaryKey, Object... idValue) {
		String[] pKeys = primaryKey.split(",");
		if (pKeys.length != idValue.length)
			throw new IllegalArgumentException("primary key number must equals id value number");
		
		String sql = config.dialect.forDbDeleteById(tableName, pKeys);
		return update(sql, idValue) >= 1;
	}
	
	/**
	 * Delete record.
	 * <pre>
	 * Example:
	 * boolean succeed = DbPro.use().delete("user", "id", user);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param record the record
	 * @return true if delete succeed otherwise false
	 */
	public boolean delete(String tableName, String primaryKey, Record record) {
		return deleteById(tableName, primaryKey, record.get(primaryKey));
	}
	
	/**
	 * <pre>
	 * Example:
	 * boolean succeed = DbPro.use().delete("user", user);
	 * </pre>
	 * @see #delete(String, String, Record)
	 */
	public boolean delete(String tableName, Record record) {
		String defaultPrimaryKey = config.dialect.getDefaultPrimaryKey();
		return deleteById(tableName, defaultPrimaryKey, record.get(defaultPrimaryKey));
	}
	
	Page<Record> paginate(Config config, Connection conn, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) throws SQLException {
		if (pageNumber < 1 || pageSize < 1)
			throw new ActiveRecordException("pageNumber and pageSize must be more than 0");
		
		if (config.dialect.isTakeOverDbPaginate())
			return config.dialect.takeOverDbPaginate(conn, pageNumber, pageSize, select, sqlExceptSelect, paras);
		
		long totalRow = 0;
		int totalPage = 0;
		List result = query(config, conn, "select count(*) " + DbKit.replaceFormatSqlOrderBy(sqlExceptSelect), paras);
		int size = result.size();
		if (size == 1)
			totalRow = ((Number)result.get(0)).longValue();
		else if (size > 1)
			totalRow = result.size();
		else
			return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, 0, 0);
		
		totalPage = (int) (totalRow / pageSize);
		if (totalRow % pageSize != 0) {
			totalPage++;
		}
		
		if (pageNumber > totalPage)
			return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, totalPage, (int)totalRow);
		
		// --------
		StringBuilder sql = new StringBuilder();
		config.dialect.forPaginate(sql, pageNumber, pageSize, select, sqlExceptSelect);
		List<Record> list = find(config, conn, sql.toString(), paras);
		return new Page<Record>(list, pageNumber, pageSize, totalPage, (int)totalRow);
	}
	
	/**
	 * @see #paginate(String, int, int, String, String, Object...)
	 */
	public Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			return paginate(config, conn, pageNumber, pageSize, select, sqlExceptSelect, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * @see #paginate(String, int, int, String, String, Object...)
	 */
	public Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return paginate(pageNumber, pageSize, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	boolean save(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		String[] pKeys = primaryKey.split(",");
		List<Object> paras = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		config.dialect.forDbSave(sql, paras, tableName, pKeys, record);
		
		PreparedStatement pst;
		if (config.dialect.isOracle())
			pst = conn.prepareStatement(sql.toString(), pKeys);
		else
			pst = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			
		config.dialect.fillStatement(pst, paras);
		int result = pst.executeUpdate();
		getGeneratedKey(pst, record, pKeys);
		DbKit.closeQuietly(pst);
		return result >= 1;
	}
	
	/**
	 * Get id after save record.
	 */
	private void getGeneratedKey(PreparedStatement pst, Record record, String[] pKeys) throws SQLException {
		ResultSet rs = pst.getGeneratedKeys();
		for (String pKey : pKeys)
			if (record.get(pKey) == null || config.dialect.isOracle())
				if (rs.next())
					record.set(pKey, rs.getObject(1));
		rs.close();
	}
	
	/**
	 * Save record.
	 * <pre>
	 * Example:
	 * Record userRole = new Record().set("user_id", 123).set("role_id", 456);
	 * DbPro.use().save("user_role", "user_id, role_id", userRole);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param record the record will be saved
	 * @param true if save succeed otherwise false
	 */
	public boolean save(String tableName, String primaryKey, Record record) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			return save(config, conn, tableName, primaryKey, record);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * @see #save(String, String, Record)
	 */
	public boolean save(String tableName, Record record) {
		return save(tableName, config.dialect.getDefaultPrimaryKey(), record);
	}
	
	boolean update(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		String[] pKeys = primaryKey.split(",");
		Object[] ids = new Object[pKeys.length];
		
		for (int i=0; i<pKeys.length; i++) {
			ids[i] = record.get(pKeys[i].trim());	// .trim() is important!
			if (ids[i] == null)
				throw new ActiveRecordException("You can't update record without Primary Key, " + pKeys[i] + " can not be null.");
		}
		
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		config.dialect.forDbUpdate(tableName, pKeys, ids, record, sql, paras);
		
		if (paras.size() <= 1) {	// Needn't update
			return false;
		}
		
		return update(config, conn, sql.toString(), paras.toArray()) >= 1;
	}
	
	/**
	 * Update Record.
	 * <pre>
	 * Example:
	 * DbPro.use().update("user_role", "user_id, role_id", record);
	 * </pre>
	 * @param tableName the table name of the Record save to
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param record the Record object
	 * @param true if update succeed otherwise false
	 */
	public boolean update(String tableName, String primaryKey, Record record) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			return update(config, conn, tableName, primaryKey, record);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * Update record with default primary key.
	 * <pre>
	 * Example:
	 * DbPro.use().update("user", record);
	 * </pre>
	 * @see #update(String, String, Record)
	 */
	public boolean update(String tableName, Record record) {
		return update(tableName, config.dialect.getDefaultPrimaryKey(), record);
	}
	
	/**
	 * @see #execute(String, ICallback)
	 */
	public Object execute(ICallback callback) {
		return execute(config, callback);
	}
	
	/**
	 * Execute callback. It is useful when all the API can not satisfy your requirement.
	 * @param config the Config object
	 * @param callback the ICallback interface
	 */
	Object execute(Config config, ICallback callback) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			return callback.call(conn);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * Execute transaction.
	 * @param config the Config object
	 * @param transactionLevel the transaction level
	 * @param atom the atom operation
	 * @return true if transaction executing succeed otherwise false
	 */
	boolean tx(Config config, int transactionLevel, IAtom atom) {
		Connection conn = config.getThreadLocalConnection();
		if (conn != null) {	// Nested transaction support
			try {
				if (conn.getTransactionIsolation() < transactionLevel)
					conn.setTransactionIsolation(transactionLevel);
				boolean result = atom.run();
				if (result)
					return true;
				throw new NestedTransactionHelpException("Notice the outer transaction that the nested transaction return false");	// important:can not return false
			}
			catch (SQLException e) {
				throw new ActiveRecordException(e);
			}
		}
		
		Boolean autoCommit = null;
		try {
			conn = config.getConnection();
			autoCommit = conn.getAutoCommit();
			config.setThreadLocalConnection(conn);
			conn.setTransactionIsolation(transactionLevel);
			conn.setAutoCommit(false);
			boolean result = atom.run();
			if (result)
				conn.commit();
			else
				conn.rollback();
			return result;
		} catch (NestedTransactionHelpException e) {
			if (conn != null) try {conn.rollback();} catch (Exception e1) {e1.printStackTrace();}
			return false;
		} catch (Throwable t) {
			if (conn != null) try {conn.rollback();} catch (Exception e1) {e1.printStackTrace();}
			throw t instanceof RuntimeException ? (RuntimeException)t : new ActiveRecordException(t);
		} finally {
			try {
				if (conn != null) {
					if (autoCommit != null)
						conn.setAutoCommit(autoCommit);
					conn.close();
				}
			} catch (Throwable t) {
				t.printStackTrace();	// can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
			} finally {
				config.removeThreadLocalConnection();	// prevent memory leak
			}
		}
	}
	
	public boolean tx(int transactionLevel, IAtom atom) {
		return tx(config, transactionLevel, atom);
	}
	
	/**
	 * Execute transaction with default transaction level.
	 * @see #tx(int, IAtom)
	 */
	public boolean tx(IAtom atom) {
		return tx(config, config.getTransactionLevel(), atom);
	}
	
	/**
	 * Find Record by cache.
	 * @see #find(String, Object...)
	 * @param cacheName the cache name
	 * @param key the key used to get date from cache
	 * @return the list of Record
	 */
	public List<Record> findByCache(String cacheName, Object key, String sql, Object... paras) {
		ICache cache = config.getCache();
		List<Record> result = cache.get(cacheName, key);
		if (result == null) {
			result = find(sql, paras);
			cache.put(cacheName, key, result);
		}
		return result;
	}
	
	/**
	 * @see #findByCache(String, Object, String, Object...)
	 */
	public List<Record> findByCache(String cacheName, Object key, String sql) {
		return findByCache(cacheName, key, sql, NULL_PARA_ARRAY);
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
	public Record findFirstByCache(String cacheName, Object key, String sql, Object... paras) {
		ICache cache = config.getCache();
		Record result = cache.get(cacheName, key);
		if (result == null) {
			result = findFirst(sql, paras);
			cache.put(cacheName, key, result);
		}
		return result;
	}
	
	/**
	 * @see #findFirstByCache(String, Object, String, Object...)
	 */
	public Record findFirstByCache(String cacheName, Object key, String sql) {
		return findFirstByCache(cacheName, key, sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Paginate by cache.
	 * @see #paginate(int, int, String, String, Object...)
	 * @return Page
	 */
	public Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		ICache cache = config.getCache();
		Page<Record> result = cache.get(cacheName, key);
		if (result == null) {
			result = paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
			cache.put(cacheName, key, result);
		}
		return result;
	}
	
	/**
	 * @see #paginateByCache(String, Object, int, int, String, String, Object...)
	 */
	public Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return paginateByCache(cacheName, key, pageNumber, pageSize, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	private int[] batch(Config config, Connection conn, String sql, Object[][] paras, int batchSize) throws SQLException {
		if (paras == null || paras.length == 0)
			throw new IllegalArgumentException("The paras array length must more than 0.");
		if (batchSize < 1)
			throw new IllegalArgumentException("The batchSize must more than 0.");
		
		boolean isInTransaction = config.isInTransaction();
		int counter = 0;
		int pointer = 0;
		int[] result = new int[paras.length];
		PreparedStatement pst = conn.prepareStatement(sql);
		for (int i=0; i<paras.length; i++) {
			for (int j=0; j<paras[i].length; j++) {
				Object value = paras[i][j];
				if (config.dialect.isOracle()) {
					if (value instanceof java.sql.Date)
						pst.setDate(j + 1, (java.sql.Date)value);
					else if (value instanceof java.sql.Timestamp)
						pst.setTimestamp(j + 1, (java.sql.Timestamp)value);
					else
						pst.setObject(j + 1, value);
				}
				else
					pst.setObject(j + 1, value);
			}
			pst.addBatch();
			if (++counter >= batchSize) {
				counter = 0;
				int[] r = pst.executeBatch();
				if (isInTransaction == false)
					conn.commit();
				for (int k=0; k<r.length; k++)
					result[pointer++] = r[k];
			}
		}
		int[] r = pst.executeBatch();
		if (isInTransaction == false)
			conn.commit();
		for (int k=0; k<r.length; k++)
			result[pointer++] = r[k];
		DbKit.closeQuietly(pst);
		return result;
	}
	
    /**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <pre>
     * Example:
     * String sql = "insert into user(name, cash) values(?, ?)";
     * int[] result = DbPro.use().batch("myConfig", sql, new Object[][]{{"James", 888}, {"zhanjin", 888}});
     * </pre>
     * @param sql The SQL to execute.
     * @param paras An array of query replacement parameters.  Each row in this array is one set of batch replacement values.
     * @return The number of rows updated per statement
     */
	public int[] batch(String sql, Object[][] paras, int batchSize) {
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = config.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			return batch(config, conn, sql, paras, batchSize);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			if (autoCommit != null)
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {e.printStackTrace();}
			config.close(conn);
		}
	}
	
	private int[] batch(Config config, Connection conn, String sql, String columns, List list, int batchSize) throws SQLException {
		if (list == null || list.size() == 0)
			return new int[0];
		Object element = list.get(0);
		if (!(element instanceof Record) && !(element instanceof Model))
			throw new IllegalArgumentException("The element in list must be Model or Record.");
		if (batchSize < 1)
			throw new IllegalArgumentException("The batchSize must more than 0.");
		boolean isModel = element instanceof Model;
		
		String[] columnArray = columns.split(",");
		for (int i=0; i<columnArray.length; i++)
			columnArray[i] = columnArray[i].trim();
		
		boolean isInTransaction = config.isInTransaction();
		int counter = 0;
		int pointer = 0;
		int size = list.size();
		int[] result = new int[size];
		PreparedStatement pst = conn.prepareStatement(sql);
		for (int i=0; i<size; i++) {
			Map map = isModel ? ((Model)list.get(i)).getAttrs() : ((Record)list.get(i)).getColumns();
			for (int j=0; j<columnArray.length; j++) {
				Object value = map.get(columnArray[j]);
				if (config.dialect.isOracle()) {
					if (value instanceof java.sql.Date)
						pst.setDate(j + 1, (java.sql.Date)value);
					else if (value instanceof java.sql.Timestamp)
						pst.setTimestamp(j + 1, (java.sql.Timestamp)value);
					else
						pst.setObject(j + 1, value);
				}
				else
					pst.setObject(j + 1, value);
			}
			pst.addBatch();
			if (++counter >= batchSize) {
				counter = 0;
				int[] r = pst.executeBatch();
				if (isInTransaction == false)
					conn.commit();
				for (int k=0; k<r.length; k++)
					result[pointer++] = r[k];
			}
		}
		int[] r = pst.executeBatch();
		if (isInTransaction == false)
			conn.commit();
		for (int k=0; k<r.length; k++)
			result[pointer++] = r[k];
		DbKit.closeQuietly(pst);
		return result;
	}
	
	/**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <pre>
     * Example:
     * String sql = "insert into user(name, cash) values(?, ?)";
     * int[] result = DbPro.use().batch("myConfig", sql, "name, cash", modelList, 500);
     * </pre>
	 * @param sql The SQL to execute.
	 * @param columns the columns need be processed by sql.
	 * @param modelOrRecordList model or record object list.
	 * @param batchSize batch size.
	 * @return The number of rows updated per statement
	 */
	public int[] batch(String sql, String columns, List modelOrRecordList, int batchSize) {
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = config.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			return batch(config, conn, sql, columns, modelOrRecordList, batchSize);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			if (autoCommit != null)
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {e.printStackTrace();}
			config.close(conn);
		}
	}
	
	private int[] batch(Config config, Connection conn, List<String> sqlList, int batchSize) throws SQLException {
		if (sqlList == null || sqlList.size() == 0)
			throw new IllegalArgumentException("The sqlList length must more than 0.");
		if (batchSize < 1)
			throw new IllegalArgumentException("The batchSize must more than 0.");
		
		boolean isInTransaction = config.isInTransaction();
		int counter = 0;
		int pointer = 0;
		int size = sqlList.size();
		int[] result = new int[size];
		Statement st = conn.createStatement();
		for (int i=0; i<size; i++) {
			st.addBatch(sqlList.get(i));
			if (++counter >= batchSize) {
				counter = 0;
				int[] r = st.executeBatch();
				if (isInTransaction == false)
					conn.commit();
				for (int k=0; k<r.length; k++)
					result[pointer++] = r[k];
			}
		}
		int[] r = st.executeBatch();
		if (isInTransaction == false)
			conn.commit();
		for (int k=0; k<r.length; k++)
			result[pointer++] = r[k];
		DbKit.closeQuietly(st);
		return result;
	}
	
    /**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <pre>
     * Example:
     * int[] result = DbPro.use().batch("myConfig", sqlList, 500);
     * </pre>
	 * @param sqlList The SQL list to execute.
	 * @param batchSize batch size.
	 * @return The number of rows updated per statement
	 */
    public int[] batch(List<String> sqlList, int batchSize) {
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = config.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			return batch(config, conn, sqlList, batchSize);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			if (autoCommit != null)
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {e.printStackTrace();}
			config.close(conn);
		}
    }
    
    /**
     * for DbKit.removeConfig(configName)
     */
    static void removeDbProWithConfig(String configName) {
    	map.remove(configName);
    }
}



