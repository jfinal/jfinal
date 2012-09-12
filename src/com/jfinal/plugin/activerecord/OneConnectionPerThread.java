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

package com.jfinal.plugin.activerecord;

import java.sql.Connection;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;

/**
 * 实现一个线程仅一个数据库连接, 以提高性能
 * 注意是否与事务冲突了
 */
public class OneConnectionPerThread implements Interceptor {
	
	public void intercept(ActionInvocation invocation) {
		Connection conn = null;
		try {
			conn = DbKit.getConnection();
			DbKit.setThreadLocalConnection(conn);
			invocation.invoke();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			DbKit.removeThreadLocalConnection();
			DbKit.close(conn);
		}
	}
}
