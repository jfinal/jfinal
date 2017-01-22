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
import com.jfinal.template.stat.Ctrl;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * SetLocal 设置局部变量
 * 
 * 通常用于 #define #include 指令内部需要与外层作用域区分，以便于定义重用型模块的场景
 * 也常用于 #for 循环内部的临时变量
 */
public class SetLocal  extends Stat {
	
	final ExprList exprList;
	
	public SetLocal(ExprList exprList, Location location) {
		if (exprList.length() == 0) {
			throw new ParseException("The parameter of #setLocal directive can not be blank", location);
		}
		
		for (Expr expr : exprList.getExprArray()) {
			if ( !(expr instanceof Assign) ) {
				throw new ParseException("#setLocal directive only supports assignment expressions", location);
			}
		}
		this.exprList = exprList;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Ctrl ctrl = scope.getCtrl();
		try {
			ctrl.setLocalAssignment();
			exprList.eval(scope);
		} finally {
			ctrl.setWisdomAssignment();
		}
	}
}





