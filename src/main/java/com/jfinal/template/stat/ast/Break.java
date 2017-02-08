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
 * Break
 * java 中 break、continue 可出现在 for 中的最后一行，不一定要套在 if 中
 */
public class Break extends Stat {
	
	public static final Break me = new Break();
	
	private Break() {
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		scope.getCtrl().setBreak();
	}
}



