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

import java.util.List;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Ctrl;
import com.jfinal.template.stat.Scope;

/**
 * StatList
 */
public class StatList extends Stat {
	
	public static final Stat NULL_STAT = NullStat.me;
	public static final Stat[] NULL_STAT_ARRAY = new Stat[0];
	
	private Stat[] statArray;
	
	public StatList(List<Stat> statList) {
		if (statList.size() > 0) {
			this.statArray = statList.toArray(new Stat[statList.size()]);
		} else {
			this.statArray = NULL_STAT_ARRAY;
		}
	}
	
	/**
	 * 持有 StatList 的指令可以通过此方法提升 AST 执行性能
	 * 1：当 statArray.length >  1 时返回 StatList 自身
	 * 2：当 statArray.length == 1 时返回 statArray[0]
	 * 3：其它情况返回 NullStat
	 * 
	 * 意义在于，当满足前面两个条件时，避免掉了 StatList.exec(...) 方法中的判断与循环
	 */
	public Stat getActualStat() {
		if (statArray.length > 1) {
			return this;
		} else if (statArray.length == 1) {
			return statArray[0];
		} else {
			return NULL_STAT;
		}
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Ctrl ctrl = scope.getCtrl();
		for (int i=0; i<statArray.length; i++) {
			if (ctrl.isJump()) {
				break ;
			}
			statArray[i].exec(env, scope, writer);	
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


