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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import com.jfinal.plugin.activerecord.cache.ICache;
import static com.jfinal.plugin.activerecord.DbKit.NULL_PARA_ARRAY;

/**
 * Db. Powerful database query and update tool box.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Db {
	
	static <T> List<T> query(Connection conn, String sql, Object... paras) throws SQLException {
		List result = new ArrayList();
		PreparedStatement pst = conn.prepareStatement(sql);
		
		for (int i=0; i<paras.length; i++) {
			pst.setObject(i + 1, paras[i]);
		}
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
	 * @see #query(DataSource, String, Object...)
	 */
	public static <T> List<T> query(String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = DbKit.getConnection();
			return query(conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.close(conn);
		}
	}
	
	/**
	 * Execute sql query. The result can not convert to Record.
	 * @param dataSource the DataSource for this query
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return List&lt;Object[]&gt; if your sql has select more than one column,
	 * 			and it return List&lt;Object&gt; if your sql has select only one column.
	 */
	public static <T> List<T> query(DataSource dataSource, String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			return query(conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.closeIgnoreThreadLocal(conn);
		}
	}
	
	/**
	 * @see #query(DataSource, String, Object...)
	 * @param sql an SQL statement
	 */
	public static <T> List<T> query(DataSource dataSource, String sql) {
		return query(dataSource, sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * @see #query(DataSource, String, Object...)
	 * @param sql an SQL statement
	 */
	public static <T> List<T> query(String sql) {		// return  List<object[]> or List<object>
		return query(sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Execute sql query and return the first result. I recommend add "limit 1" in your sql.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return Object[] if your sql has select more than one column,
	 * 			and it return Object if your sql has select only one column.
	 */
	public static <T> T queryFirst(String sql, Object... paras) {
		List<T> result = query(sql, paras);
		return (T) (result.size() > 0 ? result.get(0) : null);
	}
	
	/**
	 * @see #queryFirst(String, Object...)
	 * @param sql an SQL statement
	 */
	public static <T> T queryFirst(String sql) {
		// return queryFirst(sql, NULL_PARA_ARRAY);
		List<T> result = query(sql, NULL_PARA_ARRAY);
		return (T) (result.size() > 0 ? result.get(0) : null);
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
		List<T> result = query(sql, paras);
		if (result.size() > 0) {
			T temp = result.get(0);
			if (temp instanceof Object[])
				throw new ActiveRecordException("Only ONE COLUMN can be queried.");
			return temp;
		}
		return null;
	}
	
	public static <T> T queryColumn(String sql) {
		return queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static String queryStr(String sql, Object... paras) {
		return (String)queryColumn(sql, paras);
	}
	
	public static String queryStr(String sql) {
		return (String)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static Integer queryInt(String sql, Object... paras) {
		return (Integer)queryColumn(sql, paras);
	}
	
	public static Integer queryInt(String sql) {
		return (Integer)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static Long queryLong(String sql, Object... paras) {
		return (Long)queryColumn(sql, paras);
	}
	
	public static Long queryLong(String sql) {
		return (Long)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static Double queryDouble(String sql, Object... paras) {
		return (Double)queryColumn(sql, paras);
	}
	
	public static Double queryDouble(String sql) {
		return (Double)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static Float queryFloat(String sql, Object... paras) {
		return (Float)queryColumn(sql, paras);
	}
	
	public static Float queryFloat(String sql) {
		return (Float)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static java.math.BigDecimal queryBigDecimal(String sql, Object... paras) {
		return (java.math.BigDecimal)queryColumn(sql, paras);
	}
	
	public static java.math.BigDecimal queryBigDecimal(String sql) {
		return (java.math.BigDecimal)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static byte[] queryBytes(String sql, Object... paras) {
		return (byte[])queryColumn(sql, paras);
	}
	
	public static byte[] queryBytes(String sql) {
		return (byte[])queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static java.sql.Date queryDate(String sql, Object... paras) {
		return (java.sql.Date)queryColumn(sql, paras);
	}
	
	public static java.sql.Date queryDate(String sql) {
		return (java.sql.Date)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static java.sql.Time queryTime(String sql, Object... paras) {
		return (java.sql.Time)queryColumn(sql, paras);
	}
	
	public static java.sql.Time queryTime(String sql) {
		return (java.sql.Time)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static java.sql.Timestamp queryTimestamp(String sql, Object... paras) {
		return (java.sql.Timestamp)queryColumn(sql, paras);
	}
	
	public static java.sql.Timestamp queryTimestamp(String sql) {
		return (java.sql.Timestamp)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static Boolean queryBoolean(String sql, Object... paras) {
		return (Boolean)queryColumn(sql, paras);
	}
	
	public static Boolean queryBoolean(String sql) {
		return (Boolean)queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public static Number queryNumber(String sql, Object... paras) {
		return (Number)queryColumn(sql, paras);
	}
	
	public static Number queryNumber(String sql) {
		return (Number)queryColumn(sql, NULL_PARA_ARRAY);
	}
	// 26 queryXxx method under -----------------------------------------------
	
	/**
	 * Execute sql update
	 */
	static int update(Connection conn, String sql, Object... paras) throws SQLException {
		PreparedStatement pst = conn.prepareStatement(sql);
		for (int i=0; i<paras.length; i++) {
			pst.setObject(i + 1, paras[i]);
		}
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
	public static int update(String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = DbKit.getConnection();
			return update(conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.close(conn);
		}
	}
	
	/**
	 * @see #update(String, Object...)
	 * @param dataSource the DataSource for this query
	 */
	public static int update(DataSource dataSource, String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			return update(conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.closeIgnoreThreadLocal(conn);
		}
	}
	
	/**
	 * @see #update(DataSource, String, Object...)
	 * @param sql an SQL statement
	 */
	public static int update(DataSource dataSource, String sql) {
		return update(dataSource, sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * @see #update(DataSource, String, Object...)
	 * @param sql an SQL statement
	 */
	public static int update(String sql) {
		return update(sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Get id after insert method getGeneratedKey().
	 */
	private static Object getGeneratedKey(PreparedStatement pst) throws SQLException {
		ResultSet rs = pst.getGeneratedKeys();
		Object id = null;
		if (rs.next())
			 id = rs.getObject(1);
		rs.close();
		return id;
	}
	
	static List<Record> find(Connection conn, String sql, Object... paras) throws SQLException {
		PreparedStatement pst = conn.prepareStatement(sql);
		for (int i=0; i<paras.length; i++) {
			pst.setObject(i + 1, paras[i]);
		}
		ResultSet rs = pst.executeQuery();
		List<Record> result = RecordBuilder.build(rs);
		DbKit.closeQuietly(rs, pst);
		return result;
	}
	
	/**
	 * @see #find(DataSource, String, Object...)
	 */
	public static List<Record> find(String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = DbKit.getConnection();
			return find(conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.close(conn);
		}
	}
	
	/**
	 * @see #find(DataSource, String, Object...)
	 * @param sql the sql statement
	 */
	public static List<Record> find(String sql) {
		return find(sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Find Record.
	 * @param dataSource the DataSource for this query
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return the list of Record
	 */
	public static List<Record> find(DataSource dataSource, String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			return find(conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.closeIgnoreThreadLocal(conn);
		}
	}
	
	/**
	 * @see #find(DataSource, String, Object...)
	 * @param sql the sql statement
	 */
	public static List<Record> find(DataSource dataSource, String sql) {
		return find(dataSource, sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Find first record. I recommend add "limit 1" in your sql.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return the Record object
	 */
	public static Record findFirst(String sql, Object... paras) {
		List<Record> result = find(sql, paras);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * @see #findFirst(String, Object...)
	 * @param sql an SQL statement
	 */
	public static Record findFirst(String sql) {
		List<Record> result = find(sql, NULL_PARA_ARRAY);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Find record by id.
	 * Example: Record user = Db.findById("user", 15);
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 */
	public static Record findById(String tableName, Object idValue) {
		return findById(tableName, "id", idValue, "*");
	}
	
	/**
	 * Find record by id. Fetch the specific columns only.
	 * Example: Record user = Db.findById("user", 15, "name, age");
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 * @param columns the specific columns separate with comma character ==> ","
	 */
	public static Record findById(String tableName, Number idValue, String columns) {
		return findById(tableName, "id", idValue, columns);
	}
	
	/**
	 * Find record by id.
	 * Example: Record user = Db.findById("user", "user_id", 15);
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table
	 * @param idValue the id value of the record
	 */
	public static Record findById(String tableName, String primaryKey, Number idValue) {
		return findById(tableName, primaryKey, idValue, "*");
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
		String sql = DbKit.dialect.forDbFindById(tableName, primaryKey, columns);
		List<Record> result = find(sql, idValue);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Delete record by id.
	 * Example: boolean succeed = Db.deleteById("user", 15);
	 * @param tableName the table name of the table
	 * @param id the id value of the record
	 * @return true if delete succeed otherwise false
	 */
	public static boolean deleteById(String tableName, Object id) {
		return deleteById(tableName, "id", id);
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
		if (id == null)
			throw new IllegalArgumentException("id can not be null");
		
		String sql = DbKit.dialect.forDbDeleteById(tableName, primaryKey);
		return update(sql, id) >= 1;
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
		return deleteById(tableName, primaryKey, record.get(primaryKey));
	}
	
	/**
	 * Example: boolean succeed = Db.delete("user", user);
	 * @see #delete(String, String, Record)
	 */
	public static boolean delete(String tableName, Record record) {
		return deleteById(tableName, "id", record.get("id"));
	}
	
	static Page<Record> paginate(Connection conn, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) throws SQLException {
		if (pageNumber < 1 || pageSize < 1)
			throw new ActiveRecordException("pageNumber and pageSize must be more than 0");
		
		if (DbKit.dialect.isTakeOverDbPaginate())
			return DbKit.dialect.takeOverDbPaginate(conn, pageNumber, pageSize, select, sqlExceptSelect, paras);
		
		long totalRow = 0;
		int totalPage = 0;
		List result = query(conn, "select count(*) " + DbKit.replaceFormatSqlOrderBy(sqlExceptSelect), paras);
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
		
		// --------
		StringBuilder sql = new StringBuilder();
		DbKit.dialect.forPaginate(sql, pageNumber, pageSize, select, sqlExceptSelect);
		List<Record> list = find(conn, sql.toString(), paras);
		return new Page<Record>(list, pageNumber, pageSize, totalPage, (int)totalRow);
	}
	
	/**
	 * @see #paginate(DataSource, int, int, String, String, Object...)
	 */
	public static Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Connection conn = null;
		try {
			conn = DbKit.getConnection();
			return paginate(conn, pageNumber, pageSize, select, sqlExceptSelect, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.close(conn);
		}
	}
	
	/**
	 * Paginate.
	 * @param dataSource the DataSource for this query
	 * @param pageNumber the page number
	 * @param pageSize the page size
	 * @param select the select part of the sql statement 
	 * @param sqlExceptSelect the sql statement excluded select part
	 * @param paras the parameters of sql
	 * @return Page
	 */
	public static Page<Record> paginate(DataSource dataSource, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			return paginate(conn, pageNumber, pageSize, select, sqlExceptSelect, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.closeIgnoreThreadLocal(conn);
		}
	}
	
	/**
	 * @see #paginate(DataSource, int, int, String, String, Object...)
	 */
	public static Page<Record> paginate(DataSource dataSource, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return paginate(dataSource, pageNumber, pageSize, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	/**
	 * @see #paginate(DataSource, int, int, String, String, Object...)
	 */
	public static Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return paginate(pageNumber, pageSize, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	static boolean save(Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		List<Object> paras = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		DbKit.dialect.forDbSave(sql, paras, tableName, record);
		
		PreparedStatement pst;
		boolean isSupportAutoIncrementKey = DbKit.dialect.isSupportAutoIncrementKey();
		if (isSupportAutoIncrementKey)
			pst = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
		else
			pst = conn.prepareStatement(sql.toString());
		for (int i=0, size=paras.size(); i<size; i++) {
			pst.setObject(i + 1, paras.get(i));
		}
		int result = pst.executeUpdate();
		if (isSupportAutoIncrementKey)
			record.set(primaryKey, getGeneratedKey(pst));
		DbKit.closeQuietly(pst);
		
		return result >= 1;
	}
	
	/**
	 * @see #save(DataSource, String, String, Record)
	 */
	public static boolean save(String tableName, String primaryKey, Record record) {
		Connection conn = null;
		try {
			conn = DbKit.getConnection();
			return save(conn, tableName, primaryKey, record);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.close(conn);
		}
	}
	
	/**
	 * @see #save(DataSource, String, String, Record)
	 */
	public static boolean save(String tableName, Record record) {
		return save(tableName, "id", record);
	}
	
	/**
	 * Save record.
	 * @param dataSource the DataSource for this query
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table
	 * @param record the record will be saved
	 * @param true if save succeed otherwise false
	 */
	public static boolean save(DataSource dataSource, String tableName, String primaryKey, Record record) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			return save(conn, tableName, primaryKey, record);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.closeIgnoreThreadLocal(conn);
		}
	}
	
	/**
	 * @see #save(DataSource, String, String, Record)
	 */
	public static boolean save(DataSource dataSource, String tableName, Record record) {
		return save(dataSource, tableName, "id", record);
	}
	
	static boolean update(Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		Object id = record.get(primaryKey);
		if (id == null)
			throw new ActiveRecordException("You can't update model whitout Primary Key.");
		
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		DbKit.dialect.forDbUpdate(tableName, primaryKey, id, record, sql, paras);
		
		if (paras.size() <= 1) {	// Needn't update
			return false;
		}
		
		return update(conn, sql.toString(), paras.toArray()) >= 1;
	}
	
	/**
	 * @see #update(DataSource, String, String, Record)
	 */
	public static boolean update(String tableName, String primaryKey, Record record) {
		Connection conn = null;
		try {
			conn = DbKit.getConnection();
			return update(conn, tableName, primaryKey, record);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.close(conn);
		}
	}
	
	/**
	 * Update Record. The primary key of the table is: "id".
	 * @see #update(DataSource, String, String, Record)
	 */
	public static boolean update(String tableName, Record record) {
		return update(tableName, "id", record);
	}
	
	/**
	 * Update Record.
	 * @param dataSource the DataSource for this query
	 * @param tableName the table name of the Record save to
	 * @param primaryKey the primary key of the table
	 * @param record the Record object
	 * @param true if update succeed otherwise false
	 */
	public static boolean update(DataSource dataSource, String tableName, String primaryKey, Record record) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			return update(conn, tableName, primaryKey, record);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.closeIgnoreThreadLocal(conn);
		}
	}
	
	/**
	 * Update Record. The primary key of the table is: "id".
	 * @see #update(DataSource, String, String, Record)
	 */
	public static boolean update(DataSource dataSource, String tableName, Record record) {
		return update(dataSource, tableName, "id", record);
	}
	
	/**
	 * @see #execute(DataSource, ICallback)
	 */
	public static void execute(ICallback callback) {
		execute(DbKit.getDataSource(), callback);
	}
	
	/**
	 * Execute callback. It is useful when all the API can not satisfy your requirement.
	 * @param dataSource the DataSource for this query
	 * @param callback the ICallback interface
	 */
	public static void execute(DataSource dataSource, ICallback callback) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			callback.run(conn);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			DbKit.closeIgnoreThreadLocal(conn);
		}
	}
	
	/**
	 * Execute transaction.
	 * @param transactionLevel the transaction level
	 * @param atom the atom operation
	 * @return true if transaction executing succeed otherwise false
	 */
	public  static boolean tx(int transactionLevel, IAtom atom) {
		if (DbKit.isExistsThreadLocalConnection())
			throw new ActiveRecordException("Nested transaction can not be supported. You can't execute transaction inside another transaction.");
		
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = DbKit.getDataSource().getConnection();
			autoCommit = conn.getAutoCommit();
			DbKit.setThreadLocalConnection(conn);
			conn.setTransactionIsolation(transactionLevel);
			conn.setAutoCommit(false);
			boolean result = atom.run();
			if (result)
				conn.commit();
			else
				conn.rollback();
			return result;
		} catch (Exception e) {
			if (conn != null)
				try {conn.rollback();} catch (Exception e1) {e1.printStackTrace();}
			throw new ActiveRecordException(e);
		} finally {
			try {
				if (conn != null) {
					if (autoCommit != null)
						conn.setAutoCommit(autoCommit);
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();	// can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
			} finally {
				DbKit.removeThreadLocalConnection();	// prevent memory leak
			}
		}
	}
	
	/**
	 * Execute transaction with default transaction level.
	 * @see #ex(int, IAtom)
	 */
	public static boolean tx(IAtom atom) {
		return tx(DbKit.getTransactionLevel(), atom);
	}
	
	/**
	 * Find Record by cache.
	 * @see #find(String, Object...)
	 * @param cacheName the cache name
	 * @param key the key used to get date from cache
	 * @return the list of Record
	 */
	public static List<Record> findByCache(String cacheName, Object key, String sql, Object... paras) {
		ICache cache = DbKit.getCache();
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
	public static List<Record> findByCache(String cacheName, Object key, String sql) {
		return findByCache(cacheName, key, sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Paginate by cache.
	 * @see #paginate(int, int, String, String, Object...)
	 * @return Page
	 */
	public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		ICache cache = DbKit.getCache();
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
	public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return paginateByCache(cacheName, key, pageNumber, pageSize, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	private static int[] batch(Connection conn, String sql, Object[][] paras, int batchSize) throws SQLException {
		if (paras == null || paras.length == 0)
			throw new IllegalArgumentException("The paras array length must more than 0.");
		if (batchSize < 1)
			throw new IllegalArgumentException("The batchSize must more than 0.");
		int counter = 0;
		int pointer = 0;
		int[] result = new int[paras.length];
		PreparedStatement pst = conn.prepareStatement(sql);
		for (int i=0; i<paras.length; i++) {
			for (int j=0; j<paras[i].length; j++) {
				pst.setObject(j + 1, paras[i][j]);
			}
			pst.addBatch();
			if (++counter >= batchSize) {
				counter = 0;
				int[] r = pst.executeBatch();
				conn.commit();
				for (int k=0; k<r.length; k++)
					result[pointer++] = r[k];
			}
		}
		int[] r = pst.executeBatch();
		conn.commit();
		for (int k=0; k<r.length; k++)
			result[pointer++] = r[k];
		DbKit.closeQuietly(pst);
		return result;
	}
	
	/**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <p>
     * Example:
     * <pre>
     * String sql = "insert into user(name, cash) values(?, ?)";
     * int[] result = Db.batch(sql, new Object[][]{{"James", 888}, {"zhanjin", 888}});
     * </pre>
     * @param sql The SQL to execute.
     * @param paras An array of query replacement parameters.  Each row in this array is one set of batch replacement values.
     * @return The number of rows updated per statement
     */
    public static int[] batch(String sql, Object[][] paras, int batchSize) {
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = DbKit.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			return batch(conn, sql, paras, batchSize);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			if (autoCommit != null)
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {e.printStackTrace();}
			DbKit.close(conn);
		}
    }
    
	/**
	 * @see #batch(String, Object[][])
	 * @param dataSource the DataSource for this query
     */
	public static int[] batch(DataSource dataSource, String sql, Object[][] paras, int batchSize) {
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = dataSource.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			return batch(conn, sql, paras, batchSize);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			if (autoCommit != null)
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {e.printStackTrace();}
			DbKit.closeIgnoreThreadLocal(conn);
		}
	}
	
	private static int[] batch(Connection conn, String sql, String columns, List list, int batchSize) throws SQLException {
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
		
		int counter = 0;
		int pointer = 0;
		int size = list.size();
		int[] result = new int[size];
		PreparedStatement pst = conn.prepareStatement(sql);
		for (int i=0; i<size; i++) {
			java.util.Map map = isModel ? ((Model)list.get(i)).getAttrs() : ((Record)list.get(i)).getColumns();
			for (int j=0; j<columnArray.length; j++) {
				pst.setObject(j + 1, map.get(columnArray[j]));
			}
			pst.addBatch();
			if (++counter >= batchSize) {
				counter = 0;
				int[] r = pst.executeBatch();
				conn.commit();
				for (int k=0; k<r.length; k++)
					result[pointer++] = r[k];
			}
		}
		int[] r = pst.executeBatch();
		conn.commit();
		for (int k=0; k<r.length; k++)
			result[pointer++] = r[k];
		DbKit.closeQuietly(pst);
		return result;
	}
	
	/**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <p>
     * Example:
     * <pre>
     * String sql = "insert into user(name, cash) values(?, ?)";
     * int[] result = Db.batch(sql, "name, cash", modelList, 500);
     * </pre>
	 * @param sql The SQL to execute.
	 * @param columns the columns need be processed by sql.
	 * @param modelOrRecordList model or record object list.
	 * @param batchSize batch size.
	 * @return The number of rows updated per statement
	 */
	public static int[] batch(String sql, String columns, List modelOrRecordList, int batchSize) {
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = DbKit.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			return batch(conn, sql, columns, modelOrRecordList, batchSize);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			if (autoCommit != null)
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {e.printStackTrace();}
			DbKit.close(conn);
		}
	}
	
	/**
	 * @see #batch(String, String, List, int)
	 * @param dataSource the DataSource for this query
     */
	public static int[] batch(DataSource dataSource, String sql, String columns, List modelOrRecordList, int batchSize) {
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = dataSource.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			return batch(conn, sql, columns, modelOrRecordList, batchSize);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			if (autoCommit != null)
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {e.printStackTrace();}
			DbKit.closeIgnoreThreadLocal(conn);
		}
	}
	
	private static int[] batch(Connection conn, List<String> sqlList, int batchSize) throws SQLException {
		if (sqlList == null || sqlList.size() == 0)
			throw new IllegalArgumentException("The sqlList length must more than 0.");
		if (batchSize < 1)
			throw new IllegalArgumentException("The batchSize must more than 0.");
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
				conn.commit();
				for (int k=0; k<r.length; k++)
					result[pointer++] = r[k];
			}
		}
		int[] r = st.executeBatch();
		conn.commit();
		for (int k=0; k<r.length; k++)
			result[pointer++] = r[k];
		DbKit.closeQuietly(st);
		return result;
	}
	
	/**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * Example:
     * <pre>
     * int[] result = Db.batch(sqlList, 500);
     * </pre>
	 * @param sqlList The SQL list to execute.
	 * @param batchSize batch size.
	 * @return The number of rows updated per statement
	 */
    public static int[] batch(List<String> sqlList, int batchSize) {
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = DbKit.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			return batch(conn, sqlList, batchSize);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			if (autoCommit != null)
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {e.printStackTrace();}
			DbKit.close(conn);
		}
    }
    
	/**
	 * @see #batch(List, int)
	 * @param dataSource the DataSource for this query
     */
    public static int[] batch(DataSource dataSource, List<String> sqlList, int batchSize) {
		Connection conn = null;
		Boolean autoCommit = null;
		try {
			conn = dataSource.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			return batch(conn, sqlList, batchSize);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			if (autoCommit != null)
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {e.printStackTrace();}
			DbKit.closeIgnoreThreadLocal(conn);
		}
    }
}


