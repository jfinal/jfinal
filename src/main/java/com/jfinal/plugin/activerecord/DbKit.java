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

import com.jfinal.kit.StrKit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * DbKit
 */
@SuppressWarnings("rawtypes")
public final class DbKit {
	
	public static final int DB_BATCH_COUNT = 1024;
	/**
	 * The main Config object for system
	 */
	static Config config = null;

	/**
	 * 1: For ActiveRecordPlugin.useAsDataTransfer(...) 用于分布式场景
	 * 2: For Model.getAttrsMap()/getModifyFlag() and Record.getColumnsMap()
	 * while the ActiveRecordPlugin not start or the Exception throws of HashSessionManager.restorSession(..) by Jetty
	 */
	static Config brokenConfig = Config.createBrokenConfig();

	private static Map<Class<? extends Model>, Config> modelToConfig = new HashMap<Class<? extends Model>, Config>(512, 0.5F);
	private static Map<String, Config> configNameToConfig = new HashMap<String, Config>(32, 0.25F);

	static final Object[] NULL_PARA_ARRAY = new Object[0];
	public static final String MAIN_CONFIG_NAME = "main";
	public static final int DEFAULT_TRANSACTION_LEVEL = Connection.TRANSACTION_REPEATABLE_READ;

	private DbKit() {}

	/**
	 * Add Config object
	 * @param config the Config contains DataSource, Dialect and so on
	 */
	public static void addConfig(Config config) {
		if (config == null) {
			throw new IllegalArgumentException("Config can not be null");
		}
		if (configNameToConfig.containsKey(config.getName())) {
			throw new IllegalArgumentException("Config already exists: " + config.getName());
		}

		configNameToConfig.put(config.getName(), config);

		/**
		 * Replace the main config if current config name is MAIN_CONFIG_NAME
		 */
		if (MAIN_CONFIG_NAME.equals(config.getName())) {
			DbKit.config = config;
			Db.init(DbKit.config.getName());
		}

		/**
		 * The configName may not be MAIN_CONFIG_NAME,
		 * the main config have to set the first comming Config if it is null
		 */
		if (DbKit.config == null) {
			DbKit.config = config;
			Db.init(DbKit.config.getName());
		}
	}

	public static Config removeConfig(String configName) {
		if (DbKit.config != null && DbKit.config.getName().equals(configName)) {
			// throw new RuntimeException("Can not remove the main config.");
			DbKit.config = null;
		}

		Db.removeDbProWithConfig(configName);
		return configNameToConfig.remove(configName);
	}

	static void addModelToConfigMapping(Class<? extends Model> modelClass, Config config) {
		modelToConfig.put(modelClass, config);
	}

	public static Config getConfig() {
		return config;
	}

	public static Config getConfig(String configName) {
		return configNameToConfig.get(configName);
	}

	public static Config getConfig(Class<? extends Model> modelClass) {
		return modelToConfig.get(modelClass);
	}

	static final void close(ResultSet rs, Statement st) throws SQLException {
		if (rs != null) {rs.close();}
		if (st != null) {st.close();}
	}

	static final void close(ResultSet rs) throws SQLException {
		if (rs != null) {rs.close();}
	}

	static final void close(Statement st) throws SQLException {
		if (st != null) {st.close();}
	}

