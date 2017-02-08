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

import java.io.IOException;
import java.io.Writer;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.Scope;

/**
 * Stat
 */
public abstract class Stat {
	
	protected Location location;
	
	public Stat setLocation(Location location) {
		this.location = location;
		return this;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setExprList(ExprList exprList) {
	}
	
	public void setStat(Stat stat) {
	}
	
	public abstract void exec(Env env, Scope scope, Writer writer);
	
	public boolean hasEnd() {
		return false;
	}
	
	protected void write(Writer writer, String str) {
		try {
			writer.write(str);
		} catch (IOException e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
	
	protected void write(Writer writer, char[] chars) {
		try {
			writer.write(chars);
		} catch (IOException e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
}







