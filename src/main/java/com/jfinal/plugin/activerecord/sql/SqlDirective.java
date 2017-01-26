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
import com.jfinal.kit.StrKit;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.Template;
import com.jfinal.template.expr.ast.Const;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * SqlDirective
 */
public class SqlDirective extends Directive {
	
	private String id;
	
	public void setExprList(ExprList exprList) {
		Expr[] exprs = exprList.getExprArray();
		if (exprs.length == 0 || exprs.length > 1) {
			throw new ParseException("only one parameter allowed for #sql directive", location);
		}
		if (!(exprs[0] instanceof Const) || !((Const)exprs[0]).isStr()) {
			throw new ParseException("the parameter of #sql directive must be String", location);
		}
		
		this.id = ((Const)exprs[0]).getStr();
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		String nameSpace = (String)scope.get(NameSpaceDirective.NAME_SPACE_KEY);
		String key = StrKit.notBlank(nameSpace) ? nameSpace + "." + id : id;
		SqlKit sqlKit = (SqlKit)scope.get(SqlKit.SQL_KIT_KEY);
		try {
			sqlKit.put(key, new Template(env, stat));
		} catch (Exception e) {
			throw new ParseException(e.getMessage(), location);
		}
	}
	
	public boolean hasEnd() {
		return true;
	}
}










