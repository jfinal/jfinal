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
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Output 输出指令
 * 
 * 用法：
 * 1：#(value)
 * 2：#(x = 1, y = 2, x + y)
 * 3：#(seoTitle ?? 'JFinal 极速开发社区')
 */
public class Output extends Stat {
	
	private ExprList exprList;
	
	public Output(ExprList exprList, Location location) {
		if (exprList.length() == 0) {
			throw new ParseException("The expression of output directive like #(expression) can not be blank", location);
		}
		this.exprList = exprList;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Object value = exprList.eval(scope);
		if (value != null) {
			write(writer, value.toString());
		}
	}
}




