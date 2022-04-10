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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * DaoTemplate
 * 
 * <pre>
 * 例子：
 * model.template("find", 123).find();
 * </pre>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DaoTemplate<M extends Model> {
	
	protected Model<M> dao;
	protected SqlPara sqlPara;
	
	public DaoTemplate(Model dao, String key, Map<?, ?> data) {
		this.dao = dao;
		this.sqlPara = dao.getSqlPara(key, data);
	}
	
	public DaoTemplate(Model dao, String key, Object... paras) {
		this.dao = dao;
		this.sqlPara = dao.getSqlPara(key, paras);
	}
	
	public DaoTemplate(boolean byString, Model dao, String content, Map<?, ?> data) {
		this.dao = dao;
		this.sqlPara = dao.getSqlParaByString(content, data);
	}
	
	public DaoTemplate(boolean byString, Model dao, String content, Object... paras) {
		this.dao = dao;
		this.sqlPara = dao.getSqlParaByString(content, paras);
	}
	
	public SqlPara getSqlPara() {
		return sqlPara;
	}
	
	// ---------
	
	public List<M> find() {
		return dao.find(sqlPara);
	}
	
	public M findFirst() {
		return dao.findFirst(sqlPara);
	}
	
	public Page<M> paginate(int pageNumber, int pageSize) {
		return dao.paginate(pageNumber, pageSize, sqlPara);
	}
	
	public Page<M> paginate(int pageNumber, int pageSize, boolean isGroupBySql) {
		return dao.paginate(pageNumber, pageSize, isGroupBySql, sqlPara);
	}
	
	// ---------
	
	public void each(Function<M, Boolean> func) {
		dao.each(func, sqlPara.getSql(), sqlPara.getPara());
	}
	
	// ---------
	
	public List<M> findByCache(String cacheName, Object key) {
		return dao.findByCache(cacheName, key, sqlPara.getSql(), sqlPara.getPara());
	}
	
	public M findFirstByCache(String cacheName, Object key) {
		return dao.findFirstByCache(cacheName, key, sqlPara.getSql(), sqlPara.getPara());
	}
	
	public Page<M> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize) {
		String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
		return dao.paginateByCache(cacheName, key, pageNumber, pageSize, sqls[0], sqls[1], sqlPara.getPara());
	}
	
	public Page<M> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, boolean isGroupBySql) {
		String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
		return dao.paginateByCache(cacheName, key, pageNumber, pageSize, isGroupBySql, sqls[0], sqls[1], sqlPara.getPara());
	}
}



