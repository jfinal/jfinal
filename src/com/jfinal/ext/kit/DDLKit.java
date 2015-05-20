/**
 * DDL making Kit
 */
package com.jfinal.ext.kit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author 朱丛启  2015年5月19日 上午11:11:08
 *
 */
public class DDLKit {
	
	public static final String BIGINT = "bigint";
	public static final String VARCHAR = "varchar";
	public static final String TEXT = "text";
	public static final String INT = "int";
	public static final String TINYINT = "tinyint";
	
	public static class Table{
		// 表名
		private String tableName = null;
		// 表注释
		private String comment = null;
		// 表的所有列
		HashMap<String, Column> columnsMap = new HashMap<String, DDLKit.Column>();
		// 表的所有primary key
		private List<Column> primaryKeys = new ArrayList<Column>();
		// 表的所有 unique Key
		private List<Column> uniqueKeys = new ArrayList<Column>();
		
		private Table(String tableName, String comment){
			this.tableName = tableName;
			if (!comment.startsWith("`")) {
				comment = "'" + comment;
			}
			if (!comment.endsWith("`")) {
				comment += "'";
			}
			this.comment = comment;
		}
		
		/**
		 * 添加一列到表中
		 * @param column ， 列
		 * @return 返回添加列之后的Table
		 */
		public Table addColumn(Column column){
			if (null == column) {
				return this;
			}
			if (!this.columnsMap.containsKey(column.columnName)) {
				this.columnsMap.put(column.columnName, column);
			}
			if (column.primaryKey) {
				this.primaryKeys.add(column);
			}
			if (column.uniqueKey) {
				this.uniqueKeys.add(column);
			}
			return this;
		}

		/**
		 * 生成DDL
		 * @return ddl
		 */
		public String ddl(){
			StringBuilder sqlBuilder = new StringBuilder();
			//start ddl
			sqlBuilder.append("CREATE TABLE `").append(this.tableName).append("` (\n");
			sqlBuilder.append("`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '索引',\n");
			// all columns
			Iterator<String> keyIterator = this.columnsMap.keySet().iterator();
			while (keyIterator.hasNext()) {
				Object key = keyIterator.next();
				sqlBuilder.append(this.columnsMap.get(key).ddl())
						.append(",\n");
			}
			// all primary keys
			sqlBuilder.append("PRIMARY KEY (`id`");
			for (Column column : this.primaryKeys) {
				sqlBuilder.append(",").append(column.columnName);
			}
			sqlBuilder.append(")");
			// all unique keys
			for (Column column : this.uniqueKeys) {
				sqlBuilder.append(",\nUNIQUE KEY").append(column.columnName).append("(").append(column.columnName).append(")");
			}
			//end ddl
			sqlBuilder.append("\n) COMMENT=").append(this.comment).append(";");
			return sqlBuilder.toString();
		}
	}
	
	public static class Column{
		// 列名
		private String columnName = null;
		// 列类型
		private String columnType = null;
		// 列大小
		private int columnSize = 10;
		// 默认值
		private StringBuilder defaultValue = new StringBuilder();
		// 列注释
		private String comment = null;
		// 是否primarykey
		private boolean primaryKey = false;
		// 是否uniquekey
		private boolean uniqueKey = false;
		
		/**
		 * 初始化Column
		 * @param columnName 列名
		 * @param columnType 列类型
		 * @param comment 列注释
		 * @param columnSize 列大小
		 * @param defaultValue 默认值
		 * @param primaryKey 是否为primary key
		 * @param uniqueKey 是否为unique key
		 */
		public Column(String columnName, String columnType, String comment,
				int columnSize, String defaultValue, 
				boolean primaryKey, boolean uniqueKey){
			if (!columnName.startsWith("`")) {
				columnName = "`" + columnName;
			}
			if (!columnName.endsWith("`")) {
				columnName += "`";
			}
			this.columnName = columnName;
			this.columnType = columnType;
			
			
			
			if (null == defaultValue) {
				this.defaultValue.append("DEFAULT NULL");
			}else{
				String value = "".equals(defaultValue)?"''":"'"+defaultValue+"'";
				this.defaultValue.append("NOT NULL DEFAULT ").append(value);
			}
			
			if (!comment.startsWith("`")) {
				comment = "'" + comment;
			}
			if (!comment.endsWith("`")) {
				comment += "'";
			}
			this.comment = comment;
			this.columnSize = columnSize;
			this.primaryKey = primaryKey;
			this.uniqueKey = uniqueKey;
		}
		
		/**
		 * 生成Primary Key
		 * @param columnName 列名
		 * @param columnType 列类型
		 * @param columnSize 列的大小
		 * @return
		 */
		public Column primaryKeyColumn(String columnName, String columnType, String comment, int columnSize, String defalutValue){
			return (new Column(columnName, columnType, comment, columnSize, defalutValue, true, false));
		}
		
		/**
		 * 生成 unique Key
		 * @param columnName 列名
		 * @param columnType 列类型
		 * @param columnSize 列的大小
		 * @return
		 */
		public Column uniqueKeyColumn(String columnName, String columnType, String comment, int columnSize, String defalutValue){
			return (new Column(columnName, columnType, comment, columnSize, defalutValue, false, true));
		}

		/**
		 * column DDL
		 * @return
		 */
		public String ddl(){
			return new StringBuilder()
					.append(this.columnName).append(" ").append(this.columnType)
					.append("(").append(this.columnSize).append(") ").append(this.defaultValue)
					.append(" COMMENT ").append(this.comment).toString();
		}
	}
	
	public static Table createTable(String tableName, String comment){
		return (new Table(tableName, comment));
	}
}
