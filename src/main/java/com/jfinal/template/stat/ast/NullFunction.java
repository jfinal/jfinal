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

package com.jfinal.template.stat.ast;

import java.util.ArrayList;
import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Scope;

/**
 * NullFunction 辅助模板函数调用在同一个模板文件中只从 Env 中获取一次 function，
 * 从而提升性能，详情见 Call.exec(...) 中的使用
 */
public class NullFunction extends Define {
	
	public static final NullFunction me = new NullFunction();
	
	private NullFunction() {
		super("NullFunction can not be call", ExprList.NULL_EXPR_LIST, new StatList(new ArrayList<Stat>(0)), null);
	}
	
	@Override
	public void call(Env env, Scope scope, ExprList exprList, Writer writer) {
		throw new RuntimeException("NullFunction.call(...) 仅用于性能优化，永远不会被调用");
	}
	
	@Override
	public void exec(Env env, Scope scope, Writer writer) {
		throw new RuntimeException("NullFunction.exec(...) 仅用于性能优化，永远不会被调用");
	}
}





