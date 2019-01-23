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

package com.jfinal.template.ext.directive;

import java.util.ArrayList;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.Const;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;
import com.jfinal.template.stat.ast.Define;

/**
 * CallDirective 动态调用模板函数
 * 
 * 模板函数的名称与参数都可以动态指定，提升模板函数调用的灵活性
 * 
 * 例如：
 *     #call(funcName, p1, p2, ..., pn)
 *     其中 funcName，为函数名，p1、p2、pn 为被调用函数所使用的参数
 * 
 * 
 * 如果希望模板函数不存在时忽略其调用，添加常量值 true 在第一个参数位置即可
 * 例如：
 *     #call(true, funcName, p1, p2, ..., pn)
 * 
 * 
 * TODO 后续优化看一下 ast.Call.java
 */
public class CallDirective extends Directive {
	
	protected Expr funcNameExpr;
	protected ExprList paraExpr;
	
	protected boolean nullSafe = false;		// 是否支持函数名不存在时跳过
	
	public void setExprList(ExprList exprList) {
		int len = exprList.length();
		if (len == 0) {
			throw new ParseException("模板函数名不能缺失", location);
		}
		
		int index = 0;
		Expr expr = exprList.getExpr(index);
		if (expr instanceof Const && ((Const)expr).isBoolean()) {
			if (len == 1) {
				throw new ParseException("模板函数名不能缺失", location);
			}
			
			nullSafe = ((Const)expr).getBoolean();
			index++;
		}
		
		funcNameExpr = exprList.getExpr(index++);
		
		ArrayList<Expr> list = new ArrayList<Expr>();
		for (int i=index; i<len; i++) {
			list.add(exprList.getExpr(i));
		}
		paraExpr = new ExprList(list);
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Object funcNameValue = funcNameExpr.eval(scope);
		if (funcNameValue == null) {
			if (nullSafe) {
				return ;
			}
			throw new TemplateException("模板函数名为 null", location);
		}
		
		if (!(funcNameValue instanceof String)) {
			throw new TemplateException("模板函数名必须是字符串", location);
		}
		
		Define func = env.getFunction(funcNameValue.toString());
		
		if (func == null) {
			if (nullSafe) {
				return ;
			}
			throw new TemplateException("模板函数未找到 : " + funcNameValue, location);
		}
		
		func.call(env, scope, paraExpr, writer);
	}
}




