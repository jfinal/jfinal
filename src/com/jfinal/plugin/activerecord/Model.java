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
	
	private Set<String> getModifyFlag() {
		if (modifyFlag == null) {
			Config config = getConfig();
			if (config == null)
				modifyFlag = DbKit.brokenConfig.containerFactory.getModifyFlagSet();
			else
				modifyFlag = config.containerFactory.getModifyFlagSet();
		}
		return modifyFlag;
	}
	
	private Config getConfig() {
		return DbKit.getConfig(getClass());
	}
	
	private Table getTable() {
		return TableMapping.me().getTable(getClass());
	}
	
	/**
	 * Set attribute to model.
	 * @param attr the attribute name of the model
	 * @param value the value of the attribute
	 * @return this model
	 * @throws ActiveRecordException if the attribute is not exists of the model
	 */
	public M set(String attr, Object value) {
		if (getTable().hasColumnLabel(attr)) {
			attrs.put(attr, value);
			getModifyFlag().add(attr);	// Add modify flag, update() need this flag.
			return (M)this;
		}
		throw new ActiveRecordException("The attribute name is not exists: " + attr);
	}
	
	/**
	 * Put key value pair to the model when the key is not attribute of the model.
	 */
	public M put(String key, Object value) {
		attrs.put(key, value);
		return (M)this;
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
	 * @return Page
	 */
	public Page<M> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Config config = getConfig();
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
	
	private Page<M> paginate(Config config, Connection conn, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) throws Exception {
		if (pageNumber < 1 || pageSize < 1)
			throw new ActiveRecordException("pageNumber and pageSize must be more than 0");
		
		if (config.dialect.isTakeOverModelPaginate())
			return config.dialect.takeOverModelPaginate(conn, getClass(), pageNumber, pageSize, select, sqlExceptSelect, paras);
		
		long totalRow = 0;
		int totalPage = 0;
		List result = Db.query(config, conn, "select count(*) " + DbKit.replaceFormatSqlOrderBy(sqlExceptSelect), paras);
		int size = result.size();
		if (size == 1)
			totalRow = ((Number)result.get(0)).longValue();		// totalRow = (Long)result.get(0);
		else if (size > 1)
			totalRow = result.size();
		else
			return new Page<M>(new ArrayList<M>(0), pageNumber, pageSize, 0, 0);	// totalRow = 0;
		
		totalPage = (int) (totalRow / pageSize);
		if (totalRow % pageSize != 0) {
			totalPage++;
		}
		
		// --------
		StringBuilder sql = new StringBuilder();
		config.dialect.forPaginate(sql, pageNumber, pageSize, select, sqlExceptSelect);
		List<M> list = find(conn, sql.toString(), paras);
		return new Page<M>(list, pageNumber, pageSize, totalPage, (int)totalRow);
	}
	
	/**
	 * @see #paginate(int, int, String, String, Object...)
	 */
	public Page<M> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return paginate(pageNumber, pageSize, select, sqlExceptSelect, NULL_PARA_ARRAY);
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
	public Set<Entry<String, Object>> getAttrsEntrySet() {
		return attrs.entrySet();
	}
	
	/**
	 * Save model.
	 */
	public boolean save() {
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
				pst = conn.prepareStatement(sql.toString(), new String[]{table.getPrimaryKey()});
			else
				pst = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			
			config.dialect.fillStatement(pst, paras);
			result = pst.executeUpdate();
			getGeneratedKey(pst, table);
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
	private void getGeneratedKey(PreparedStatement pst, Table table) throws SQLException {
		String pKey = table.getPrimaryKey();
		if (get(pKey) == null || getConfig().dialect.isOracle()) {
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				Class colType = table.getColumnType(pKey);
				if (colType == Integer.class || colType == int.class)
					set(pKey, rs.getInt(1));
				else if (colType == Long.class || colType == long.class)
					set(pKey, rs.getLong(1));
				else
					set(pKey, rs.getObject(1));		// It returns Long object for int colType
				rs.close();
			}
		}
	}
	
	/**
	 * Delete model.
	 */
	public boolean delete() {
		Table table = getTable();
		Object id = attrs.get(table.getPrimaryKey());
		if (id == null)
			throw new ActiveRecordException("You can't delete model without id.");
		return deleteById(table, id);
	}
	
	/**
	 * Delete model by id.
	 * @param id the id value of the model
	 * @return true if delete succeed otherwise false
	 */
	public boolean deleteById(Object id) {
		if (id == null)
			throw new IllegalArgumentException("id can not be null");
		return deleteById(getTable(), id);
	}
	
	private boolean deleteById(Table table, Object id) {
		Config config = getConfig();
		Connection conn = null;
		try {
			conn = config.getConnection();
			String sql = config.dialect.forModelDeleteById(table);
			return Db.update(config, conn, sql, id) >= 1;
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
		if (getModifyFlag().isEmpty())
			return false;
		
		Table table = getTable();
		String pKey = table.getPrimaryKey();
		Object id = attrs.get(pKey);
		if (id == null)
			throw new ActiveRecordException("You can't update model without Primary Key.");
		
		Config config = getConfig();
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		config.dialect.forModelUpdate(table, attrs, getModifyFlag(), pKey, id, sql, paras);
		
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
		Class<? extends Model> modelClass = getClass();
		if (config.devMode)
			checkTableName(modelClass, sql);
		
		PreparedStatement pst = conn.prepareStatement(sql);
		config.dialect.fillStatement(pst, paras);
		ResultSet rs = pst.executeQuery();
		List<M> result = ModelBuilder.build(rs, modelClass);
		DbKit.closeQuietly(rs, pst);
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
	 * Check the table name. The table name must in sql.
	 */
	private void checkTableName(Class<? extends Model> modelClass, String sql) {
		Table table = TableMapping.me().getTable(modelClass);
		if (! sql.toLowerCase().contains(table.getName().toLowerCase()))
			throw new ActiveRecordException("The table name: " + table.getName() + " not in your sql.");
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
		List<M> result = find(sql, NULL_PARA_ARRAY);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Find model by id.
	 * @param id the id value of the model
	 */
	public M findById(Object id) {
		return findById(id, "*");
	}
	
	/**
	 * Find model by id. Fetch the specific columns only.
	 * Example: User user = User.dao.findById(15, "name, age");
	 * @param id the id value of the model
	 * @param columns the specific columns separate with comma character ==> ","
	 */
	public M findById(Object id, String columns) {
		Table table = getTable();
		String sql = getConfig().dialect.forModelFindById(table, columns);
		List<M> result = find(sql, id);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Set attributes with other model.
	 * @param model the Model
	 * @return this Model
	 */
	public M setAttrs(M model) {
		return setAttrs(model.getAttrs());
	}
	
	/**
	 * Set attributes with Map.
	 * @param attrs attributes of this model
	 * @return this Model
	 */
	public M setAttrs(Map<String, Object> attrs) {
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
		sb.append(super.toString()).append(" {");
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
	 * @param key the key used to get date from cache
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
	 * Paginate by cache.
	 * @see #paginate(int, int, String, String, Object...)
	 * @param cacheName the cache name
	 * @param key the key used to get date from cache
	 * @return Page
	 */
	public Page<M> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		ICache cache = getConfig().getCache();
		Page<M> result = cache.get(cacheName, key);
		if (result == null) {
			result = paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
			cache.put(cacheName, key, result);
		}
		return result;
	}
	
	/**
	 * @see #paginateByCache(String, Object, int, int, String, String, Object...)
	 */
	public Page<M> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		return paginateByCache(cacheName, key, pageNumber, pageSize, select, sqlExceptSelect, NULL_PARA_ARRAY);
	}
	
	/**
	 * Return attribute names of this model.
	 */
	public String[] getAttrNames() {
		Set<String> attrNameSet = attrs.keySet();
		return attrNameSet.toArray(new String[attrNameSet.size()]);
	}
	
	/**
	 * Return attribute values of this model.
	 */
	public Object[] getAttrValues() {
		java.util.Collection<Object> attrValueCollection = attrs.values();
		return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
	}
	
	/**
	 * Return json string of this model.
	 */
	public String toJson() {
		return com.jfinal.kit.JsonKit.toJson(attrs, 4);
	}
}


