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
 *   #switch (v)
 *      #case (1, 2, 3)
 *         ...
 *      #case (999)
 *         ...
 *      #case (expr)
 *         ...
 *      #default
 *         ...
 *   #end
 * 
 * 如上所示，#case 指令中可以用逗号分隔开多个值，上述逗号表达式的值 1、2、3 只要
 * 有一个与 switch 指定的参数 v 相等的话，该 case 分支就会被执行
 * 
 * 上述代码中的第三个 #case 指令中使用了 expr，也就是说 #case 指令除了支持常量以外
 * 还支持表达式
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







