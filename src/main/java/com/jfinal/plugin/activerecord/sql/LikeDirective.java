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

package com.jfinal.plugin.activerecord.sql;

import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.Const;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.expr.ast.Id;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * #like 指令用于在 sql 模板中根据参数名生成 like 子句问号占位以及查询参数
 * 基于 #para 指令修改而来
 *
 * 备忘：like 子句生成时参数值前后要添加百分号字符，例如："%" + value + "%"
 *       https://jfinal.com/doc/5-13
 *
 * <pre>
 * 一、参数为表达式的用法
 * 1：模板内容
 *   #sql("find")
 *     select * from user where #like(nickName)
 *   #end
 *
 * 2： java 代码
 *   user.template("find", Kv.of("nickName")).find();
 *
 * 3：以上用法会在 #like(expr) 处生成问号占位字符，并且实际的参数放入 SqlPara 对象的参数列表中
 *
 * 二、参数为 int 型数字的用法（#like 指令前方需要指定字段名）
 * 1：模板内容
 *   #sql("find")
 *     select * from user where nickName #like(0)
 *   #end
 *
 * 2： java 代码
 *   user.template("find", "james").find();
 *
 * 3：以上用法会在 #like(0) 处生成问号占位字符，并且将参数 "james" 放入 SqlPara 对象的参数列表中
 * </pre>
 */
public class LikeDirective extends Directive {

	private int index = -1;
	private String paraName = null;
	private static boolean checkParaAssigned = true;

	public static void setCheckParaAssigned(boolean checkParaAssigned) {
		LikeDirective.checkParaAssigned = checkParaAssigned;
	}

	public void setExprList(ExprList exprList) {
		if (exprList.length() == 0) {
			throw new ParseException("The parameter of #like directive can not be blank", location);
		}

		if (exprList.length() == 1) {
			Expr expr = exprList.getExpr(0);
			if (expr instanceof Const && ((Const)expr).isInt()) {
				index = ((Const)expr).getInt();
				if (index < 0) {
					throw new ParseException("The index of para array must greater than -1", location);
				}
			}
		}

		if (checkParaAssigned && exprList.getLastExpr() instanceof Id) {
			Id id = (Id)exprList.getLastExpr();
			paraName = id.getId();
		}

		this.exprList = exprList;
	}

	public void exec(Env env, Scope scope, Writer writer) {
		SqlPara sqlPara = (SqlPara)scope.get(SqlKit.SQL_PARA_KEY);
		if (sqlPara == null) {
			throw new TemplateException("#like directive invoked by getSqlPara(...) method only", location);
		}

		if (index == -1) {
			// #like(paraName) 中的 paraName 没有赋值时抛出异常
			// issue: https://jfinal.com/feedback/1832
			if (checkParaAssigned && paraName != null && !scope.exists(paraName)) {
				throw new TemplateException("The parameter \""+ paraName +"\" must be assigned", location);
			}

			write(writer, "like ?");
			sqlPara.addPara("%" + exprList.eval(scope) + "%");
		} else {
			Object[] paras = (Object[])scope.get(SqlKit.PARA_ARRAY_KEY);
			if (paras == null) {
				throw new TemplateException("The #like(" + index + ") directive must invoked by getSqlPara(String, Object...) method", location);
			}
			if (index >= paras.length) {
				throw new TemplateException("The index of #like directive is out of bounds: " + index, location);
			}

			write(writer, "like ?");
			sqlPara.addPara("%" + paras[index] + "%");
		}
	}
}



