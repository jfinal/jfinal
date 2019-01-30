/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
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

import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Switch
 * 
 * #switch 指令与 Java 12 switch 新特性的设计相似: http://openjdk.java.net/jeps/325
 * 
 * 在与 java 老版本指令基本用法相同的基础上，主要变化与特性有：
 * 1: 移除 java 语法中的 fall-through semantics，即不需要 break 关键字进行断开
 * 2: 不引入 #break 指令，代码更少、更优雅
 * 3: #case 参数可使用多个用逗号分隔的表达式，每个表达式求值后与 #switch 参数求值后比较，
 *    从根本上消除了 #break 指令的必要性
 * 4: #case 支持任意类型数据与表达式（java 语言只支持少数常量类型）
 * 
 * <pre>
 * 示例：
 *   #switch (month)
 *     #case (1, 3, 5, 7, 8, 10, 12)
 *       #(month) 月有 31 天
 *     #case (2)
 *       #(month) 月平年有28天，闰年有29天
 *     #default
 *       月份错误: #(month ?? "null")
 *   #end
 * 
 * 如上代码所示，如果 #case 指令参数有多个值，那么可以用逗号分隔，
 * 上述逗号表达式的值 1, 3, 5, 7, 8, 10, 12 之中只要有一个与
 * switch 指令参数 month 相等的话，该 case 分支就会被执行，
 * 该特性从根本上消灭了 #break 指令的必要性
 * 
 * 
 * 除了常量值以外 #case 参数还可以是任意表达式
 * 例如：
 *     #case (a, b, c, x + y, obj.method(z))
 *     
 * 上述代码中 #case 参数中的所有表达式先会被求值，然后逐一与 #switch
 * 参数进行对比，同样也是只要有一个对比相等，则该 case 分支就会被执行
 * 
 * </pre>
 */
public class Switch extends Stat implements CaseSetter {
	
	private Expr expr;
	private Case nextCase;
	private Default _default;
	
	public Switch(ExprList exprList, Location location) {
		if (exprList.length() == 0) {
			throw new ParseException("The parameter of #switch directive can not be blank", location);
		}
		this.expr = exprList.getActualExpr();
	}
	
	public void setNextCase(Case nextCase) {
		this.nextCase = nextCase;
	}
	
	public void setDefault(Default _default, Location location) {
		if (this._default != null) {
			throw new ParseException("The #default case of #switch is already defined", location);
		}
		this._default = _default;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Object switchValue = expr.eval(scope);
		
		if (nextCase != null && nextCase.execIfMatch(switchValue, env, scope, writer)) {
			return ;
		}
		
		if (_default != null) {
			_default.exec(env, scope, writer);
		}
	}
	
	public boolean hasEnd() {
		return true;
	}
}







