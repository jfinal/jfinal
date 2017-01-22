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

import com.jfinal.template.stat.Ctrl;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * NullSafe
 * 在原则上只支持具有动态特征的用法，例如：方法调用、字段取值、Map 与 List 取值
 * 而不支持具有静态特征的用法，例如：static method 调用、shared method 调用
 * 
 * 用法：
 * #( seoTitle ?? "JFinal 极速开发社区" )
 * 支持级联：  #( a.b.c ?? "JFinal 极速开发社区" )
 * 支持嵌套：  #( a ?? b ?? c ?? d)
 */
public class NullSafe extends Expr {
	
	private Expr left;
	private Expr right;
	
	public NullSafe(Expr left, Expr right, Location location) {
		if (left == null) {
			throw new ParseException("The expression on the left side of null coalescing and safe access operator \"??\" can not be blank", location);
		}
		this.left = left;
		this.right = right;
		this.location = location;
	}
	
	public Object eval(Scope scope) {
		Ctrl ctrl = scope.getCtrl();
		boolean oldNullSafeValue = ctrl.isNullSafe();
		
		Object ret;
		try {
			ctrl.setNullSafe(true);
			ret = left.eval(scope);
		} finally {
			ctrl.setNullSafe(oldNullSafeValue);
		}
		
		return ret == null && right != null ? right.eval(scope) : ret;
	}
}






