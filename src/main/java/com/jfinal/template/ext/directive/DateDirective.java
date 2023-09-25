/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

import java.time.temporal.Temporal;
import java.util.Date;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * #date 日期格式化输出指令
 *
 * 三种用法：
 * 1：#date(createAt) 用默认 datePattern 配置，输出 createAt 变量中的日期值
 * 2：#date(createAt, "yyyy-MM-dd HH:mm:ss") 用第二个参数指定的 datePattern，输出 createAt 变量中的日期值
 * 3：#date() 用默认 datePattern 配置，输出 “当前” 日期值
 *
 * 注意：
 * 1：#date 指令中的参数可以是变量，例如：#date(d, p) 中的 d 与 p 可以全都是变量
 * 2：默认 datePattern 可通过 Engine.setDatePattern(...) 进行配置
 * 3：jfinal 4.9.02 版新增支持 java 8 的 LocalDateTime、LocalDate、LocalTime
 */
public class DateDirective extends Directive {

	private Expr dateExpr;
	private Expr patternExpr;

	public void setExprList(ExprList exprList) {
		int paraNum = exprList.length();
		if (paraNum == 0) {
			this.dateExpr = null;
			this.patternExpr = null;
		} else if (paraNum == 1) {
			this.dateExpr = exprList.getExpr(0);
			this.patternExpr = null;
		} else if (paraNum == 2) {
			this.dateExpr = exprList.getExpr(0);
			this.patternExpr = exprList.getExpr(1);
		} else {
			throw new ParseException("Wrong number parameter of #date directive, two parameters allowed at most", location);
		}
	}

	public void exec(Env env, Scope scope, Writer writer) {
		Object date;
		String pattern;

		if (dateExpr != null) {
			date = dateExpr.eval(scope);
		} else {
			date = new Date();
		}

		if (patternExpr != null) {
			Object temp = patternExpr.eval(scope);
			if (temp instanceof String) {
				pattern = (String)temp;
			} else {
				throw new TemplateException("The second parameter datePattern of #date directive must be String", location);
			}
		} else {
			pattern = env.getEngineConfig().getDatePattern();
		}

		write(date, pattern, writer);
	}

	private void write(Object date, String pattern, Writer writer) {
		try {

			if (date instanceof Date) {
				writer.write((Date)date, pattern);
			} else if (date instanceof Temporal) {		// 输出 LocalDateTime、LocalDate、LocalTime
				writer.write((Temporal)date, pattern);
			} else if (date != null) {
				throw new TemplateException("The first parameter of #date directive can not be " + date.getClass().getName(), location);
			}

		} catch (TemplateException | ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
}



