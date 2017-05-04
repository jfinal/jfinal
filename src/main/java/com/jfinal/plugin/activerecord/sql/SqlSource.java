package com.jfinal.plugin.activerecord.sql;

import com.jfinal.template.IStringSource;

/**
 * 封装 sql 模板源
 */
class SqlSource {
	
	String file;
	IStringSource stringSource;
	
	SqlSource(String file) {
		this.file = file;
		this.stringSource = null;
	}
	
	SqlSource(IStringSource stringSource) {
		this.file = null;
		this.stringSource = stringSource;
	}
	
	boolean isFile() {
		return file != null;
	}
}



