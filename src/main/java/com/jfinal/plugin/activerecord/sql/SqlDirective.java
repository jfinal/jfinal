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
import java.util.Map;
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
		if (exprList.length() == 0) {
			throw new ParseException("The parameter of #sql directive can not be blank", location);
		}
		if (exprList.length() > 1) {
			throw new ParseException("Only one parameter allowed for #sql directive", location);
		}
		Expr expr = exprList.getExpr(0);
		if (expr instanceof Const && ((Const)expr).isStr()) {
		} else {
			throw new ParseException("The parameter of #sql directive must be String", location);
		}
		
		this.id = ((Const)expr).getStr();
	}
	
	@SuppressWarnings("unchecked")
	public void exec(Env env, Scope scope, Writer writer) {
		String nameSpace = (String)scope.get(NameSpaceDirective.NAME_SPACE_KEY);
		String key = StrKit.isBlank(nameSpace) ? id : nameSpace + "." + id;
		Map<String, Template> sqlTemplateMap = (Map<String, Template>)scope.get(SqlKit.SQL_TEMPLATE_MAP_KEY);
		if (sqlTemplateMap.containsKey(key)) {
			throw new ParseException("Sql already exists with key : " + key, location);
		}
		
		sqlTemplateMap.put(key, new Template(env, stat));
	}
	
	public boolean hasEnd() {
		return true;
	}
}



