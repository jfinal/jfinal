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

package com.jfinal.template.ext.directive;

import java.io.Writer;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.stat.Scope;

/**
 * 输出随机数
 */
public class Random extends Directive {
	
	private java.util.Random random = new java.util.Random();
	
	public void exec(Env env, Scope scope, Writer writer) {
		write(writer, String.valueOf(random.nextInt()));
	}
}



