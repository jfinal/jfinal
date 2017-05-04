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

import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * StaticMethod : ID_list : '::' ID '(' exprList? ')'
 * 用法： com.jfinal.kit.Str::isBlank("abc")
 */
public class StaticMethod extends Expr {
	
	private Class<?> clazz;
	private String methodName;
	private ExprList exprList;
	
	public StaticMethod(String className, String methodName, Location location) {
		init(className, methodName, ExprList.NULL_EXPR_LIST, location);
	}
	
	public StaticMethod(String className, String methodName, ExprList exprList, Location location) {
		if (exprList == null || exprList.length() == 0) {
			throw new ParseException("exprList can not be blank", location);
		}
		init(className, methodName, exprList, location);
	}
	
	private void init(String className, String methodName, ExprList exprList, Location location) {
		try {
			this.clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new ParseException("Class not found: " + className, location, e);
		} catch (Exception e) {
			throw new ParseException(e.getMessage(), location, e);
		}
		this.methodName = methodName;
		this.exprList = exprList;
		this.location = location;
	}
	
	public Object eval(Scope scope) {
		Object[] argValues = exprList.evalExprList(scope);
		MethodInfo methodInfo;
		try {
			methodInfo = MethodKit.getMethod(clazz, methodName, argValues);
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
		
		// StaticMethod 是固定的存在，不支持 null safe，null safe 只支持具有动态特征的用法
		if (methodInfo == null) {
			throw new TemplateException(Method.buildMethodNotFoundSignature("public static method not found: " + clazz.getName() + "::", methodName, argValues), location);
		}
		if (!methodInfo.isStatic()) {
			throw new TemplateException(Method.buildMethodNotFoundSignature("Not public static method: " + clazz.getName() + "::", methodName, argValues), location);
		}
		
		try {
			return methodInfo.invoke(null, argValues);
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
}




