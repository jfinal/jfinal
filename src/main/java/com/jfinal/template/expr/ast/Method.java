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

package com.jfinal.template.expr.ast;

import java.lang.reflect.InvocationTargetException;
import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Method : expr '.' ID '(' exprList? ')'
 * 
 * 每次通过 MethodKit.getMethod(...) 取 MethodInfo 而不是用属性持有其对象
 * 是为了支持 target 对象的动态类型，MethodInfo 中的 Method 被调用 15 次以后
 * 会被 JDK 动态生成 GeneratedAccessorXXX 字节码，性能不是问题
 * 唯一的性能损耗是从 HashMap 中获取 MethodInfo 对象，可以忽略不计
 * 
 * 如果在未来通过结合 #dynamic(boolean) 指令来优化，需要在 Ctrl 中引入一个
 * boolean dynamic = false 变量，而不能在 Env、Scope 引入该变量
 * 
 * 还需要引入一个 NullMethodInfo 以及 notNull() 方法，此优化复杂度提高不少，
 * 暂时不做此优化
 */
public class Method extends Expr {
	
	private Expr expr;
	private String methodName;
	private ExprList exprList;
	
	public Method(Expr expr, String methodName, ExprList exprList, Location location) {
		if (exprList == null || exprList.length() == 0) {
			throw new ParseException("The parameter of method can not be blank", location);
		}
		init(expr, methodName, exprList, location);
	}
	
	public Method(Expr expr, String methodName, Location location) {
		init(expr, methodName, ExprList.NULL_EXPR_LIST, location);
	}
	
	private void init(Expr expr, String methodName, ExprList exprList, Location location) {
		if (expr == null) {
			throw new ParseException("The target for method invoking can not be blank", location);
		}
		if (MethodKit.isForbiddenMethod(methodName)) {
			throw new ParseException("Forbidden method: " + methodName, location);
		}
		this.expr = expr;
		this.methodName = methodName;
		this.exprList = exprList;
		this.location = location;
	}
	
	public Object eval(Scope scope) {
		Object target = expr.eval(scope);
		if (target == null) {
			if (scope.getCtrl().isNullSafe()) {
				return null;
			}
			throw new TemplateException("The target for method invoking can not be null, method name: " + methodName, location);
		}
		
		Object[] argValues = exprList.evalExprList(scope);
		try {
			
			MethodInfo methodInfo = MethodKit.getMethod(target.getClass(), methodName, argValues);
			if (methodInfo != null) {
				return methodInfo.invoke(target, argValues);
			}
			
			if (scope.getCtrl().isNullSafe()) {
				return null;
			}
			throw new TemplateException(buildMethodNotFoundSignature("public method not found: " + target.getClass().getName() + ".", methodName, argValues), location);
			
		} catch (TemplateException | ParseException e) {
			throw e;
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t == null) {t = e;}
			throw new TemplateException(t.getMessage(), location, t);
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
	
	static String buildMethodNotFoundSignature(String preMsg, String methodName, Object[] argValues) {
		StringBuilder ret = new StringBuilder().append(preMsg).append(methodName).append("(");
		if (argValues != null) {
			for (int i = 0; i < argValues.length; i++) {
				if (i > 0) {
					ret.append(", ");
				}
				ret.append(argValues[i] != null ? argValues[i].getClass().getName() : "null");
			}
		}
		return ret.append(")").toString();
	}
	
	/*
	public static Object invokeVarArgsMethod(java.lang.reflect.Method method, Object target, Object[] argValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?>[] paraTypes = method.getParameterTypes();
		Object[] finalArgValues = new Object[paraTypes.length];
		
		int fixedParaLength = paraTypes.length - 1;
		System.arraycopy(argValues, 0, finalArgValues, 0, fixedParaLength);
		Class<?> varParaComponentType = paraTypes[paraTypes.length - 1].getComponentType();
		Object varParaValues = Array.newInstance(varParaComponentType, argValues.length - fixedParaLength);
		int p = 0;
		for (int i=fixedParaLength; i<argValues.length; i++) {
			Array.set(varParaValues, p++, argValues[i]);
		}
		finalArgValues[paraTypes.length - 1] = varParaValues;
		return method.invoke(target, finalArgValues);
	}*/
}





