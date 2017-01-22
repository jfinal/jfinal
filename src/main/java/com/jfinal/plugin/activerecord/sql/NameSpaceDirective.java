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
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.Const;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * NameSpaceDirective
 */
public class NameSpaceDirective extends Directive {
	
	static final String NAME_SPACE_KEY = "_NAME_SPACE_";
	
	private String nameSpace;
	
	public void setExprList(ExprList exprList) {
		Expr[] exprs = exprList.getExprArray();
		if (exprs.length == 0 || exprs.length > 1) {
			throw new ParseException("only one parameter allowed for #namespace directive", location);
		}
		if (!(exprs[0] instanceof Const) || !((Const)exprs[0]).isStr()) {
			throw new ParseException("the parameter of #namespace directive must be String", location);
		}
		
		this.nameSpace = ((Const)exprs[0]).getStr();
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		if (scope.get(NAME_SPACE_KEY) != null) {
			throw new TemplateException("#namespace directive can not be nested", location);
		}
		scope.set(NAME_SPACE_KEY, nameSpace);
		try {
			stat.exec(env, scope, writer);
		} finally {
			scope.remove(NAME_SPACE_KEY);
		}
	}
	
	public boolean hasEnd() {
		return true;
	}
}





