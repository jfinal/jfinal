package com.jfinal.plugin.activerecord;

import java.util.List;
import java.util.Map;

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
}



