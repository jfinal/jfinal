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

package com.jfinal.plugin.activerecord.dialect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.ModelBuilder;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.RecordBuilder;
import com.jfinal.plugin.activerecord.Table;

/**
 * Dialect.
 */
public abstract class Dialect {
	
	// Methods for common
	public abstract String forTableBuilderDoBuild(String tableName);
	public abstract String forPaginate(int pageNumber, int pageSize, StringBuilder findSql);
	
	// Methods for Model
	public abstract String forModelFindById(Table table, String columns);
	public abstract String forModelDeleteById(Table table);
	public abstract void forModelSave(Table table, Map<String, Object> attrs, StringBuilder sql, List<Object> paras);
	public abstract void forModelUpdate(Table table, Map<String, Object> attrs, Set<String> modifyFlag, StringBuilder sql, List<Object> paras);
	
	// Methods for DbPro. Do not delete the String[] pKeys parameter, the element of pKeys needs to trim()
	public abstract String forDbFindById(String tableName, String[] pKeys);
	public abstract String forDbDeleteById(String tableName, String[] pKeys);
	public abstract void forDbSave(String tableName, String[] pKeys, Record record, StringBuilder sql, List<Object> paras);
	public abstract void forDbUpdate(String tableName, String[] pKeys, Object[] ids, Record record, StringBuilder sql, List<Object> paras);
	
	/**
	 * 覆盖此方法，可以对 JDBC 到 java 数据类型进行定制化转换
	 * 不同数据库从 JDBC 到 java 数据类型的映射关系有所不同
	 * 
	 * 此外，还可以通过改变 ModelBuilder.buildLabelNamesAndTypes()
	 * 方法逻辑，实现下划线字段名转驼峰变量名的功能
	 */
	@SuppressWarnings("rawtypes")
	public <T> List<T> buildModelList(ResultSet rs, Class<? extends Model> modelClass) throws SQLException, InstantiationException, IllegalAccessException {
		return ModelBuilder.me.build(rs, modelClass);
	}
	
	/**
	 * 覆盖此方法，可以对 JDBC 到 java 数据类型进行定制化转换
	 * 不同数据库从 JDBC 到 java 数据类型的映射关系有所不同
	 * 
	 * 此外，还可以通过改变 RecordBuilder.buildLabelNamesAndTypes()
	 * 方法逻辑，实现下划线字段名转驼峰变量名的功能
	 */
	public List<Record> buildRecordList(Config config, ResultSet rs) throws SQLException {
		return RecordBuilder.me.build(config, rs);
	}
	
	public boolean isOracle() {
		return false;
	}
	
	public boolean isTakeOverDbPaginate() {
		return false;
	}
	
	public Page<Record> takeOverDbPaginate(Connection conn, int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, StringBuilder findSql, Object... paras) throws SQLException {
		throw new RuntimeException("You should implements this method in " + getClass().getName());
	}
	
	public boolean isTakeOverModelPaginate() {
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	public Page takeOverModelPaginate(Connection conn, Class<? extends Model> modelClass, int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, StringBuilder findSql, Object... paras) throws Exception {
		throw new RuntimeException("You should implements this method in " + getClass().getName());
	}
	
	public void fillStatement(PreparedStatement pst, List<Object> paras) throws SQLException {
		for (int i=0, size=paras.size(); i<size; i++) {
			pst.setObject(i + 1, paras.get(i));
		}
	}
	
	public void fillStatement(PreparedStatement pst, Object... paras) throws SQLException {
		for (int i=0; i<paras.length; i++) {
			pst.setObject(i + 1, paras[i]);
		}
	}
	
	public String getDefaultPrimaryKey() {
		return "id";
	}
	
	public boolean isPrimaryKey(String colName, String[] pKeys) {
		for (String pKey : pKeys) {
			if (colName.equalsIgnoreCase(pKey)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 一、forDbXxx 系列方法中若有如下两种情况之一，则需要调用此方法对 pKeys 数组进行 trim():
	 * 1：方法中调用了 isPrimaryKey(...)：为了防止在主键相同情况下，由于前后空串造成 isPrimaryKey 返回 false
	 * 2：为了防止 tableName、colName 与数据库保留字冲突的，添加了包裹字符的：为了防止串包裹区内存在空串
	 *   如 mysql 使用的 "`" 字符以及 PostgreSql 使用的 "\"" 字符
	 * 不满足以上两个条件之一的 forDbXxx 系列方法也可以使用 trimPrimaryKeys(...) 方法让 sql 更加美观，但不是必须
	 * 
	 * 二、forModelXxx 由于在映射时已经trim()，故不再需要调用此方法
	 */
	public void trimPrimaryKeys(String[] pKeys) {
		for (int i=0; i<pKeys.length; i++) {
			pKeys[i] = pKeys[i].trim();
		}
	}
	
	protected static class Holder {
		// "order\\s+by\\s+[^,\\s]+(\\s+asc|\\s+desc)?(\\s*,\\s*[^,\\s]+(\\s+asc|\\s+desc)?)*";
		private static final Pattern ORDER_BY_PATTERN = Pattern.compile(
			"order\\s+by\\s+[^,\\s]+(\\s+asc|\\s+desc)?(\\s*,\\s*[^,\\s]+(\\s+asc|\\s+desc)?)*",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	}
	
	public String replaceOrderBy(String sql) {
		return Holder.ORDER_BY_PATTERN.matcher(sql).replaceAll("");
	}
}






