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

package com.jfinal.template.stat;

import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.ast.Output;

/**
 * OutputDirectiveFactory
 * 用于定制自定义输出指令，替换系统默认输出指令，满足个性化需求
 * 
 * 用法：
 * 1：定义 MyOutput
 * public class MyOutput extends Output {
 *   public MyOutput(ExprList exprList) {
 *     super(exprList);
 *   }
 *   
 *   public void exec(Env env, Scope scope, Writer writer) {
 *     Object value = exprList.eval(scope);
 *     if (value != null) {
 *     	  write(writer, value.toString());
 *     }
 *   }
 * }
 * 
 * 2：定义 MyOutputDirectiveFactory
 * public class MyOutputDirectiveFactory extends OutputDirectiveFactory {
 *   public Output getOutputDirective(ExprList exprList) {
 *     return new MyOutput(exprList);
 *   }
 * }
 * 
 * 3：配置
 * engine.setOutputDirectiveFactory(new MyOutputDirectiveFactory())
 */
public class OutputDirectiveFactory {
	
	public static final OutputDirectiveFactory me = new OutputDirectiveFactory();
	
	public Output getOutputDirective(ExprList exprList, Location location) {
		return new Output(exprList, location);
	}
}


