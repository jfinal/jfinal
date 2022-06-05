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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.kit.TimeKit;
import com.jfinal.plugin.activerecord.cache.ICache;
import static com.jfinal.plugin.activerecord.DbKit.NULL_PARA_ARRAY;

/**
 * DbPro. Professional database query and update tool.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DbPro {
	
	protected final Config config;
	
	public DbPro() {
		if (DbKit.config == null) {
			throw new RuntimeException("The main config is null, initialize ActiveRecordPlugin first");
		}
		this.config = DbKit.config;
	}
	
	public DbPro(String configName) {
		this.config = DbKit.getConfig(configName);
		if (this.config == null) {
			throw new IllegalArgumentException("Config not found by configName: " + configName);
		}
	}
	
	public Config getConfig() {
		return config;
	}
	
	protected <T> List<T> query(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		List result = new ArrayList();
		try (PreparedStatement pst = conn.prepareStatement(sql)) {
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
			DbKit.close(rs);
			return result;
		}
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
	 * @return <T> T
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
		Object s = queryColumn(sql, paras);
		return s != null ? s.toString() : null;
	}
	
	public String queryStr(String sql) {
		return queryStr(sql, NULL_PARA_ARRAY);
	}
	
	public Integer queryInt(String sql, Object... paras) {
		Number n = queryNumber(sql, paras);
		return n != null ? n.intValue() : null;
	}
	
	public Integer queryInt(String sql) {
		return queryInt(sql, NULL_PARA_ARRAY);
	}
	
	public Long queryLong(String sql, Object... paras) {
		Number n = queryNumber(sql, paras);
		return n != null ? n.longValue() : null;
	}
	
	public Long queryLong(String sql) {
		return queryLong(sql, NULL_PARA_ARRAY);
	}
	
	public Double queryDouble(String sql, Object... paras) {
		Number n = queryNumber(sql, paras);
		return n != null ? n.doubleValue() : null;
	}
	
	public Double queryDouble(String sql) {
		return queryDouble(sql, NULL_PARA_ARRAY);
	}
	
	public Float queryFloat(String sql, Object... paras) {
		Number n = queryNumber(sql, paras);
		return n != null ? n.floatValue() : null;
	}
	
	public Float queryFloat(String sql) {
		return queryFloat(sql, NULL_PARA_ARRAY);
	}
	
	public BigDecimal queryBigDecimal(String sql, Object... paras) {
		Object n = queryColumn(sql, paras);
		if (n instanceof BigDecimal) {
			return (BigDecimal)n;
		} else if (n != null) {
			return new BigDecimal(n.toString());
		} else {
			return null;
		}
	}
	
	public BigDecimal queryBigDecimal(String sql) {
		return queryBigDecimal(sql, NULL_PARA_ARRAY);
	}
	
	public BigInteger queryBigInteger(String sql, Object... paras) {
		Object n = queryColumn(sql, paras);
		if (n instanceof BigInteger) {
			return (BigInteger)n;
		} else if (n != null) {
			return new BigInteger(n.toString());
		} else {
			return null;
		}
	}
	
	public BigInteger queryBigInteger(String sql) {
		return queryBigInteger(sql, NULL_PARA_ARRAY);
	}
	
	public byte[] queryBytes(String sql, Object... paras) {
		return (byte[])queryColumn(sql, paras);
	}
	
	public byte[] queryBytes(String sql) {
		return (byte[])queryColumn(sql, NULL_PARA_ARRAY);
	}
	
	public java.util.Date queryDate(String sql, Object... paras) {
		Object d = queryColumn(sql, paras);
		
		if (d instanceof Temporal) {
			if (d instanceof LocalDateTime) {
				return TimeKit.toDate((LocalDateTime)d);
			}
			if (d instanceof LocalDate) {
				return TimeKit.toDate((LocalDate)d);
			}
			if (d instanceof LocalTime) {
				return TimeKit.toDate((LocalTime)d);
			}
		}
		
		return (java.util.Date)d;
	}
	
	public java.util.Date queryDate(String sql) {
		return queryDate(sql, NULL_PARA_ARRAY);
	}
	
	public LocalDateTime queryLocalDateTime(String sql, Object... paras) {
		Object d = queryColumn(sql, paras);
		
		if (d instanceof LocalDateTime) {
			return (LocalDateTime)d;
		}
		if (d instanceof LocalDate) {
			return ((LocalDate)d).atStartOfDay();
		}
		if (d instanceof LocalTime) {
			return LocalDateTime.of(LocalDate.now(), (LocalTime)d);
		}
		if (d instanceof java.util.Date) {
			return TimeKit.toLocalDateTime((java.util.Date)d);
		}
		
		return (LocalDateTime)d;
	}
	
	public LocalDateTime queryLocalDateTime(String sql) {
		return queryLocalDateTime(sql, NULL_PARA_ARRAY);
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
	
	public Short queryShort(String sql, Object... paras) {
		Number n = queryNumber(sql, paras);
		return n != null ? n.shortValue() : null;
	}
	
	public Short queryShort(String sql) {
		return queryShort(sql, NULL_PARA_ARRAY);
	}
	
	public Byte queryByte(String sql, Object... paras) {
		Number n = queryNumber(sql, paras);
		return n != null ? n.byteValue() : null;
	}
	
	public Byte queryByte(String sql) {
		return queryByte(sql, NULL_PARA_ARRAY);
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
	protected int update(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(sql)) {
			config.dialect.fillStatement(pst, paras);
			int result = pst.executeUpdate();
			return result;
		}
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
	
	protected List<Record> find(Config config, Connection conn, String sql, Object... paras) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(sql)) {
			config.dialect.fillStatement(pst, paras);
			ResultSet rs = pst.executeQuery();
			List<Record> result = config.dialect.buildRecordList(config, rs);	// RecordBuilder.build(config, rs);
			DbKit.close(rs);
			return result;
		}
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
	
	public List<Record> findAll(String tableName) {
		String sql = config.dialect.forFindAll(tableName);
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
		return findFirst(sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Find record by id with default primary key.
	 * <pre>
	 * Example:
	 * Record user = Db.use().findById("user", 15);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 */
	public Record findById(String tableName, Object idValue) {
		return findByIds(tableName, config.dialect.getDefaultPrimaryKey(), idValue);
	}
	
	public Record findById(String tableName, String primaryKey, Object idValue) {
		return findByIds(tableName, primaryKey, idValue);
	}
	
	/**
	 * Find record by ids.
	 * <pre>
	 * Example:
	 * Record user = Db.use().findByIds("user", "user_id", 123);
	 * Record userRole = Db.use().findByIds("user_role", "user_id, role_id", 123, 456);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param idValues the id value of the record, it can be composite id values
	 */
	public Record findByIds(String tableName, String primaryKey, Object... idValues) {
		String[] pKeys = primaryKey.split(",");
		if (pKeys.length != idValues.length)
			throw new IllegalArgumentException("primary key number must equals id value number");
		
		String sql = config.dialect.forDbFindById(tableName, pKeys);
		List<Record> result = find(sql, idValues);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Delete record by id with default primary key.
	 * <pre>
	 * Example:
	 * Db.use().deleteById("user", 15);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param idValue the id value of the record
	 * @return true if delete succeed otherwise false
	 */
	public boolean deleteById(String tableName, Object idValue) {
		return deleteByIds(tableName, config.dialect.getDefaultPrimaryKey(), idValue);
	}
	
	public boolean deleteById(String tableName, String primaryKey, Object idValue) {
		return deleteByIds(tableName, primaryKey, idValue);
	}
	
	/**
	 * Delete record by ids.
	 * <pre>
	 * Example:
	 * Db.use().deleteByIds("user", "user_id", 15);
	 * Db.use().deleteByIds("user_role", "user_id, role_id", 123, 456);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param idValues the id value of the record, it can be composite id values
	 * @return true if delete succeed otherwise false
	 */
	public boolean deleteByIds(String tableName, String primaryKey, Object... idValues) {
		String[] pKeys = primaryKey.split(",");
		if (pKeys.length != idValues.length)
			throw new IllegalArgumentException("primary key number must equals id value number");
		
		String sql = config.dialect.forDbDeleteById(tableName, pKeys);
		return update(sql, idValues) >= 1;
	}
	
	/**
	 * Delete record.
	 * <pre>
	 * Example:
	 * boolean succeed = Db.use().delete("user", "id", user);
	 * </pre>
	 * @param tableName the table name of the table
	 * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
	 * @param record the record
	 * @return true if delete succeed otherwise false
	 */
	public boolean delete(String tableName, String primaryKey, Record record) {
		String[] pKeys = primaryKey.split(",");
		if (pKeys.length <= 1) {
			Object t = record.get(primaryKey);	// 引入中间变量避免 JDK 8 传参有误
			return deleteByIds(tableName, primaryKey, t);
		}
		
		config.dialect.trimPrimaryKeys(pKeys);
		Object[] idValue = new Object[pKeys.length];
		for (int i=0; i<pKeys.length; i++) {
			idValue[i] = record.get(pKeys[i]);
			if (idValue[i] == null)
				throw new IllegalArgumentException("The value of primary key \"" + pKeys[i] + "\" can not be null in record object");
		}
		return deleteByIds(tableName, primaryKey, idValue);
	}
	
	/**
	 * <pre>
	 * Example:
	 * boolean succeed = Db.use().delete("user", user);
	 * </pre>
	 * @see #delete(String, String, Record)
	 */
	public boolean delete(String tableName, Record record) {
		String defaultPrimaryKey = config.dialect.getDefaultPrimaryKey();
		Object t = record.get(defaultPrimaryKey);	// 引入中间变量避免 JDK 8 传参有误
		return deleteByIds(tableName, defaultPrimaryKey, t);
	}
	
	/**
	 * Execute delete sql statement.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return the row count for <code>DELETE</code> statements, or 0 for SQL statements 
     *         that return nothing
	 */
	public int delete(String sql, Object... paras) {
		return update(sql, paras);
	}
	
	/**
	 * @see #delete(String, Object...)
	 * @param sql an SQL statement
	 */
	public int delete(String sql) {
		return update(sql);
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
	public Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		return doPaginate(pageNumber, pageSize, null, select, sqlExceptSelect, paras);
	}
	
	/**
	 * @see #paginate(String, int, int, String, String, Object...)
	 */
	public Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return doPaginate(pageNumber, pageSize, null, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	public Page<Record> paginate(int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		return doPaginate(pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
	}
	
	protected Page<Record> doPaginate(int pageNumber, int pageSize, Boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			String totalRowSql = config.dialect.forPaginateTotalRow(select, sqlExceptSelect, null);
			StringBuilder findSql = new StringBuilder();
			findSql.append(select).append(' ').append(sqlExceptSelect);
			return doPaginateByFullSql(config, conn, pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	protected Page<Record> doPaginateByFullSql(Config config, Connection conn, int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, StringBuilder findSql, Object... paras) throws SQLException {
		if (pageNumber < 1 || pageSize < 1) {
			throw new ActiveRecordException("pageNumber and pageSize must more than 0");
		}
		if (config.dialect.isTakeOverDbPaginate()) {
			return config.dialect.takeOverDbPaginate(conn, pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
		}
		
		List result = query(config, conn, totalRowSql, paras);
		int size = result.size();
		if (isGroupBySql == null) {
			isGroupBySql = size > 1;
		}
		
		long totalRow;
		if (isGroupBySql) {
			totalRow = size;
		} else {
			totalRow = (size > 0) ? ((Number)result.get(0)).longValue() : 0;
		}
		if (totalRow == 0) {
			return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, 0, 0);
		}
		
		int totalPage = (int) (totalRow / pageSize);
		if (totalRow % pageSize != 0) {
			totalPage++;
		}
		
		if (pageNumber > totalPage) {
			return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, totalPage, (int)totalRow);
		}
		
		// --------
		String sql = config.dialect.forPaginate(pageNumber, pageSize, findSql);
		List<Record> list = find(config, conn, sql, paras);
		return new Page<Record>(list, pageNumber, pageSize, totalPage, (int)totalRow);
	}
	
	protected Page<Record> paginate(Config config, Connection conn, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) throws SQLException {
		String totalRowSql = config.dialect.forPaginateTotalRow(select, sqlExceptSelect, null);
		StringBuilder findSql = new StringBuilder();
		findSql.append(select).append(' ').append(sqlExceptSelect);
		return doPaginateByFullSql(config, conn, pageNumber, pageSize, null, totalRowSql, findSql, paras);
	}
	
	protected Page<Record> doPaginateByFullSql(int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			StringBuilder findSqlBuf = new StringBuilder().append(findSql);
			return doPaginateByFullSql(config, conn, pageNumber, pageSize, isGroupBySql, totalRowSql, findSqlBuf, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	public Page<Record> paginateByFullSql(int pageNumber, int pageSize, String totalRowSql, String findSql, Object... paras) {
		return doPaginateByFullSql(pageNumber, pageSize, null, totalRowSql, findSql, paras);
	}
	
	public Page<Record> paginateByFullSql(int pageNumber, int pageSize, boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
		return doPaginateByFullSql(pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
	}
	
	protected boolean save(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		String[] pKeys = primaryKey.split(",");
		List<Object> paras = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		config.dialect.forDbSave(tableName, pKeys, record, sql, paras);
		
		try (PreparedStatement pst =
				config.dialect.isOracle() ?
				conn.prepareStatement(sql.toString(), pKeys) :
				conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
			config.dialect.fillStatement(pst, paras);
			int result = pst.executeUpdate();
			config.dialect.getRecordGeneratedKey(pst, record, pKeys);
			record.clearModifyFlag();
			return result >= 1;
		}
	}
	
	/**
	 * Save record.
	 * <pre>
	 * Example:
	 * Record userRole = new Record().set("user_id", 123).set("role_id", 456);
	 * Db.use().save("user_role", "user_id, role_id", userRole);
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
	
	protected boolean update(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
		if (record.modifyFlag == null || record.modifyFlag.isEmpty()) {
			return false;
		}
		
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
		
		if (paras.size() <= 1) {	// 参数个数为 1 的情况表明只有主键，也无需更新
			return false;
		}
		
		int result = update(config, conn, sql.toString(), paras.toArray());
		if (result >= 1) {
			record.clearModifyFlag();
			return true;
		}
		return false;
	}
	
	/**
	 * Update Record.
	 * <pre>
	 * Example:
	 * Db.use().update("user_role", "user_id, role_id", record);
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
	 * Db.use().update("user", record);
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
	protected Object execute(Config config, ICallback callback) {
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
	protected boolean tx(Config config, int transactionLevel, IAtom atom) {
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
			if (conn != null) try {conn.rollback();} catch (Exception e1) {LogKit.error(e1.getMessage(), e1);}
			LogKit.logNothing(e);
			return false;
		} catch (Throwable t) {
			if (conn != null) try {conn.rollback();} catch (Exception e1) {LogKit.error(e1.getMessage(), e1);}
			throw t instanceof RuntimeException ? (RuntimeException)t : new ActiveRecordException(t);
		} finally {
			try {
				if (conn != null) {
					if (autoCommit != null)
						conn.setAutoCommit(autoCommit);
					conn.close();
				}
			} catch (Throwable t) {
				LogKit.error(t.getMessage(), t);	// can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
			} finally {
				config.removeThreadLocalConnection();	// prevent memory leak
			}
		}
	}
	
	/**
	 * Execute transaction with default transaction level.
	 * @see #tx(int, IAtom)
	 */
	public boolean tx(IAtom atom) {
		return tx(config, config.getTransactionLevel(), atom);
	}
	
	public boolean tx(int transactionLevel, IAtom atom) {
		return tx(config, transactionLevel, atom);
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
	public Future<Boolean> txInNewThread(IAtom atom) {
		FutureTask<Boolean> task = new FutureTask<>(() -> tx(config, config.getTransactionLevel(), atom));
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
		return task;
	}
	
	public Future<Boolean> txInNewThread(int transactionLevel, IAtom atom) {
		FutureTask<Boolean> task = new FutureTask<>(() -> tx(config, transactionLevel, atom));
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
		return task;
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
		return doPaginateByCache(cacheName, key, pageNumber, pageSize, null, select, sqlExceptSelect, paras);
	}
	
	/**
	 * @see #paginateByCache(String, Object, int, int, String, String, Object...)
	 */
	public Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return doPaginateByCache(cacheName, key, pageNumber, pageSize, null, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	public Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		return doPaginateByCache(cacheName, key, pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
	}
	
	protected Page<Record> doPaginateByCache(String cacheName, Object key, int pageNumber, int pageSize, Boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		ICache cache = config.getCache();
		Page<Record> result = cache.get(cacheName, key);
		if (result == null) {
			result = doPaginate(pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
			cache.put(cacheName, key, result);
		}
		return result;
	}
	
	protected int[] batch(Config config, Connection conn, String sql, Object[][] paras, int batchSize) throws SQLException {
		if (paras == null || paras.length == 0)
			return new int[0];
		if (batchSize < 1)
			throw new IllegalArgumentException("The batchSize must more than 0.");
		
		boolean isInTransaction = config.isInTransaction();
		int counter = 0;
		int pointer = 0;
		int[] result = new int[paras.length];
		try (PreparedStatement pst = conn.prepareStatement(sql)) {
			for (int i=0; i<paras.length; i++) {
				for (int j=0; j<paras[i].length; j++) {
					Object value = paras[i][j];
					if (value instanceof java.util.Date) {
						if (value instanceof java.sql.Date) {
							pst.setDate(j + 1, (java.sql.Date)value);
						} else if (value instanceof java.sql.Timestamp) {
							pst.setTimestamp(j + 1, (java.sql.Timestamp)value);
						} else {
							// Oracle、SqlServer 中的 TIMESTAMP、DATE 支持 new Date() 给值
							java.util.Date d = (java.util.Date)value;
							pst.setTimestamp(j + 1, new java.sql.Timestamp(d.getTime()));
						}
					}
					else {
						pst.setObject(j + 1, value);
					}
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
			if (counter != 0) {
				int[] r = pst.executeBatch();
				if (isInTransaction == false)
					conn.commit();
				for (int k = 0; k < r.length; k++)
					result[pointer++] = r[k];
			}
			
			return result;
		}
	}
	
    /**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <pre>
     * Example:
     * String sql = "insert into user(name, cash) values(?, ?)";
     * int[] result = Db.use().batch(sql, new Object[][]{{"James", 888}, {"zhanjin", 888}});
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
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {LogKit.error(e.getMessage(), e);}
			config.close(conn);
		}
	}
	
	protected int[] batch(Config config, Connection conn, String sql, String columns, List list, int batchSize) throws SQLException {
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
		try (PreparedStatement pst = conn.prepareStatement(sql)) {
			for (int i=0; i<size; i++) {
				Map map = isModel ? ((Model)list.get(i))._getAttrs() : ((Record)list.get(i)).getColumns();
				for (int j=0; j<columnArray.length; j++) {
					Object value = map.get(columnArray[j]);
					if (value instanceof java.util.Date) {
						if (value instanceof java.sql.Date) {
							pst.setDate(j + 1, (java.sql.Date)value);
						} else if (value instanceof java.sql.Timestamp) {
							pst.setTimestamp(j + 1, (java.sql.Timestamp)value);
						} else {
							// Oracle、SqlServer 中的 TIMESTAMP、DATE 支持 new Date() 给值
							java.util.Date d = (java.util.Date)value;
							pst.setTimestamp(j + 1, new java.sql.Timestamp(d.getTime()));
						}
					}
					else {
						pst.setObject(j + 1, value);
					}
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
			if (counter != 0) {
				int[] r = pst.executeBatch();
				if (isInTransaction == false)
					conn.commit();
				for (int k = 0; k < r.length; k++)
					result[pointer++] = r[k];
			}
			
			return result;
		}
	}
	
	/**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <pre>
     * Example:
     * String sql = "insert into user(name, cash) values(?, ?)";
     * int[] result = Db.use().batch(sql, "name, cash", modelList, 500);
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
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {LogKit.error(e.getMessage(), e);}
			config.close(conn);
		}
	}
	
	protected int[] batch(Config config, Connection conn, List<String> sqlList, int batchSize) throws SQLException {
		if (sqlList == null || sqlList.size() == 0)
			return new int[0];
		if (batchSize < 1)
			throw new IllegalArgumentException("The batchSize must more than 0.");
		
		boolean isInTransaction = config.isInTransaction();
		int counter = 0;
		int pointer = 0;
		int size = sqlList.size();
		int[] result = new int[size];
		try (Statement st = conn.createStatement()) {
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
			if (counter != 0) {
				int[] r = st.executeBatch();
				if (isInTransaction == false)
					conn.commit();
				for (int k = 0; k < r.length; k++)
					result[pointer++] = r[k];
			}
			
			return result;
		}
	}
	
    /**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <pre>
     * Example:
     * int[] result = Db.use().batch(sqlList, 500);
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
				try {conn.setAutoCommit(autoCommit);} catch (Exception e) {LogKit.error(e.getMessage(), e);}
			config.close(conn);
		}
    }
    
    /**
     * Batch save models using the "insert into ..." sql generated by the first model in modelList.
     * Ensure all the models can use the same sql as the first model.
     */
    public int[] batchSave(List<? extends Model> modelList, int batchSize) {
    	if (modelList == null || modelList.size() == 0)
    		return new int[0];
    	
    	Model model = modelList.get(0);
    	Map<String, Object> attrs = model._getAttrs();
    	int index = 0;
    	StringBuilder columns = new StringBuilder();
    	// the same as the iterator in Dialect.forModelSave() to ensure the order of the attrs
		for (Entry<String, Object> e : attrs.entrySet()) {
			if (config.dialect.isOracle()) {	// 支持 oracle 自增主键
				Object value = e.getValue();
				if (value instanceof String && ((String)value).endsWith(".nextval")) {
					continue ;
				}
			}
			
			if (index++ > 0) {
				columns.append(',');
			}
			columns.append(e.getKey());
		}
    	
    	StringBuilder sql = new StringBuilder();
    	List<Object> parasNoUse = new ArrayList<Object>();
    	config.dialect.forModelSave(TableMapping.me().getTable(model.getClass()), attrs, sql, parasNoUse);
    	return batch(sql.toString(), columns.toString(), modelList, batchSize);
    }
    
    /**
     * Batch save records using the "insert into ..." sql generated by the first record in recordList.
     * Ensure all the record can use the same sql as the first record.
     * @param tableName the table name
     */
    public int[] batchSave(String tableName, List<? extends Record> recordList, int batchSize) {
    	if (recordList == null || recordList.size() == 0)
    		return new int[0];
    	
    	Record record = recordList.get(0);
    	Map<String, Object> cols = record.getColumns();
    	int index = 0;
    	StringBuilder columns = new StringBuilder();
    	// the same as the iterator in Dialect.forDbSave() to ensure the order of the columns
		for (Entry<String, Object> e : cols.entrySet()) {
			if (config.dialect.isOracle()) {	// 支持 oracle 自增主键
				Object value = e.getValue();
				if (value instanceof String && ((String)value).endsWith(".nextval")) {
					continue ;
				}
			}
			
			if (index++ > 0) {
				columns.append(',');
			}
			columns.append(e.getKey());
		}
    	
    	String[] pKeysNoUse = new String[0];
    	StringBuilder sql = new StringBuilder();
    	List<Object> parasNoUse = new ArrayList<Object>();
    	config.dialect.forDbSave(tableName, pKeysNoUse, record, sql, parasNoUse);
    	return batch(sql.toString(), columns.toString(), recordList, batchSize);
    }
    
    /**
     * Batch update models using the attrs names of the first model in modelList.
     * Ensure all the models can use the same sql as the first model.
     */
    public int[] batchUpdate(List<? extends Model> modelList, int batchSize) {
    	if (modelList == null || modelList.size() == 0)
    		return new int[0];
    	
    	Model model = modelList.get(0);
    	
    	// 新增支持 modifyFlag
    	if (model.modifyFlag == null || model.modifyFlag.isEmpty()) {
    		return new int[0];
    	}
    	Set<String> modifyFlag = model._getModifyFlag();
    	
    	Table table = TableMapping.me().getTable(model.getClass());
    	String[] pKeys = table.getPrimaryKey();
    	Map<String, Object> attrs = model._getAttrs();
    	List<String> attrNames = new ArrayList<String>();
    	// the same as the iterator in Dialect.forModelSave() to ensure the order of the attrs
    	for (Entry<String, Object> e : attrs.entrySet()) {
    		String attr = e.getKey();
    		if (modifyFlag.contains(attr) && !config.dialect.isPrimaryKey(attr, pKeys) && table.hasColumnLabel(attr))
    			attrNames.add(attr);
    	}
    	for (String pKey : pKeys)
    		attrNames.add(pKey);
    	String columns = StrKit.join(attrNames.toArray(new String[attrNames.size()]), ",");
    	
    	// update all attrs of the model not use the midifyFlag of every single model
    	// Set<String> modifyFlag = attrs.keySet();	// model.getModifyFlag();
    	
    	StringBuilder sql = new StringBuilder();
    	List<Object> parasNoUse = new ArrayList<Object>();
    	config.dialect.forModelUpdate(TableMapping.me().getTable(model.getClass()), attrs, modifyFlag, sql, parasNoUse);
    	return batch(sql.toString(), columns, modelList, batchSize);
    }
    
    /**
     * Batch update records using the columns names of the first record in recordList.
     * Ensure all the records can use the same sql as the first record.
     * @param tableName the table name
     * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
     */
    public int[] batchUpdate(String tableName, String primaryKey, List<? extends Record> recordList, int batchSize) {
    	if (recordList == null || recordList.size() == 0)
    		return new int[0];
    	
    	String[] pKeys = primaryKey.split(",");
    	config.dialect.trimPrimaryKeys(pKeys);
    	
    	Record record = recordList.get(0);
    	
    	// Record 新增支持 modifyFlag
    	if (record.modifyFlag == null || record.modifyFlag.isEmpty()) {
    		return new int[0];
    	}
    	Set<String> modifyFlag = record._getModifyFlag();
    	
    	Map<String, Object> cols = record.getColumns();
    	List<String> colNames = new ArrayList<String>();
    	// the same as the iterator in Dialect.forDbUpdate() to ensure the order of the columns
    	for (Entry<String, Object> e : cols.entrySet()) {
    		String col = e.getKey();
    		if (modifyFlag.contains(col) && !config.dialect.isPrimaryKey(col, pKeys))
    			colNames.add(col);
    	}
    	for (String pKey : pKeys)
    		colNames.add(pKey);
    	String columns = StrKit.join(colNames.toArray(new String[colNames.size()]), ",");
    	
    	Object[] idsNoUse = new Object[pKeys.length];
    	StringBuilder sql = new StringBuilder();
    	List<Object> parasNoUse = new ArrayList<Object>();
    	config.dialect.forDbUpdate(tableName, pKeys, idsNoUse, record, sql, parasNoUse);
    	return batch(sql.toString(), columns, recordList, batchSize);
    }
    
    /**
     * Batch update records with default primary key, using the columns names of the first record in recordList.
     * Ensure all the records can use the same sql as the first record.
     * @param tableName the table name
     */
    public int[] batchUpdate(String tableName, List<? extends Record> recordList, int batchSize) {
    	return batchUpdate(tableName, config.dialect.getDefaultPrimaryKey(),recordList, batchSize);
    }
    
    public String getSql(String key) {
    	return config.getSqlKit().getSql(key);
    }
    
    // 支持传入变量用于 sql 生成。为了避免用户将参数拼接在 sql 中引起 sql 注入风险，只在 SqlKit 中开放该功能
    // public String getSql(String key, Map data) {
    //     return config.getSqlKit().getSql(key, data);
    // }
    
    public SqlPara getSqlPara(String key, Record record) {
    	return getSqlPara(key, record.getColumns());
    }
    
    public SqlPara getSqlPara(String key, Model model) {
    	return getSqlPara(key, model._getAttrs());
    }
    
    public SqlPara getSqlPara(String key, Map data) {
    	return config.getSqlKit().getSqlPara(key, data);
    }
    
    public SqlPara getSqlPara(String key, Object... paras) {
    	return config.getSqlKit().getSqlPara(key, paras);
    }
	
	public SqlPara getSqlParaByString(String content, Map data) {
		return config.getSqlKit().getSqlParaByString(content, data);
	}
	
	public SqlPara getSqlParaByString(String content, Object... paras) {
		return config.getSqlKit().getSqlParaByString(content, paras);
	}
	
    public List<Record> find(SqlPara sqlPara) {
    	return find(sqlPara.getSql(), sqlPara.getPara());
    }
    
    public Record findFirst(SqlPara sqlPara) {
    	return findFirst(sqlPara.getSql(), sqlPara.getPara());
    }
    
    public int update(SqlPara sqlPara) {
    	return update(sqlPara.getSql(), sqlPara.getPara());
    }
    
    public Page<Record> paginate(int pageNumber, int pageSize, SqlPara sqlPara) {
    	String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
    	return doPaginate(pageNumber, pageSize, null, sqls[0], sqls[1], sqlPara.getPara());
    }
	
	public Page<Record> paginate(int pageNumber, int pageSize, boolean isGroupBySql, SqlPara sqlPara) {
		String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
		return doPaginate(pageNumber, pageSize, isGroupBySql, sqls[0], sqls[1], sqlPara.getPara());
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
	public void each(Function<Record, Boolean> func, String sql, Object... paras) {
		Connection conn = null;
		try {
			conn = config.getConnection();
			
			try (PreparedStatement pst = conn.prepareStatement(sql)) {
				config.dialect.fillStatement(pst, paras);
				ResultSet rs = pst.executeQuery();
				config.dialect.eachRecord(config, rs, func);
				DbKit.close(rs);
			}
			
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	// ---------
	
	public DbTemplate template(String key, Map data) {
		return new DbTemplate(this, key, data);
	}
	
	public DbTemplate template(String key, Object... paras) {
		return new DbTemplate(this, key, paras);
	}
	
	// ---------
	
	public DbTemplate templateByString(String content, Map data) {
		return new DbTemplate(true, this, content, data);
	}
	
	public DbTemplate templateByString(String content, Object... paras) {
		return new DbTemplate(true, this, content, paras);
	}
}



