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
import java.util.HashMap;
import java.util.Map;
import com.jfinal.template.Directive;
import com.jfinal.template.EngineConfig;
import com.jfinal.template.Env;
import com.jfinal.template.FileStringSource;
import com.jfinal.template.IStringSource;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.Assign;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.Ctrl;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Parser;
import com.jfinal.template.stat.Scope;
import com.jfinal.template.stat.ast.Define;
import com.jfinal.template.stat.ast.Include;
import com.jfinal.template.stat.ast.Stat;

/**
 * #render 指令用于动态渲染子模板，作为 include 指令的补充
 * 
 * <pre>
 * 两种用法：
 * 1：只传入一个参数，参数可以是 String 常量，也可以是任意表达式
 *   #render("_hot.html")
 *   #render(subFile)
 *   
 * 2：传入任意多个参数，除第一个参数以外的所有参数必须是赋值表达式，用于实现参数传递功能
 *   #render("_hot.html", title = "热门新闻", list = newsList)
 *   
 *   上例中传递了 title、list 两个参数，可以代替父模板中的 #set 指令传参方式
 *   并且此方式传入的参数只在子模板作用域有效，不会污染父模板作用域
 *   
 *   这种传参方式有利于将子模板模块化，例如上例的调用改成如下的参数：
 *   #render("_hot.html", title = "热门项目", list = projectList)
 *   通过这种传参方式在子模板 _hot.html 之中，完全不需要修改对于 title 与 list
 *   这两个变量的处理代码，就实现了对 “热门项目” 数据的渲染
 *   
 * </pre>
 */
public class RenderDirective extends Directive {
	
	private String parentFileName;
	private Map<String, StatInfo> statInfoCache = new HashMap<String,StatInfo>();
	
	public void setExprList(ExprList exprList) {
		int len = exprList.length();
		if (len == 0) {
			throw new ParseException("The parameter of #render directive can not be blank", location);
		}
		if (len > 1) {
			for (int i = 1; i < len; i++) {
				if (!(exprList.getExpr(i) instanceof Assign)) {
					throw new ParseException("The " + i + "th parameter of #render directive must be an assignment expression", location);
				}
			}
		}
		
		/**
		 * 从 location 中获取父模板的 fileName，用于生成 subFileName
		 * 如果是孙子模板，那么 parentFileName 为最顶层的模板，而非直接上层的模板
		 */
		this.parentFileName = location.getTemplateFile();
		this.exprList = exprList;
	}
	
	/**
	 * 对 exprList 进行求值，并将第一个表达式的值作为模板名称返回，
	 * 开启 local assignment 保障 #render 指令参数表达式列表
	 * 中的赋值表达式在当前 scope 中进行，有利于模块化
	 */
	private Object evalAssignExpressionAndGetFileName(Scope scope) {
		Ctrl ctrl = scope.getCtrl();
		try {
			ctrl.setLocalAssignment();
			return exprList.evalExprList(scope)[0];
		} finally {
			ctrl.setWisdomAssignment();
		}
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		// 在 exprList.eval(scope) 之前创建，使赋值表达式在本作用域内进行
		scope = new Scope(scope);
		
		Object value = evalAssignExpressionAndGetFileName(scope);
		if (!(value instanceof String)) {
			throw new TemplateException("The parameter value of #render directive must be String", location);
		}
		
		String subFileName = Include.getSubFileName((String)value, parentFileName);
		StatInfo statInfo = statInfoCache.get(subFileName);
		if (statInfo == null) {
			statInfo = parseStatInfo(env, subFileName);
			statInfoCache.put(subFileName, statInfo);
		} else if (env.getEngineConfig().isDevMode()) {
			// statInfo.env.isStringSourceModified() 逻辑可以支持 #render 子模板中的 #include 过来的子模板在 devMode 下在修改后可被重加载
			if (statInfo.stringSource.isModified() || statInfo.env.isStringSourceListModified()) {
				statInfo = parseStatInfo(env, subFileName);
				statInfoCache.put(subFileName, statInfo);
			}
		}
		
		statInfo.stat.exec(statInfo.env, scope, writer);
		scope.getCtrl().setJumpNone();
	}
	
	private StatInfo parseStatInfo(Env env, String subFileName) {
		EngineConfig config = env.getEngineConfig();
		FileStringSource fileStringSource = new FileStringSource(config.getBaseTemplatePath(), subFileName, config.getEncoding());
		
		try {
			EnvSub envSub = new EnvSub(env);
			Stat stat = new Parser(envSub, fileStringSource.getContent(), subFileName).parse();
			return new StatInfo(envSub, stat, fileStringSource);
		} catch (Exception e) {
			throw new ParseException(e.getMessage(), location, e);
		}
	}
	
	private static class StatInfo {
		EnvSub env;
		Stat stat;
		IStringSource stringSource;
		
		StatInfo(EnvSub env, Stat stat, IStringSource stringSource) {
			this.env = env;
			this.stat = stat;
			this.stringSource = stringSource;
		}
	}
	
	/**
	 * EnvSub 用于将子模板与父模板中的模板函数隔离开来，
	 * 否则在子模板被修改并被重新解析时会再次添加子模板中的
	 * 模板函数，从而抛出异常
	 * 
	 * EnvSub 也可以使子模板中定义的模板函数不与上层产生冲突，
	 * 有利于动态型模板渲染的模块化
	 * 
	 * 注意： #render 子模板中定义的模板函数无法被上层调用
	 */
	private static class EnvSub extends Env {
		Env parentEnv;
		
		public EnvSub(Env parentEnv) {
			super(parentEnv.getEngineConfig());
			this.parentEnv = parentEnv;
		}
		
		/**
		 * 接管父类 getFunction()，先从子模板中找模板函数，找不到再去父模板中找
		 */
		public Define getFunction(String functionName) {
			Define func = functionMap.get(functionName);
			return func != null ? func : parentEnv.getFunction(functionName);
		}
	}
}




