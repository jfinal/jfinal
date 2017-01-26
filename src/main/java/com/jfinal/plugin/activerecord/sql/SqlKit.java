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

package com.jfinal.plugin.activerecord.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

/**
 * SqlKit
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class SqlKit {
	
	static final String SQL_KIT_KEY = "_SQL_KIT_";
	static final String SQL_PARA_KEY = "_SQL_PARA_";
	
	private String configName;
	private boolean devMode;
	private Engine engine;
	private List<String> sqlTemplateList = new ArrayList<String>();
	private Map<String, Template> sqlAst = new HashMap<String, Template>();
	
	private Map<String, Template> mapForDevMode = new HashMap<String, Template>();
	
	public SqlKit(String configName, boolean devMode) {
		this.configName = configName;
		this.devMode = devMode;
		
		engine =  new Engine(configName);
		engine.setDevMode(devMode);
		
		engine.addDirective("namespace", new NameSpaceDirective());
		engine.addDirective("sql", new SqlDirective());
		engine.addDirective("para", new ParaDirective());
		engine.addDirective("p", new ParaDirective());		// 配置 #para 指令的别名指令 #p
	}
	
	public SqlKit(String configName) {
		this(configName, false);
	}
	
	public Engine getEngine() {
		return engine;
	}
	
	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
		engine.setDevMode(devMode);
	}
	
	public void setBaseSqlTemplatePath(String baseSqlTemplatePath) {
		engine.setBaseTemplatePath(baseSqlTemplatePath);
	}
	
	public void addSqlTemplate(String sqlTemplate) {
		if (StrKit.isBlank(sqlTemplate)) {
			throw new IllegalArgumentException("sqlTemplate can not be blank");
		}
		sqlTemplateList.add(sqlTemplate);
	}
	
	public void parseTemplate() {
		for (String st : sqlTemplateList) {
			Template template = engine.getTemplate(st);
			Map<Object, Object> data = new HashMap<Object, Object>();
			data.put(SQL_KIT_KEY, this);
			template.renderToString(data);
			if (devMode) {
				mapForDevMode.put(st, template);
			}
		}
	}
	
	void put(String key, Template template) throws Exception {
		if (sqlAst.containsKey(key)) {
			throw new Exception("Sql already exists with key : " + key);
		}
		sqlAst.put(key, template);
	}
	
	public String getSql(String key, Map data) {
		if (devMode) {
			reloadModifiedTemplate();
		}
		Template template = sqlAst.get(key);
		return template != null ? template.renderToString(data) : null;
	}
	
	public SqlPara getSqlPara(String key, Map data) {
		SqlPara sqlPara = new SqlPara();
		data.put(SQL_PARA_KEY, sqlPara);
		String sql = getSql(key, data);
		sqlPara.setSql(sql);
		return sqlPara;
	}
	
	/**
	 * 利用 Engine 已有的机制实现被修改模板文件的重加载
	 * 只需对比 Engine 中缓存的 Template 与 SqlKit 中缓存的 Template 地址值即可
	 */
	private void reloadModifiedTemplate() {
		for (String st : sqlTemplateList) {
			if (mapForDevMode.get(st) != engine.getTemplate(st)) {
				mapForDevMode.clear();
				sqlAst.clear();
				engine.removeAllTemplateCache();
				parseTemplate();
				return ;
			}
		}
	}
	
	public String toString() {
		return "SqlKit for config : " + configName;
	}
}





