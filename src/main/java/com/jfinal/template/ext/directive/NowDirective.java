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
import java.text.SimpleDateFormat;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * 输出当前时间，默认考虑是输出时间，给 pattern 输出可能是 Date、DateTime、Timestamp
 * 带 String 参数，表示 pattern
 */
public class NowDirective extends Directive {
	
	public void setExrpList(ExprList exprList) {
		if (exprList.length() > 1) {
			throw new ParseException("#now directive support one parameter only", location);	
		}
		super.setExprList(exprList);
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		String dataPattern;
		if (exprList.length() == 0) {
			dataPattern = env.getEngineConfig().getDatePattern();
		} else {
			Object dp = exprList.eval(scope);
			if (dp instanceof String) {
				dataPattern = (String)dp;
			} else {
				throw new TemplateException("The parameter of #new directive must be String", location);
			}
		}
		
		try {
			String value = new SimpleDateFormat(dataPattern).format(new java.util.Date());
			write(writer, value);
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
}



