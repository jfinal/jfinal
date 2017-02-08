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
import com.jfinal.template.expr.ast.Logic;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * ElseIf
 */
public class ElseIf extends Stat {
	
	private ExprList cond;
	private Stat stat;
	private Stat elseIfOrElse;
	
	public ElseIf(ExprList cond, Stat stat, Location location) {
		if (cond.length() == 0) {
			throw new ParseException("The condition expression of #elseif statement can not be blank", location);
		}
		this.cond = cond;
		this.stat = stat;
	}
	
	/**
	 * take over setStat(...) method of super class
	 */
	public void setStat(Stat elseIfOrElse) {
		this.elseIfOrElse = elseIfOrElse;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		if (Logic.isTrue(cond.eval(scope))) {
			stat.exec(env, scope, writer);
		} else if (elseIfOrElse != null) {
			elseIfOrElse.exec(env, scope, writer);
		}
	}
}





