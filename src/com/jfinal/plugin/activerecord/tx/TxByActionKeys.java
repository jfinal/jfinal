/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.plugin.activerecord.tx;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

/**
 * TxByActionKeys
 */
public class TxByActionKeys implements Interceptor {
	
	private Set<String> actionKeySet = new HashSet<String>();
	
	public TxByActionKeys(String... actionKeys) {
		if (actionKeys == null || actionKeys.length == 0)
			throw new IllegalArgumentException("actionKeys can not be blank.");
		
		for (String actionKey : actionKeys)
			actionKeySet.add(actionKey.trim());
	}
	
	public void intercept(final ActionInvocation ai) {
		if (actionKeySet.contains(ai.getActionKey())) {
			Db.tx(new IAtom(){
				public boolean run() throws SQLException {
					ai.invoke();
					return true;
				}});
		}
		else {
			ai.invoke();
		}
	}
}
