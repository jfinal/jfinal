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
import com.jfinal.template.EngineConfig;
import com.jfinal.template.Env;
import com.jfinal.template.FileStringSource;
import com.jfinal.template.expr.ast.Assign;
import com.jfinal.template.expr.ast.Const;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.Ctrl;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Parser;
import com.jfinal.template.stat.Scope;

/**
 * Include
 * 
 * 1：父模板被缓存时，被 include 的模板会被间接缓存，无需关心缓存问题
 * 2：同一个模板文件被多个父模板 include，所处的背景环境不同，例如各父模板中定义的模板函数不同
 * 各父模板所处的相对路径不同，所以多个父模板不能共用一次 parse 出来的结果，而是在每个被include
 * 的地方重新 parse
 * 
 * <pre>
 * 两种用法：
 * 1：只传入一个参数，参数必须是 String 常量，如果希望第一个参数是变量可以使用 #render 指令去实现
 *   #include("_hot.html")
 *   
 * 2：传入任意多个参数，除第一个参数以外的所有参数必须是赋值表达式，用于实现参数传递功能
 *   #include("_hot.html", title = "热门新闻", list = newsList)
 *   
 *   上例中传递了 title、list 两个参数，可以代替父模板中的 #set 指令传参方式
 *   并且此方式传入的参数只在子模板作用域有效，不会污染父模板作用域
 *   
 *   这种传参方式有利于将子模板模块化，例如上例的调用改成如下的参数：
 *   #include("_hot.html", title = "热门项目", list = projectList)
 *   通过这种传参方式在子模板 _hot.html 之中，完全不需要修改对于 title 与 list
 *   这两个变量的处理代码，就实现了对 “热门项目” 数据的渲染
 * </pre>
 */
public class Include extends Stat {
	
	private Assign[] assignArray;
	private Stat stat;
	
	public Include(Env env, ExprList exprList, String parentFileName, Location location) {
		int len = exprList.length();
		if (len == 0) {
			throw new ParseException("The parameter of #include directive can not be blank", location);
		}
		// 第一个参数必须为 String 类型
		Expr expr = exprList.getExpr(0);
		if (expr instanceof Const && ((Const)expr).isStr()) {
		} else {
			throw new ParseException("The first parameter of #include directive must be String", location); 
		}
		// 其它参数必须为赋值表达式
		if (len > 1) {
			for (int i = 1; i < len; i++) {
				if (!(exprList.getExpr(i) instanceof Assign)) {
					throw new ParseException("The " + i + "th parameter of #include directive must be an assignment expression", location);
				}
			}
		}
		
		parseSubTemplate(env, ((Const)expr).getStr(), parentFileName, location);
		getAssignExpression(exprList);
	}
	
	private void parseSubTemplate(Env env, String fileName, String parentFileName, Location location) {
		String subFileName = getSubFileName(fileName, parentFileName);
		EngineConfig config = env.getEngineConfig();
		FileStringSource fileStringSource = new FileStringSource(config.getBaseTemplatePath(), subFileName, config.getEncoding());
		try {
			Parser parser = new Parser(env, fileStringSource.getContent(), subFileName);
			if (config.isDevMode()) {
				env.addStringSource(fileStringSource);
			}
			this.stat = parser.parse();
		} catch (Exception e) {
			// 文件路径不正确抛出异常时添加 location 信息
			throw new ParseException(e.getMessage(), location, e);
		}
	}
	
	/**
	 * 获取在父模板之下子模板的最终文件名，子模板目录相对于父模板文件目录来确定
	 * 以 "/" 打头则以 baseTemplatePath 为根，否则以父文件所在路径为根
	 */
	public static String getSubFileName(String fileName, String parentFileName) {
		if (parentFileName == null) {
			return fileName;
		}
		if (fileName.startsWith("/")) {
			return fileName;
		}
		int index = parentFileName.lastIndexOf('/');
		if (index == -1) {
			return fileName;
		}
		return parentFileName.substring(0, index + 1) + fileName;
	}
	
	private void getAssignExpression(ExprList exprList) {
		int len = exprList.length();
		if (len > 1) {
			assignArray = new Assign[len - 1];
			for (int i = 0; i < assignArray.length; i++) {
				assignArray[i] = (Assign)exprList.getExpr(i + 1);
			}
		} else {
			assignArray = null;
		}
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		scope = new Scope(scope);
		if (assignArray != null) {
			evalAssignExpression(scope);
		}
		stat.exec(env, scope, writer);
		scope.getCtrl().setJumpNone();
	}
	
	private void evalAssignExpression(Scope scope) {
		Ctrl ctrl = scope.getCtrl();
		try {
			ctrl.setLocalAssignment();
			for (Assign assign : assignArray) {
				assign.eval(scope);
			}
		} finally {
			ctrl.setWisdomAssignment();
		}
	}
}







