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
import java.util.List;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Ctrl;
import com.jfinal.template.stat.Scope;

/**
 * StatList
 */
public class StatList extends Stat {
	
	public static final Stat[] NULL_STATS =  new Stat[0];
	private Stat[] statArray;
	
	public StatList(List<Stat> statList) {
		if (statList.size() > 0) {
			this.statArray = statList.toArray(new Stat[statList.size()]);
		} else {
			this.statArray = NULL_STATS;
		}
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Ctrl ctrl = scope.getCtrl();
		for (Stat stat : statArray) {
			if (ctrl.isJump()) {
				break ;
			}
			stat.exec(env, scope, writer);	
		}
	}
	
	public int length() {
		return statArray.length;
	}
	
	public Stat getStat(int index) {
		if (index < 0 || index >= statArray.length) {
			throw new TemplateException("Index out of bounds: index = " + index + ", length = " + statArray.length, location);
		}
		return statArray[index];
	}
}


