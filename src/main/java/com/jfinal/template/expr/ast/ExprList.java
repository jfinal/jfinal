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

package com.jfinal.template.expr.ast;

import java.util.List;
import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Scope;

/**
 * ExprList
 */
public class ExprList extends Expr {
	
	public static final Expr[] NULL_EXPR_ARRAY = new Expr[0];
	public static final Object[] NULL_OBJECT_ARRAY =  new Object[0];
	public static final ExprList NULL_EXPR_LIST = new ExprList();
	
	private Expr[] exprArray;
	
	private ExprList() {
		this.exprArray = NULL_EXPR_ARRAY;
	}
	
	public ExprList(List<Expr> exprList) {
		if (exprList != null && exprList.size() > 0) {
			exprArray = exprList.toArray(new Expr[exprList.size()]);
		} else {
			exprArray = NULL_EXPR_ARRAY;
		}
	}
	
	public Expr[] getExprArray() {
		return exprArray;
	}
	
	public Expr getExpr(int index) {
		if (index < 0 || index >= exprArray.length) {
			throw new TemplateException("Index out of bounds: index = " + index + ", length = " + exprArray.length, location);
		}
		return exprArray[index];
	}
	
	public int length() {
		return exprArray.length;
	}
	
	/**
	 * 对所有表达式求值，只返回最后一个表达式的值
	 */
	public Object eval(Scope scope) {
		Object ret = null;
		for (Expr expr : exprArray) {
			ret = expr.eval(scope);
		}
		return ret;
	}
	
	/**
	 * 对所有表达式求值，并返回所有表达式的值
	 */
	public Object[] evalExprList(Scope scope) {
		if (exprArray.length == 0) {
			return NULL_OBJECT_ARRAY;
		}
		
		Object[] ret = new Object[exprArray.length];
		for (int i=0; i<exprArray.length; i++) {
			ret[i] = exprArray[i].eval(scope);
		}
		return ret;
	}
}



