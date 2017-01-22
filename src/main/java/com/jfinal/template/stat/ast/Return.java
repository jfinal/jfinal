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
import com.jfinal.template.stat.Scope;

/**
 * Return
 * 通常用于 #define 指令内部，不支持返回值
 */
public class Return  extends Stat {
	
	public static final Return me = new Return();
	
	private Return() {
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		scope.getCtrl().setReturn();
	}
}







