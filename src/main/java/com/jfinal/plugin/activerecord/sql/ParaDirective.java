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

package com.jfinal.plugin.activerecord.sql;

import java.io.Writer;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.Const;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * #para 指令用于在 sql 模板中根据参数名生成问号占位以及查询参数
 * 
 * <pre>
 * 一、参数为表达式的用法
 * 1：模板内容
 *   #sql("find")
 *     select * from user where nickName = #para(nickName) and age > #para(age)
 *   #end
 *   
 * 2： java 代码
 *   SqlPara sp = getSqlPara("find", Kv.create("nickName", "prettyGirl").set("age", 18));
 *   user.find(sp)
 *   或者：
 *   user.find(sp.getSql(), sp.getPara());
 * 
 * 3：以上用法会在 #para(expr) 处生成问号占位字符，并且实际的参数放入 SqlPara 对象的参数列表中
 *   后续可以通过 sqlPara.getPara() 获取到参数并直接用于查询
 * 
 * 
 * 二、参数为 int 型数字的用法
 * 1：模板内容
 *   #sql("find")
 *     select * from user where id > #para(0) and id < #para(1)
 *   #end
 *   
 * 2： java 代码
 *   SqlPara sp = getSqlPara("find", 10, 100);
 *   user.find(sp)
 * 
 * 3：以上用法会在 #para(0) 与 #para(1) 处生成问号占位字符，并且将 10、100 这两个参数放入
 *    SqlPara 对象的参数列表中，后续可以通过 sqlPara.getPara() 获取到参数并直接用于查询
 * </pre>
 */
public class ParaDirective extends Directive {
	
	private int index = -1;
	
	public void setExprList(ExprList exprList) {
		if (exprList.length() == 1) {
			Expr expr = exprList.getExpr(0);
			if (expr instanceof Const && ((Const)expr).isInt()) {
				index = ((Const)expr).getInt();
				if (index < 0) {
					throw new ParseException("The index of para array must greater than -1", location);
				}
			}
		}
		this.exprList = exprList;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		SqlPara sqlPara = (SqlPara)scope.get(SqlKit.SQL_PARA_KEY);
		if (sqlPara == null) {
			throw new TemplateException("#para or #p directive invoked by getSqlPara(...) method only", location);
		}
		
		write(writer, "?");
		if (index == -1) {
			sqlPara.addPara(exprList.eval(scope));
		} else {
			Object[] paras = (Object[])scope.get(SqlKit.PARA_ARRAY_KEY);
			if (paras == null) {
				throw new TemplateException("The #para(" + index + ") directive must invoked by getSqlPara(String, Object...) method", location);
			}
			if (index >= paras.length) {
				throw new TemplateException("The index of #para directive is out of bounds: " + index, location);
			}
			sqlPara.addPara(paras[index]);
		}
	}
}



