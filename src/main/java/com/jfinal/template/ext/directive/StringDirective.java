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

package com.jfinal.template.ext.directive;

import java.io.Writer;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.FastStringWriter;
import com.jfinal.template.expr.ast.Const;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.expr.ast.Id;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * #string 指令方便定义大量的多行文本变量，这个是 java 语言中极为需要的功能
 * 
 * 定义：
 * #string(name)
 *    在此是大量的字符串
 * #end
 * 
 * 使用：
 * #(name)
 */
public class StringDirective extends Directive {
	
	private String name;
	private boolean isLocalAssignment = false;
	
	public void setExprList(ExprList exprList) {
		Expr[] exprArray = exprList.getExprArray();
		if (exprArray.length == 0) {
			throw new ParseException("#string directive parameter cant not be null", location);
		}
		if (exprArray.length > 2) {
			throw new ParseException("wrong number of #string directive parameter, two parameters allowed at most", location);
		}
		
		if (!(exprArray[0] instanceof Id)) {
			throw new ParseException("#string first parameter must be identifier", location);
		}
		this.name = ((Id)exprArray[0]).getId();
		if (exprArray.length == 2) {
			if (exprArray[1] instanceof Const) {
				if (((Const)exprArray[1]).isBoolean()) {
					this.isLocalAssignment = ((Const)exprArray[1]).getBoolean();
				} else {
					throw new ParseException("#string sencond parameter must be boolean", location);
				}
			}
		}
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		FastStringWriter fsw = new FastStringWriter();
		stat.exec(env, scope, fsw);
		
		if (this.isLocalAssignment) {
			scope.setLocal(name, fsw.toString());
		} else {
			scope.set(name, fsw.toString());
		}
	}
	
	/**
	 * hasEnd() 方法返回 true 时，表示该指令拥有指令体以及 #end 结束块
	 * 模板引擎在解析时会将 "指令体" 赋值到 stat 属性中，在 exec(...) 方法中
	 * 可通过 stat.exec(...) 执行 "指令体" 内部的所有指令
	 */
	public boolean hasEnd() {
		return true;
	}
}








