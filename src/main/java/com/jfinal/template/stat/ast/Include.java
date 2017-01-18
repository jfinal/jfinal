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
import com.jfinal.template.expr.ast.Const;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
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
 */
public class Include extends Stat {
	
	private Stat stat;
	
	public Include(Env env, ExprList exprList, String parentFileName, Location location) {
		if (exprList.length() != 1) {
			throw new ParseException("Only one paramether allowed by #include directive", location);
		}
		Expr expr = exprList.getExprArray()[0];
		if (expr instanceof Const && ((Const)expr).isStr()) {
		} else {
			throw new ParseException("The parameter of #include directive must be String", location); 
		}
		
		String fileName = ((Const)expr).getStr();
		String finalFileName = getFinalFileName(fileName, parentFileName);
		EngineConfig config = env.getEngineConfig();
		FileStringSource fileStringSource = new FileStringSource(config.getBaseTemplatePath(), finalFileName, config.getEncoding());
		if (config.isDevMode()) {
			env.addTemplateFinalFileName(fileStringSource.getFinalFileName(), fileName, location);
		}
		
		try {
			Parser parser = new Parser(env, fileStringSource.getContent(), finalFileName);
			this.stat = parser.parse();
		} catch (Exception e) {
			// 文件路径不正确抛出异常时添加 location 信息
			throw new ParseException(e.getMessage(), location, e);
		}
	}
	
	/**
	 * 以 "/" 打头则以 baseTemplatePath 为根，否则以父文件所在路径为根
	 */
	private String getFinalFileName(String fileName, String parentFileName) {
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
	
	public void exec(Env env, Scope scope, Writer writer) {
		scope = new Scope(scope);
		stat.exec(env, scope, writer);
	}
}







