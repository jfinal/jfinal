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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ICallback provide a JDBC Connection if you need it or the active record plugin can not satisfy you requirement.
 */
public interface ICallback {
	
	/**
	 * Place codes here that need call back by active record plugin.
	 * @param conn the JDBC Connection, you need't close this connection after used it, active record plugin will close it automatically
	 */
	Object call(Connection conn) throws SQLException;
}