/**
 * Copyright (c) 2011-2021, James Zhan 詹波 (jfinal@126.com).
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
import java.util.List;
import java.util.Map;

/**
 * DbTemplate
 * 
 * <pre>
 * 例子：
 * Db.template("find", 123).find();
 * </pre>
 */
public class DbTemplate {
	
	protected DbPro db;
	protected SqlPara sqlPara;
	
	public DbTemplate(DbPro db, String key, Map<?, ?> data) {
		this.db = db;
		this.sqlPara = db.getSqlPara(key, data);
	}
	
	public DbTemplate(DbPro db, String key, Object... paras) {
		this.db = db;
		this.sqlPara = db.getSqlPara(key, paras);
	}
	
	public DbTemplate(boolean byString, DbPro db, String content, Map<?, ?> data) {
		this.db = db;
		this.sqlPara = db.getSqlParaByString(content, data);
	}
	
	public DbTemplate(boolean byString, DbPro db, String content, Object... paras) {
		this.db = db;
		this.sqlPara = db.getSqlParaByString(content, paras);
	}
	
	/*
	 * 下一版本根据需求强度考虑添加此方法
	 * TODO 这里要严格测试，因为没有 Map data 值，所以 getSqlPara(...) 不一定可以正常工作
	public DbTemplate(DbPro db, String key) {
		this.db = db;
		this.sqlPara = db.getSqlPara(key);
	}*/
	
	public List<Record> find() {
		return db.find(sqlPara);
	}
	
	public Record findFirst() {
		return db.findFirst(sqlPara);
	}
	
	public int update() {
		return db.update(sqlPara);
	}
	
	public Page<Record> paginate(int pageNumber, int pageSize) {
		return db.paginate(pageNumber, pageSize, sqlPara);
	}
	
	public Page<Record> paginate(int pageNumber, int pageSize, boolean isGroupBySql) {
		return db.paginate(pageNumber, pageSize, isGroupBySql, sqlPara);
	}
	
	// ---------
	
	public int delete() {
		return db.delete(sqlPara.getSql(), sqlPara.getPara());
	}
	
	public String queryStr() {
		return db.queryStr(sqlPara.getSql(), sqlPara.getPara());
	}
	
	public Integer queryInt() {
		return db.queryInt(sqlPara.getSql(), sqlPara.getPara());
	}
	
	public Long queryLong() {
		return db.queryLong(sqlPara.getSql(), sqlPara.getPara());
	}
	
	public BigDecimal queryBigDecimal() {
		return db.queryBigDecimal(sqlPara.getSql(), sqlPara.getPara());
	}
	
	public BigInteger queryBigInteger() {
		return db.queryBigInteger(sqlPara.getSql(), sqlPara.getPara());
	}
	
	// ---------
	
	public <T> T queryColumn() {
		return db.queryColumn(sqlPara.getSql(), sqlPara.getPara());
	}
	
	public <T> List<T> query() {
		return db.query(sqlPara.getSql(), sqlPara.getPara());
	}
	
	public <T> T queryFirst() {
		return db.queryFirst(sqlPara.getSql(), sqlPara.getPara());
	}
	
	// ---------
	
	public List<Record> findByCache(String cacheName, Object key) {
		return db.findByCache(cacheName, key, sqlPara.getSql(), sqlPara.getPara());
	}
	
	public Record findFirstByCache(String cacheName, Object key) {
		return db.findFirstByCache(cacheName, key, sqlPara.getSql(), sqlPara.getPara());
	}
	
	public Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize) {
		String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
		return db.paginateByCache(cacheName, key, pageNumber, pageSize, sqls[0], sqls[1], sqlPara.getPara());
	}
	
	public Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, boolean isGroupBySql) {
		String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
		return db.paginateByCache(cacheName, key, pageNumber, pageSize, isGroupBySql, sqls[0], sqls[1], sqlPara.getPara());
	}
}


