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

package com.jfinal.template;

import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.ast.Stat;

/**
 * Directive 供用户继承并扩展自定义指令，具体用法可以参考
 * com.jfinal.template.ext.directive 包下面的例子
 */
public abstract class Directive extends Stat {
	
	/**
	 * 传递给指令的表达式列表
	 * 1：表达式列表可通过 exprList.eval(scope) 以及 exprList.evalExprList(scope) 进行求值
	 * 2:使用赋值表达式可实现参数传递功能
	 * 
	 * <pre>
	 * 例如：#render("_hot.html", title="热门新闻", list=newsList)
	 * </pre>
	 */
	protected ExprList exprList;
	
	/**
	 * 具有 #end 结束符的指令内部嵌套的所有内容，调用 stat.exec(env, scope, writer)
	 * 即可执行指令内部嵌入所有指令与表达式，如果指令没有 #end 结束符，该属性无效 
	 */
	protected Stat stat;
	
	/**
	 * 指令被解析时注入指令参数表达式列表，继承类可以通过覆盖此方法对参数长度和参数类型进行校验
	 */
	public void setExprList(ExprList exprList) {
		this.exprList = exprList;
	}
	
	/**
	 * 指令被解析时注入指令 body 内容，仅对于具有 #end 结束符的指令有效
	 */
	public void setStat(Stat stat) {
		this.stat = stat;
	}
}





