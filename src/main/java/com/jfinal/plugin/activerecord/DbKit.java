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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * DbKit
 */
@SuppressWarnings("rawtypes")
public final class DbKit {
	
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
	
	private static Map<Class<? extends Model>, Config> modelToConfig = new HashMap<Class<? extends Model>, Config>();
	private static Map<String, Config> configNameToConfig = new HashMap<String, Config>();
	
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
			DbPro.init(DbKit.config.getName());
		}
		
		/**
		 * The configName may not be MAIN_CONFIG_NAME,
		 * the main config have to set the first comming Config if it is null
		 */
		if (DbKit.config == null) {
			DbKit.config = config;
			DbPro.init(DbKit.config.getName());
		}
	}
	
	public static Config removeConfig(String configName) {
		if (DbKit.config != null && DbKit.config.getName().equals(configName)) {
			// throw new RuntimeException("Can not remove the main config.");
			DbKit.config = null;
		}
		
		DbPro.removeDbProWithConfig(configName);
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
	
	static final void close(ResultSet rs, Statement st) {
		if (rs != null) {try {rs.close();} catch (SQLException e) {throw new ActiveRecordException(e);}}
		if (st != null) {try {st.close();} catch (SQLException e) {throw new ActiveRecordException(e);}}
	}
	
	static final void close(Statement st) {
		if (st != null) {try {st.close();} catch (SQLException e) {throw new ActiveRecordException(e);}}
	}
	
	public static Set<Map.Entry<String, Config>> getConfigSet() {
		return configNameToConfig.entrySet();
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Model> getUsefulClass(Class<? extends Model> modelClass) {
		// com.demo.blog.Blog$$EnhancerByCGLIB$$69a17158
		return (Class<? extends Model>)((modelClass.getName().indexOf("EnhancerByCGLIB") == -1 ? modelClass : modelClass.getSuperclass()));
	}
}




