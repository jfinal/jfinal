/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import com.jfinal.plugin.activerecord.cache.ICache;
import static com.jfinal.plugin.activerecord.DbKit.NULL_PARA_ARRAY;

/**
 * Model.
 * <p>
 * A clever person solves a problem.
 * A wise person avoids it.
 * A stupid person makes it.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class Model<M extends Model> implements Serializable {
	
	private static final long serialVersionUID = -990334519496260591L;
	
	public static final int FILTER_BY_SAVE = 0;
	public static final int FILTER_BY_UPDATE = 1;
	
	public M dao() {
		attrs = DaoContainerFactory.daoMap;
		modifyFlag = DaoContainerFactory.daoSet;
		return (M)this;
	}
	
	/**
	 * Attributes of this model
	 */
	private Map<String, Object> attrs = getAttrsMap();	// getConfig().containerFactory.getAttrsMap();	// new HashMap<String, Object>();
	
	private Map<String, Object> getAttrsMap() {
		Config config = getConfig();
		if (config == null)
			return DbKit.brokenConfig.containerFactory.getAttrsMap();
		return config.containerFactory.getAttrsMap();
	}
	
	/**
	 * Flag of column has been modified. update need this flag
	 */
	private Set<String> modifyFlag;
	
	/*
	private Set<String> getModifyFlag() {
		if (modifyFlag == null)
			modifyFlag = getConfig().containerFactory.getModifyFlagSet();	// new HashSet<String>();
		return modifyFlag;
	}*/
	
	Set<String> getModifyFlag() {
		if (modifyFlag == null) {
			Config config = getConfig();
			if (config == null)
				modifyFlag = DbKit.brokenConfig.containerFactory.getModifyFlagSet();
			else
				modifyFlag = config.containerFactory.getModifyFlagSet();
		}
		return modifyFlag;
	}
	
	private String configName = null;
	
	/**
	 * Switching data source, dialect and all config by configName
	 */
	public M use(String configName) {
		this.configName = configName;
		return (M)this;
	}
	
	protected Config getConfig() {
		if (configName != null)
			return DbKit.getConfig(configName);
		return DbKit.getConfig(getUsefulClass());
	}
	
	/*
	private Config getConfig() {
		return DbKit.getConfig(getUsefulClass());
	}*/
	
	private Table getTable() {
		return TableMapping.me().getTable(getUsefulClass());
	}
	
	/**
	 * Set attribute to model.
	 * @param attr the attribute name of the model
	 * @param value the value of the attribute
	 * @return this model
	 * @throws ActiveRecordException if the attribute is not exists of the model
	 */
	public M set(String attr, Object value) {
		Table table = getTable();	// table 为 null 时用于未启动 ActiveRecordPlugin 的场景
		if (table != null && !table.hasColumnLabel(attr)) {
			throw new ActiveRecordException("The attribute name does not exist: \"" + attr + "\"");
		}
		
		attrs.put(attr, value);
		getModifyFlag().add(attr);	// Add modify flag, update() need this flag.
		return (M)this;
	}
	
	// public static transient boolean checkPutKey = true;
	/**
	 * Put key value pair to the model without check attribute name.
	 */
	public M put(String key, Object value) {
		/*
		if (checkPutKey) {
			Table table = getTable();	// table 为 null 时用于未启动 ActiveRecordPlugin 的场景
			if (table != null && table.hasColumnLabel(key)) {
				throw new ActiveRecordException("The key can not be attribute name: \"" + key + "\", using set(String, Object) for attribute value");
			}
		}*/
		attrs.put(key, value);
		return (M)this;
	}
	
	/**
	 * Put map to the model without check attribute name.
	 */
	public M put(Map<String, Object> map) {
		attrs.putAll(map);
		return (M)this;
	}
	
	/**
	 * Put other model to the model without check attribute name.
	 */
	public M put(Model model) {
		attrs.putAll(model.getAttrs());
		return (M)this;
	}
	
	/**
	 * Put record to the model without check attribute name.
	 */
	public M put(Record record) {
		attrs.putAll(record.getColumns());
		return (M)this;
	}
	
	/**
	 * Convert model to record.
	 */
	public Record toRecord() {
		return new Record().setColumns(getAttrs());
	}
	
	/**
	 * Get attribute of any mysql type
	 */
	public <T> T get(String attr) {
		return (T)(attrs.get(attr));
	}
	
	/**
	 * Get attribute of any mysql type. Returns defaultValue if null.
	 */
	public <T> T get(String attr, Object defaultValue) {
		Object result = attrs.get(attr);
		return (T)(result != null ? result : defaultValue);
	}
	
	/**
	 * Get attribute of mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
	 */
	public String getStr(String attr) {
		return (String)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: int, integer, tinyint(n) n > 1, smallint, mediumint
	 */
	public Integer getInt(String attr) {
		return (Integer)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: bigint, unsign int
	 */
	public Long getLong(String attr) {
		return (Long)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: unsigned bigint
	 */
	public java.math.BigInteger getBigInteger(String attr) {
		return (java.math.BigInteger)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: date, year
	 */
	public java.util.Date getDate(String attr) {
		return (java.util.Date)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: time
	 */
	public java.sql.Time getTime(String attr) {
		return (java.sql.Time)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: timestamp, datetime
	 */
	public java.sql.Timestamp getTimestamp(String attr) {
		return (java.sql.Timestamp)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: real, double
	 */
	public Double getDouble(String attr) {
		return (Double)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: float
	 */
	public Float getFloat(String attr) {
		return (Float)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: bit, tinyint(1)
	 */
	public Boolean getBoolean(String attr) {
		return (Boolean)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: decimal, numeric
	 */
	public java.math.BigDecimal getBigDecimal(String attr) {
		return (java.math.BigDecimal)attrs.get(attr);
	}
	
	/**
	 * Get attribute of mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob
	 */
	public byte[] getBytes(String attr) {
		return (byte[])attrs.get(attr);
	}
	
	/**
	 * Get attribute of any type that extends from Number
	 */
	public Number getNumber(String attr) {
		return (Number)attrs.get(attr);
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
	public Page<M> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		return doPaginate(pageNumber, pageSize, null, select, sqlExceptSelect, paras);
	}
	
	/**
	 * @see #paginate(int, int, String, String, Object...)
	 */
	public Page<M> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return doPaginate(pageNumber, pageSize, null, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	/**
	 * 指定分页 sql 最外层以是否含有 group by 语句
	 * <pre>
	 * 举例：
	 * paginate(1, 10, true, "select *", "from user where id>? group by age", 123);
	 * </pre>
	 */
	public Page<M> paginate(int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		return doPaginate(pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
	}
	
	private Page<M> doPaginate(int pageNumber, int pageSize, Boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		Config config = getConfig();
		Connection conn = null;
		try {
			conn = config.getConnection();
			String totalRowSql = "select count(*) " + config.dialect.replaceOrderBy(sqlExceptSelect);
			StringBuilder findSql = new StringBuilder();
			findSql.append(select).append(" ").append(sqlExceptSelect);
			return doPaginateByFullSql(config, conn, pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	private Page<M> doPaginateByFullSql(Config config, Connection conn, int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, StringBuilder findSql, Object... paras) throws Exception {
		if (pageNumber < 1 || pageSize < 1) {
			throw new ActiveRecordException("pageNumber and pageSize must more than 0");
		}
		if (config.dialect.isTakeOverModelPaginate()) {
			return config.dialect.takeOverModelPaginate(conn, getUsefulClass(), pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
		}
		
		List result = Db.query(config, conn, totalRowSql, paras);
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
			return new Page<M>(new ArrayList<M>(0), pageNumber, pageSize, 0, 0);	// totalRow = 0;
		}
		
		int totalPage = (int) (totalRow / pageSize);
		if (totalRow % pageSize != 0) {
			totalPage++;
		}
		
		if (pageNumber > totalPage) {
			return new Page<M>(new ArrayList<M>(0), pageNumber, pageSize, totalPage, (int)totalRow);
		}
		
		// --------
		String sql = config.dialect.forPaginate(pageNumber, pageSize, findSql);
		List<M> list = find(conn, sql, paras);
		return new Page<M>(list, pageNumber, pageSize, totalPage, (int)totalRow);
	}
	
	private Page<M> doPaginateByFullSql(int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
		Config config = getConfig();
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
	
	public Page<M> paginateByFullSql(int pageNumber, int pageSize, String totalRowSql, String findSql, Object... paras) {
		return doPaginateByFullSql(pageNumber, pageSize, null, totalRowSql, findSql, paras);
	}
	
	public Page<M> paginateByFullSql(int pageNumber, int pageSize, boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
		return doPaginateByFullSql(pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
	}
	
	/**
	 * Return attribute Map.
	 * <p>
	 * Danger! The update method will ignore the attribute if you change it directly.
	 * You must use set method to change attribute that update method can handle it.
	 */
	protected Map<String, Object> getAttrs() {
		return attrs;
	}
	
	/**
	 * Return attribute Set.
	 */
	public Set<Entry<String, Object>> _getAttrsEntrySet() {
		return attrs.entrySet();
	}
	
	/**
	 * Save model.
	 */
	public boolean save() {
		filter(FILTER_BY_SAVE);
		
		Config config = getConfig();
		Table table = getTable();
		
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		config.dialect.forModelSave(table, attrs, sql, paras);
		// if (paras.size() == 0)	return false;	// The sql "insert into tableName() values()" works fine, so delete this line
		
		// --------
		Connection conn = null;
		PreparedStatement pst = null;
		int result = 0;
		try {
			conn = config.getConnection();
			if (config.dialect.isOracle())
				pst = conn.prepareStatement(sql.toString(), table.getPrimaryKey());
			else
				pst = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			
			config.dialect.fillStatement(pst, paras);
			result = pst.executeUpdate();
			getGeneratedKey(pst, table, config);
			getModifyFlag().clear();
			return result >= 1;
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(pst, conn);
		}
	}
	
	/**
	 * Get id after save method.
	 */
	private void getGeneratedKey(PreparedStatement pst, Table table, Config config) throws SQLException {
		String[] pKeys = table.getPrimaryKey();
		ResultSet rs = pst.getGeneratedKeys();
		for (String pKey : pKeys) {
			if (get(pKey) == null || config.dialect.isOracle()) {
				if (rs.next()) {
					Class colType = table.getColumnType(pKey);
					if (colType == Integer.class || colType == int.class)
						set(pKey, rs.getInt(1));
					else if (colType == Long.class || colType == long.class)
						set(pKey, rs.getLong(1));
					else
						set(pKey, rs.getObject(1));		// It returns Long object for int colType
				}
			}
		}
		rs.close();
	}
	
	/**
	 * Delete model.
	 */
	public boolean delete() {
		Table table = getTable();
		String[] pKeys = table.getPrimaryKey();
		Object[] ids = new Object[pKeys.length];
		for (int i=0; i<pKeys.length; i++) {
			ids[i] = attrs.get(pKeys[i]);
			if (ids[i] == null)
				throw new ActiveRecordException("You can't delete model without primary key value, " + pKeys[i] + " is null");
		}
		return deleteById(table, ids);
	}
	
	/**
	 * Delete model by id.
	 * @param idValue the id value of the model
	 * @return true if delete succeed otherwise false
	 */
	public boolean deleteById(Object idValue) {
		if (idValue == null)
			throw new IllegalArgumentException("idValue can not be null");
		return deleteById(getTable(), idValue);
	}
	
	/**
	 * Delete model by composite id values.
	 * @param idValues the composite id values of the model
	 * @return true if delete succeed otherwise false
	 */
	public boolean deleteById(Object... idValues) {
		Table table = getTable();
		if (idValues == null || idValues.length != table.getPrimaryKey().length)
			throw new IllegalArgumentException("Primary key nubmer must equals id value number and can not be null");
		
		return deleteById(table, idValues);
	}
	
	private boolean deleteById(Table table, Object... idValues) {
		Config config = getConfig();
		Connection conn = null;
		try {
			conn = config.getConnection();
			String sql = config.dialect.forModelDeleteById(table);
			return Db.update(config, conn, sql, idValues) >= 1;
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * Update model.
	 */
	public boolean update() {
		filter(FILTER_BY_UPDATE);
		
		if (getModifyFlag().isEmpty()) {
			return false;
		}
		
		Table table = getTable();
		String[] pKeys = table.getPrimaryKey();
		for (String pKey : pKeys) {
			Object id = attrs.get(pKey);
			if (id == null)
				throw new ActiveRecordException("You can't update model without Primary Key, " + pKey + " can not be null.");
		}
		
		Config config = getConfig();
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		config.dialect.forModelUpdate(table, attrs, getModifyFlag(), sql, paras);
		
		if (paras.size() <= 1) {	// Needn't update
			return false;
		}
		
		// --------
		Connection conn = null;
		try {
			conn = config.getConnection();
			int result = Db.update(config, conn, sql.toString(), paras.toArray());
			if (result >= 1) {
				getModifyFlag().clear();
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * Find model.
	 */
	private List<M> find(Connection conn, String sql, Object... paras) throws Exception {
		Config config = getConfig();
		PreparedStatement pst = conn.prepareStatement(sql);
		config.dialect.fillStatement(pst, paras);
		ResultSet rs = pst.executeQuery();
		List<M> result = config.dialect.buildModelList(rs, getUsefulClass());	// ModelBuilder.build(rs, getUsefulClass());
		DbKit.close(rs, pst);
		return result;
	}
	
	/**
	 * Find model.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return the list of Model
	 */
	public List<M> find(String sql, Object... paras) {
		Config config = getConfig();
		Connection conn = null;
		try {
			conn = config.getConnection();
			return find(conn, sql, paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	
	/**
	 * @see #find(String, Object...)
	 */
	public List<M> find(String sql) {
		return find(sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Find first model. I recommend add "limit 1" in your sql.
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 * @return Model
	 */
	public M findFirst(String sql, Object... paras) {
		List<M> result = find(sql, paras);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * @see #findFirst(String, Object...)
	 * @param sql an SQL statement
	 */
	public M findFirst(String sql) {
		return findFirst(sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Find model by id.
	 * <pre>
	 * Example:
	 * User user = User.dao.findById(123);
	 * </pre>
	 * @param idValue the id value of the model
	 */
	public M findById(Object idValue) {
		return findByIdLoadColumns(new Object[]{idValue}, "*");
	}
	
	/**
	 * Find model by composite id values.
	 * <pre>
	 * Example:
	 * User user = User.dao.findById(123, 456);
	 * </pre>
	 * @param idValues the composite id values of the model
	 */
	public M findById(Object... idValues) {
		return findByIdLoadColumns(idValues, "*");
	}
	
	/**
	 * Find model by id and load specific columns only.
	 * <pre>
	 * Example:
	 * User user = User.dao.findByIdLoadColumns(123, "name, age");
	 * </pre>
	 * @param idValue the id value of the model
	 * @param columns the specific columns to load
	 */
	public M findByIdLoadColumns(Object idValue, String columns) {
		return findByIdLoadColumns(new Object[]{idValue}, columns);
	}
	
	/**
	 * Find model by composite id values and load specific columns only.
	 * <pre>
	 * Example:
	 * User user = User.dao.findByIdLoadColumns(new Object[]{123, 456}, "name, age");
	 * </pre>
	 * @param idValues the composite id values of the model
	 * @param columns the specific columns to load
	 */
	public M findByIdLoadColumns(Object[] idValues, String columns) {
		Table table = getTable();
		if (table.getPrimaryKey().length != idValues.length)
			throw new IllegalArgumentException("id values error, need " + table.getPrimaryKey().length + " id value");
		
		String sql = getConfig().dialect.forModelFindById(table, columns);
		List<M> result = find(sql, idValues);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Set attributes with other model.
	 * @param model the Model
	 * @return this Model
	 */
	public M _setAttrs(M model) {
		return (M)_setAttrs(model.getAttrs());
	}
	
	/**
	 * Set attributes with Map.
	 * @param attrs attributes of this model
	 * @return this Model
	 */
	public M _setAttrs(Map<String, Object> attrs) {
		for (Entry<String, Object> e : attrs.entrySet())
			set(e.getKey(), e.getValue());
		return (M)this;
	}
	
	/**
	 * Remove attribute of this model.
	 * @param attr the attribute name of the model
	 * @return this model
	 */
	public M remove(String attr) {
		attrs.remove(attr);
		getModifyFlag().remove(attr);
		return (M)this;
	}
	
	/**
	 * Remove attributes of this model.
	 * @param attrs the attribute names of the model
	 * @return this model
	 */
	public M remove(String... attrs) {
		if (attrs != null)
			for (String a : attrs) {
				this.attrs.remove(a);
				this.getModifyFlag().remove(a);
			}
		return (M)this;
	}
	
	/**
	 * Remove attributes if it is null.
	 * @return this model
	 */
	public M removeNullValueAttrs() {
		for (Iterator<Entry<String, Object>> it = attrs.entrySet().iterator(); it.hasNext();) {
			Entry<String, Object> e = it.next();
			if (e.getValue() == null) {
				it.remove();
				getModifyFlag().remove(e.getKey());
			}
		}
		return (M)this;
	}
	
	/**
	 * Keep attributes of this model and remove other attributes.
	 * @param attrs the attribute names of the model
	 * @return this model
	 */
	public M keep(String... attrs) {
		if (attrs != null && attrs.length > 0) {
			Config config = getConfig();
			Map<String, Object> newAttrs = config.containerFactory.getAttrsMap();	// new HashMap<String, Object>(attrs.length);
			Set<String> newModifyFlag = config.containerFactory.getModifyFlagSet();	// new HashSet<String>();
			for (String a : attrs) {
				if (this.attrs.containsKey(a))	// prevent put null value to the newColumns
					newAttrs.put(a, this.attrs.get(a));
				if (this.getModifyFlag().contains(a))
					newModifyFlag.add(a);
			}
			this.attrs = newAttrs;
			this.modifyFlag = newModifyFlag;
		}
		else {
			this.attrs.clear();
			this.getModifyFlag().clear();
		}
		return (M)this;
	}
	
	/**
	 * Keep attribute of this model and remove other attributes.
	 * @param attr the attribute name of the model
	 * @return this model
	 */
	public M keep(String attr) {
		if (attrs.containsKey(attr)) {	// prevent put null value to the newColumns
			Object keepIt = attrs.get(attr);
			boolean keepFlag = getModifyFlag().contains(attr);
			attrs.clear();
			getModifyFlag().clear();
			attrs.put(attr, keepIt);
			if (keepFlag)
				getModifyFlag().add(attr);
		}
		else {
			attrs.clear();
			getModifyFlag().clear();
		}
		return (M)this;
	}
	
	/**
	 * Remove all attributes of this model.
	 * @return this model
	 */
	public M clear() {
		attrs.clear();
		getModifyFlag().clear();
		return (M)this;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (Entry<String, Object> e : attrs.entrySet()) {
			if (first)
				first = false;
			else
				sb.append(", ");
			
			Object value = e.getValue();
			if (value != null)
				value = value.toString();
			sb.append(e.getKey()).append(":").append(value);
		}
		sb.append("}");
		return sb.toString();
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Model))
            return false;
		if (getUsefulClass() != ((Model)o).getUsefulClass())
			return false;
		if (o == this)
			return true;
		return this.attrs.equals(((Model)o).attrs);
	}
	
	public int hashCode() {
		return (attrs == null ? 0 : attrs.hashCode()) ^ (getModifyFlag() == null ? 0 : getModifyFlag().hashCode());
	}
	
	/**
	 * Find model by cache.
	 * @see #find(String, Object...)
	 * @param cacheName the cache name
	 * @param key the key used to get data from cache
	 * @return the list of Model
	 */
	public List<M> findByCache(String cacheName, Object key, String sql, Object... paras) {
		ICache cache = getConfig().getCache();
		List<M> result = cache.get(cacheName, key);
		if (result == null) {
			result = find(sql, paras);
			cache.put(cacheName, key, result);
		}
		return result;
	}
	
	/**
	 * @see #findByCache(String, Object, String, Object...)
	 */
	public List<M> findByCache(String cacheName, Object key, String sql) {
		return findByCache(cacheName, key, sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Find first model by cache. I recommend add "limit 1" in your sql.
	 * @see #findFirst(String, Object...)
	 * @param cacheName the cache name
	 * @param key the key used to get data from cache
	 * @param sql an SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param paras the parameters of sql
	 */
	public M findFirstByCache(String cacheName, Object key, String sql, Object... paras) {
		ICache cache = getConfig().getCache();
		M result = cache.get(cacheName, key);
		if (result == null) {
			result = findFirst(sql, paras);
			cache.put(cacheName, key, result);
		}
		return result;
	}
	
	/**
	 * @see #findFirstByCache(String, Object, String, Object...)
	 */
	public M findFirstByCache(String cacheName, Object key, String sql) {
		return findFirstByCache(cacheName, key, sql, NULL_PARA_ARRAY);
	}
	
	/**
	 * Paginate by cache.
	 * @see #paginate(int, int, String, String, Object...)
	 * @param cacheName the cache name
	 * @param key the key used to get date from cache
	 * @return Page
	 */
	public Page<M> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		return doPaginateByCache(cacheName, key, pageNumber, pageSize, null, select, sqlExceptSelect, paras);
	}
	
	/**
	 * @see #paginateByCache(String, Object, int, int, String, String, Object...)
	 */
	public Page<M> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return doPaginateByCache(cacheName, key, pageNumber, pageSize, null, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	public Page<M> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		return doPaginateByCache(cacheName, key, pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
	}
	
	private Page<M> doPaginateByCache(String cacheName, Object key, int pageNumber, int pageSize, Boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
		ICache cache = getConfig().getCache();
		Page<M> result = cache.get(cacheName, key);
		if (result == null) {
			result = doPaginate(pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
			cache.put(cacheName, key, result);
		}
		return result;
	}
	
	/**
	 * Return attribute names of this model.
	 */
	public String[] _getAttrNames() {
		Set<String> attrNameSet = attrs.keySet();
		return attrNameSet.toArray(new String[attrNameSet.size()]);
	}
	
	/**
	 * Return attribute values of this model.
	 */
	public Object[] _getAttrValues() {
		java.util.Collection<Object> attrValueCollection = attrs.values();
		return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
	}
	
	/**
	 * Return json string of this model.
	 */
	public String toJson() {
		return com.jfinal.kit.JsonKit.toJson(attrs);
	}
	
	private Class<? extends Model> getUsefulClass() {
		Class c = getClass();
		return c.getName().indexOf("EnhancerByCGLIB") == -1 ? c : c.getSuperclass();	// com.demo.blog.Blog$$EnhancerByCGLIB$$69a17158
	}
	
	/**
	 * filter () 方法将被 save()、update() 调用，可用于过滤类似于 XSS 攻击脚本
	 * @param filterBy 0 表示当前正被 save() 调用, 1 表示当前正被 update() 调用
	 */
	protected void filter(int filterBy) {
		
	}
	
	public String getSql(String key) {
		return getConfig().getSqlKit().getSql(key);
	}
	
	/**
	 * 可以在模板中利用 Model 自身的属性参与动态生成 sql，例如：
	 * select * from user where nickName = #(nickName)
	 * new Account().setNickName("James").getSqlPara(...)
	 * 
	 * 注意：由于 dao 对象上的 attrs 不允许读写，不要调用其 getSqlPara(String) 方法
	
	public SqlPara getSqlPara(String key) {
		return getSqlPara(key, this.attrs);
	} */
	
	public SqlPara getSqlPara(String key, Model model) {
		return getSqlPara(key, model.attrs);
	}
	
	public SqlPara getSqlPara(String key, Map data) {
		return getConfig().getSqlKit().getSqlPara(key, data);
	}
	
	public SqlPara getSqlPara(String key, Object... paras) {
		return getConfig().getSqlKit().getSqlPara(key, paras);
	}
	
	public List<M> find(SqlPara sqlPara) {
		return find(sqlPara.getSql(), sqlPara.getPara());
	}
	
	public M findFirst(SqlPara sqlPara) {
		return findFirst(sqlPara.getSql(), sqlPara.getPara());
	}
	
	public Page<M> paginate(int pageNumber, int pageSize, SqlPara sqlPara) {
		String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
		return doPaginate(pageNumber, pageSize, null, sqls[0], sqls[1], sqlPara.getPara());
	}
}


