/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.plugin.activerecord;

import java.sql.SQLException;

/**
 * IAtom support transaction of database.
 * It can be invoked in Db.tx(IAtom atom) method.
 * <br>
 * Example:<br>
 * Db.tx(new IAtom(){<br>
 * 		public boolean run() throws SQLException {<br>
 * 			int result1 = Db.update("update account set cash = cash - ? where id = ?", 100, 123);<br>
 * 			int result2 = Db.update("update account set cash = cash + ? where id = ?", 100, 456);<br>
 * 			return result1 == 1 && result2 == 1;<br>
 * 		}});
 */
public interface IAtom {
	
	/**
	 * Place codes here that need transaction support.
	 * @return true if you want to commit the transaction otherwise roll back transaction
	 */
	boolean run() throws SQLException;
}