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

package com.jfinal.template.stat.ast;

import java.io.Writer;
import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.Assign;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Set 赋值，从内向外作用域查找变量，找到则替换变量值，否则在顶层作用域赋值
 * 
 * 用法：
 * 1：#set(k = v)
 * 2：#set(k1 = v1, k2 = v2, ..., kn = vn)
 * 3：#set(x = 1+2)
 * 4：#set(x = 1+2, y = 3>4, ..., z = c ? a : b)
 */
public class Set extends Stat {
	
	private ExprList exprList;
	
	public Set(ExprList exprList, Location location) {
		if (exprList.length() == 0) {
			throw new ParseException("The parameter of #set directive can not be blank", location);
		}
		
		for (Expr expr : exprList.getExprArray()) {
			if ( !(expr instanceof Assign) ) {
				throw new ParseException("#set directive only supports assignment expressions", location);
			}
		}
		this.exprList = exprList;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		scope.getCtrl().setWisdomAssignment();
		exprList.eval(scope);
	}
}

