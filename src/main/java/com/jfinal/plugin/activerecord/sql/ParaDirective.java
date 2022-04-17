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

import java.lang.reflect.Array;
import java.util.Collection;
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
 *   user.template("find", Kv.of("nickName", "prettyGirl").set("age", 18)).find();
 * 
 * 3：以上用法会在 #para(expr) 处生成问号占位字符，并且实际的参数放入 SqlPara 对象的参数列表中
 * 
 * 
 * 二、参数为 int 型数字的用法
 * 1：模板内容
 *   #sql("find")
 *     select * from user where id > #para(0) and id < #para(1)
 *   #end
 *   
 * 2： java 代码
 *   user.template("find", 10, 100).find();
 * 
 * 3：以上用法会在 #para(0) 与 #para(1) 处生成问号占位字符，并且将 10、100 这两个参数放入
 *    SqlPara 对象的参数列表中
 * 
 * 三、4.9.23 新增支持 like、in 子句
 *    ### 一般用法，第二个参数传入 "like"、"in" 参数即可
 *    select * from t title like #para(title, "like")
 *    select * from t title like #para(title, "in")
 *    
 *    ### like 类型第一个参数支持 int 类型
 *    select * from t title like #para(0, "like")
 *    
 *    ### like 支持左侧与右侧百分号用法
 *    select * from t title like #para(title, "%like")
 *    select * from t title like #para(title, "like%")
 *    
 *    ### 警告：对于 in 子句，如果 #para 第一个参数是 int 型，并且 java 代码针对 Object... 参数传入的是数组
 *    select * from t id in #para(0, "in")
 *    ### 那么 java 代码中要将 Object... 处的参数强制转成 Object，否则参数传递不正确
 *    Integer[] idArray = {1, 2, 3};
      Db.template("findByIdArray", (Object)idArray).find();
 * 
 * </pre>
 */
public class ParaDirective extends Directive {
	
	private int index = -1;
	private String paraName = null;
	private static boolean checkParaAssigned = true;
	
	// 支持 like、in 子句
	private int type = 0;
	private static final int TYPE_LIKE = 1;
	private static final int TYPE_LIKE_LEFT = 2;
	private static final int TYPE_LIKE_RIGHT = 3;
	private static final int TYPE_IN = 4;
	
	public static void setCheckParaAssigned(boolean checkParaAssigned) {
		ParaDirective.checkParaAssigned = checkParaAssigned;
	}
	
	public void setExprList(ExprList exprList) {
		if (exprList.length() == 0) {
			throw new ParseException("The parameter of #para directive can not be blank", location);
		}
		
		Expr expr = exprList.getExpr(0);
		if (expr instanceof Const && ((Const)expr).isInt()) {
			index = ((Const)expr).getInt();
			if (index < 0) {
				throw new ParseException("The index of para array must greater than -1", location);
			}
		}
		
        if (exprList.length() > 1) {
            expr = exprList.getExpr(1);
            if (expr instanceof Const && ((Const)expr).isStr()) {
                String typeStr = ((Const)expr).getStr();
                if ("like".equalsIgnoreCase(typeStr) || "%like%".equalsIgnoreCase(typeStr)) {
                    type = TYPE_LIKE;
                } else if ("%like".equalsIgnoreCase(typeStr)) {
                    type = TYPE_LIKE_LEFT;
                } else if ("like%".equalsIgnoreCase(typeStr)) {
                    type = TYPE_LIKE_RIGHT;
                } else if ("in".equalsIgnoreCase(typeStr)) {
                    type = TYPE_IN;
                } else {
                    throw new ParseException("The type of para must be: like, %like, like%, in. Not support : " + typeStr, location);
                }
            }
        }
		
		if (checkParaAssigned && exprList.getExpr(0) instanceof Id) {
			Id id = (Id)exprList.getExpr(0);
			paraName = id.getId();
		}
		
		this.exprList = exprList;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		SqlPara sqlPara = (SqlPara)scope.get(SqlKit.SQL_PARA_KEY);
		if (sqlPara == null) {
			throw new TemplateException("#para directive invoked by getSqlPara(...) method only", location);
		}
		
		if (index == -1) {
			// #para(paraName) 中的 paraName 没有赋值时抛出异常
			// issue: https://jfinal.com/feedback/1832
			if (checkParaAssigned && paraName != null && !scope.exists(paraName)) {
				throw new TemplateException("The parameter \""+ paraName +"\" must be assigned", location);
			}
			
			handleSqlPara(writer, sqlPara, exprList.getExpr(0).eval(scope));
		} else {
			Object[] paras = (Object[])scope.get(SqlKit.PARA_ARRAY_KEY);
			if (paras == null) {
				throw new TemplateException("The #para(" + index + ") directive must invoked by template(String, Object...) or getSqlPara(String, Object...) method", location);
			}
			if (index >= paras.length) {
				throw new TemplateException("The index of #para directive is out of bounds: " + index, location);
			}
			
			handleSqlPara(writer, sqlPara, paras[index]);
		}
	}
	
    private void handleSqlPara(Writer writer, SqlPara sqlPara, Object value) {
        if (type == 0) {
            write(writer, "?");
            sqlPara.addPara(value);
        } else if (type == TYPE_LIKE) {
            write(writer, "?");
            sqlPara.addPara("%" + value + "%");
        } else if (type == TYPE_LIKE_LEFT) {
            write(writer, "?");
            sqlPara.addPara("%" + value);
        } else if (type == TYPE_LIKE_RIGHT) {
            write(writer, "?");
            sqlPara.addPara(value + "%");
        } else if (type == TYPE_IN) {
            if (value instanceof Collection) {
                handleCollection(writer, sqlPara, (Collection<?>)value);
            } else if (value != null && value.getClass().isArray()) {
                handleArray(writer, sqlPara, value);
            } else {
                write(writer, "(?)");
                sqlPara.addPara(value);
            }
        }
    }
    
    private void handleCollection(Writer writer, SqlPara sqlPara, Collection<?> collection) {
        write(writer, "(");
        boolean first = true;
        for (Object element : collection) {
            if (first) {
                first = false;
                write(writer, "?");
            } else {
                write(writer, ", ?");
            }
            sqlPara.addPara(element);
        }
        write(writer, ")");
    }
    
    private void handleArray(Writer writer, SqlPara sqlPara, Object array) {
        write(writer, "(");
        int size = Array.getLength(array);
        for (int i=0; i<size; i++) {
            if (i == 0) {
                write(writer, "?");
            } else {
                write(writer, ", ?");
            }
            sqlPara.addPara(Array.get(array, i));
        }
        write(writer, ")");
    }
}



