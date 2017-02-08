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

package com.jfinal.template.expr.ast;

import com.jfinal.template.stat.Scope;

/**
 * Id
 */
public class Id extends Expr {
	
	private String id;
	
	public Id(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public Object eval(Scope scope) {
		return scope.get(id);
	}
	
	public String toString() {
		return id;
	}
}


