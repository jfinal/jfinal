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

import java.io.Writer;

import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.expr.ast.Id;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * ParaDirective
 * 
 * #p 与 #para 指令用于在 sql 模板中根据参数名生成问号占位以及查询参数
 * Example：
 * 1：模板内容
 *   #sql("find")
 *     select * from user where nickName = #p(nickName) and age > #p(age)
 *   #end
 *   
 * 2： java 代码
 *   SqlPara sp = getSqlPara("find", JMap.create("nickName", "prettyGirl").put("age", 18));
 *   user.find(sp)
 *   或者：
 *   user.find(sp.getSql(), sp.getPara());
 */
public class ParaDirective extends Directive {
	
	private Id id;
	
	public void setExprList(ExprList exprList) {
		Expr[] exprs = exprList.getExprArray();
		if (exprs.length == 0 || exprs.length > 1) {
			throw new ParseException("only one parameter allowed for #p or #para directive", location);
		}
		if (!(exprs[0] instanceof Id)) {
			throw new ParseException("the parameter of #p or #para directive must be identifier", location);
		}
		
		this.id = (Id)exprs[0];
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		SqlPara sqlPara = (SqlPara)scope.get(SqlKit.SQL_PARA_KEY);
		if (sqlPara == null) {
			throw new TemplateException("#p or #para directive invoked by getSqlPara(...) method only", location);
		}
		
		write(writer, "?");
		sqlPara.addPara(id.eval(scope));
	}
}



