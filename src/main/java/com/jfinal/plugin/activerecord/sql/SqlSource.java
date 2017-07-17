package com.jfinal.plugin.activerecord.sql;

import com.jfinal.template.source.ISource;

/**
 * 封装 sql 模板源
 */
class SqlSource {
	
	String file;
	ISource source;
	
	SqlSource(String file) {
		this.file = file;
		this.source = null;
	}
	
	SqlSource(ISource source) {
		this.file = null;
		this.source = source;
	}
	
	boolean isFile() {
		return file != null;
	}
}



