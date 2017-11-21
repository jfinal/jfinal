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

import java.text.DecimalFormat;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * #number 数字格式化输出指令
 * 
 * 两种用法：
 * 1：#number(n) 用默认 pattern 输出变量中的值
 * 2：#number(n, "#.##") 用第二个参数指定的 pattern 输出变量中的值
 * 
 * 注意：
 * 1：pattern 的使用与 java.text.DecimalFormat 的完全一样
 *    在拿不定主意的时候可以在搜索引擎中搜索关键字：DecimalFormat
 * 2：#number 指令中的参数可以是变量，例如：#number(n, p) 中的 n 与 p 可以全都是变量
 * 
 * <pre>
 * 示例：
 * #number(3.1415926, "#.##")
 * #number(0.9518, "#.##%")
 * #number(300000, "光速为每秒 ,### 公里。")
 * 
 * #set(n = 1.234)
 * #set(p = "#.##")
 * #number(n, p)
 * </pre>
 */
public class NumberDirective extends Directive {
	
	private Expr valueExpr;
	private Expr patternExpr;
	private int paraNum;
	
	public void setExprList(ExprList exprList) {
		this.paraNum = exprList.length();
		if (paraNum == 0) {
			throw new ParseException("The parameter of #number directive can not be blank", location);
		}
		if (paraNum > 2) {
			throw new ParseException("Wrong number parameter of #number directive, two parameters allowed at most", location);
		}
		
		if (paraNum == 1) {
			this.valueExpr = exprList.getExpr(0);
			this.patternExpr = null;
		} else if (paraNum == 2) {
			this.valueExpr = exprList.getExpr(0);
			this.patternExpr = exprList.getExpr(1);
		}
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Object value = valueExpr.eval(scope);
		if (value == null) {
			return ;
		}
		
		if (paraNum == 1) {
			outputWithoutPattern(writer, value);
		} else if (paraNum == 2) {
			outputWithPattern(scope, writer, value);
		}
	}
	
	private void outputWithoutPattern(Writer writer, Object value) {	
		String ret = new DecimalFormat().format(value);
		write(writer, ret);
	}
	
	private void outputWithPattern(Scope scope, Writer writer, Object value) {
		Object pattern = patternExpr.eval(scope);
		if ( !(pattern instanceof String) ) {
			throw new TemplateException("The sencond parameter pattern of #number directive must be String", location);
		}
		
		String ret = new DecimalFormat((String)pattern).format(value);
		write(writer, ret);
	}
}





