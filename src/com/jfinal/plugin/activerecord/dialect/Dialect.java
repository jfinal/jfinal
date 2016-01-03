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

package com.jfinal.plugin.activerecord.dialect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.Table;

/**
 * Dialect.
 */
public abstract class Dialect {
	
	// Methods for common
	public abstract String forTableBuilderDoBuild(String tableName);
	public abstract String forPaginate(int pageNumber, int pageSize, String sql);
	
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
	
	public boolean isOracle() {
		return false;
	}
	
	public boolean isTakeOverDbPaginate() {
		return false;
	}
	
	public Page<Record> takeOverDbPaginate(Connection conn, int pageNumber, int pageSize, String sql, Object... paras) throws SQLException {
		throw new RuntimeException("You should implements this method in " + getClass().getName());
	}
	
	public boolean isTakeOverModelPaginate() {
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	public Page takeOverModelPaginate(Connection conn, Class<? extends Model> modelClass, int pageNumber, int pageSize, String sql, Object... paras) throws Exception {
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
		
		// "(^\\s*select\\s+[\\s\\S]+?(\\s+|\\)))from\\b";
		// "(^\\s*select\\s+(\\s*\\S*\\([\\s\\S]+\\)\\s*as\\s+\\S+|\\s*\\S+(\\s+as\\s+\\S+)?)+?\\s*)from"
		private static final Pattern SELECT_PATTERN = Pattern.compile(
			// "(^\\s*select\\s+[\\s\\S]+?(\\s+|\\)))from",
			"(^\\s*select\\s+(\\s*\\S*\\([\\s\\S]+\\)\\s*as\\s+\\S+|\\s*\\S+(\\s+as\\s+\\S+)?)+?\\s*)from",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		
		private static final Pattern GROUP_BY_PATTERN = Pattern.compile(
			".+?\\s+group\\s+by\\s+.+?",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	}
	
	public String replaceOrderBy(String sql) {
		return Holder.ORDER_BY_PATTERN.matcher(sql).replaceAll("");
	}
	
	public String forTotalRow(Object[] actualSqlParas, String sql, Object... paras) {
		String sqlExceptSelect = getSqlExceptSelect(sql);
		if (sqlExceptSelect != null) {
			actualSqlParas[0] = sql;
			actualSqlParas[1] = paras;
			return "select count(*) " + replaceOrderBy(sqlExceptSelect);
		}
		
		// Compatible with paginate(1, 10, "select *", "from ...", a, b);
		if (paras != null && paras.length > 0 && paras[0] instanceof String) {
			sqlExceptSelect = (String)paras[0];
			actualSqlParas[0] = sql + " " + sqlExceptSelect;
			
			// Compatible with paginate(1, 10, "select *", "from ...", paras.toArray());
			if (paras.length == 2 && paras[1] instanceof Object[]) {
				actualSqlParas[1] = (Object[])paras[1];
			}
			else {
				Object[] newParas = new Object[paras.length - 1];
				for (int i=1; i<paras.length; i++) {
					newParas[i-1] = paras[i];
				}
				actualSqlParas[1] = newParas;
			}
			return "select count(*) " + replaceOrderBy(sqlExceptSelect);
		}
		
		throw new RuntimeException("Sql error : " + sql);
	}
	
	public String getSqlExceptSelect(String sql) {
		Matcher matcher = Holder.SELECT_PATTERN.matcher(sql);
		if (matcher.find()) {
			return sql.substring(matcher.end(1));
		} else {
			return null;
		}
	}
	
	/**
	 * 判断 sql 中是否带有 group by 子句
	 */
	public boolean isGroupBySql(String sql) {
		Matcher matcher = Holder.GROUP_BY_PATTERN.matcher(sql);
		return matcher.find();
	}
}