	public static Set<Map.Entry<String, Config>> getConfigSet() {
		return configNameToConfig.entrySet();
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends Model> getUsefulClass(Class<? extends Model> modelClass) {
		// com.demo.blog.Blog$$EnhancerByCGLIB$$69a17158
		// return (Class<? extends Model>)((modelClass.getName().indexOf("EnhancerByCGLIB") == -1 ? modelClass : modelClass.getSuperclass()));
		// return (Class<? extends Model>)(modelClass.getName().indexOf("$$EnhancerBy") == -1 ? modelClass : modelClass.getSuperclass());
		String n = modelClass.getName();
		return (Class<? extends Model>)(n.indexOf("_$$_") > -1 || n.indexOf("$$Enhancer") > -1 ? modelClass.getSuperclass() : modelClass);
	}

	/**
	 * 原有框架方法更新只会取modelList第一个元素的字段状态，批量更新的SQL全部相同，只是参数值不同
	 * 本方法会根据modelList中所有元素，生成不同的SQL和参数，分批分别执行
	 * 自动过滤所有null值属性
	 *
	 * @param modelList
	 * @param batchSize
	 * @param db 使用的数据源，为空时使用默认
	 * @return
	 * @see ：https://jfinal.com/share/2629
	 */
	public static List<Integer> batchListUpdate(List<? extends Model> modelList, int batchSize,String db) {
		if (modelList == null || modelList.size() == 0)
			return new ArrayList<>();
		Map<String, BatchInfo> modelUpdateMap = new HashMap<>();

		for (Model model : modelList) {
			Set<String> modifyFlag = CPI.getModifyFlag(model);
			Config config = CPI.getConfig(model);
			Table table = TableMapping.me().getTable(model.getClass());
			String[] pKeys = table.getPrimaryKey();
			Map<String, Object> attrs = CPI.getAttrs(model);
			List<String> attrNames = new ArrayList<>();

			// the same as the iterator in Dialect.forModelSave() to ensure the order of the attrs
			for (Map.Entry<String, Object> e : attrs.entrySet()) {
				String attr = e.getKey();
				if (modifyFlag.contains(attr) && !config.getDialect().isPrimaryKey(attr, pKeys) && table.hasColumnLabel(attr))
					attrNames.add(attr);
			}
			for (String pKey : pKeys)
				attrNames.add(pKey);
			String columns = StrKit.join(attrNames.toArray(new String[attrNames.size()]), ",");
			BatchInfo updateInfo = modelUpdateMap.get(columns);
			if (updateInfo == null) {
				updateInfo = new BatchInfo();
				updateInfo.list = new ArrayList<>();
				StringBuilder sql = new StringBuilder();
				config.getDialect().forModelUpdate(TableMapping.me().getTable(model.getClass()), attrs, modifyFlag, sql, new ArrayList<>());
				updateInfo.sql = sql.toString();
				modelUpdateMap.put(columns, updateInfo);
			}
			updateInfo.list.add(model);
		}
		return batchModelList(modelList, batchSize,db, modelUpdateMap);
	}
	public static List<Integer> batchListUpdate(List<? extends Model> modelList) {
		return batchListUpdate(modelList,DB_BATCH_COUNT,null);
	}
	public static List<Integer> batchListUpdate(List<? extends Model> modelList,String db) {
		return batchListUpdate(modelList,DB_BATCH_COUNT,db);
	}

	private static List<Integer> batchModelList(List list, int batchSize, String db, Map<String, BatchInfo> modelUpdateMap) {
		List<Integer> ret = new ArrayList<>(list.size());
		DbPro dbPro;
		if(StrKit.isBlank(db)){
			dbPro = Db.use();
		}else{
			dbPro=Db.use(db);
		}
		//批量更新
		for (Map.Entry<String, BatchInfo> entry : modelUpdateMap.entrySet()) {
			int[] batch = dbPro.batch(entry.getValue().sql, entry.getKey(), entry.getValue().list, batchSize);
			for (int i : batch) {
				ret.add(i);
			}
		}
		return ret;
	}

	/**
	 * 原有框架方法更新只会取modelList第一个元素的字段状态，批量插入的SQL全部相同，只是参数值不同
	 * 本方法会根据modelList中所有元素，生成不同的SQL和参数，分批分别执行
	 * 自动过滤所有null值属性
	 *
	 * @param modelList
	 * @param batchSize
	 * @param db 使用的数据源，为空时使用默认
	 * @return
	 * @see ：https://jfinal.com/share/2629
	 */
	public static List<Integer> batchListSave(List<? extends Model> modelList, int batchSize, String db) {
		if (modelList == null || modelList.size() == 0)
			return new ArrayList<>();
		Map<String, BatchInfo> modelUpdateMap = new HashMap<>();

		for (Model model : modelList) {
			Config config = CPI.getConfig(model);
			Map<String, Object> attrs = CPI.getAttrs(model);
			int index = 0;
			StringBuilder columns = new StringBuilder();
			// the same as the iterator in Dialect.forModelSave() to ensure the order of the attrs
			for (Map.Entry<String, Object> e : attrs.entrySet()) {
				if (index++ > 0) {
					columns.append(',');
				}
				columns.append(e.getKey());
			}
			String cs = columns.toString();
			BatchInfo batchInfo = modelUpdateMap.get(cs);
			if (batchInfo == null) {
				batchInfo = new BatchInfo();
				batchInfo.list = new ArrayList<>();
				StringBuilder sql = new StringBuilder();
				config.getDialect().forModelSave(TableMapping.me().getTable(model.getClass()), attrs, sql, new ArrayList());
				batchInfo.sql = sql.toString();
				modelUpdateMap.put(cs, batchInfo);
			}
			batchInfo.list.add(model);
		}
		return batchModelList(modelList, batchSize, db,modelUpdateMap);
	}
	public static List<Integer> batchListSave(List<? extends Model> modelList) {
		return batchListSave(modelList,DB_BATCH_COUNT,null);
	}
	public static List<Integer> batchListSave(List<? extends Model> modelList,String db) {
		return batchListSave(modelList,DB_BATCH_COUNT,db);
	}
	public static List<Integer> batchListSave(String tableName,List<? extends Record> recordList, int batchSize, String db) {
		if (recordList == null || recordList.size() == 0)
			return new ArrayList<>();
		Map<String, BatchInfo> updateMap = new HashMap<>();

		for (Record record : recordList) {
			Map<String, Object> attrs = record.getColumns();
			int index = 0;
			StringBuilder columns = new StringBuilder();
			// the same as the iterator in Dialect.forModelSave() to ensure the order of the attrs
			for (Map.Entry<String, Object> e : attrs.entrySet()) {
				if (index++ > 0) {
					columns.append(',');
				}
				columns.append(e.getKey());
			}
			String cs = columns.toString();
			BatchInfo batchInfo = updateMap.get(cs);
			if (batchInfo == null) {
				batchInfo = new BatchInfo();
				batchInfo.list = new ArrayList<>();
				StringBuilder sql = new StringBuilder();
				Db.use().getConfig().getDialect().forDbSave(tableName, new String[0], record, sql, new ArrayList<>());
				batchInfo.sql = sql.toString();
				updateMap.put(cs, batchInfo);
			}
			batchInfo.list.add(record);
		}
		return batchModelList(recordList, batchSize,db, updateMap);
	}
	public static List<Integer> batchListSave(String tableName,List<? extends Record> recordList) {
		return batchListSave(tableName,recordList,DB_BATCH_COUNT,null);
	}
	/**
	 * 设置IN查询的sql和参数
	 *
	 * @param paras
	 * @param sb
	 * @param inParas
	 * @return
	 */
	public static StringBuilder buildInSqlPara(List<Object> paras, StringBuilder sb, Object[] inParas) {
		sb.append("(");
		for (int i = 0; i < inParas.length; i++) {
			paras.add(inParas[i]);
			if (i < inParas.length - 1) {
				sb.append("?,");
			} else {
				sb.append("?)");
			}
		}
		return sb;
	}

	public static class BatchInfo {
		public String sql;
		public List list;
	}
}




