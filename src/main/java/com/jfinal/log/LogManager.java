/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.log;

/**
 * LogManager.
 */
public class LogManager {
	
	private static final LogManager me = new LogManager();
	
	private LogManager() {}
	
	public static LogManager me() {
		return me;
	}
	
	public void init() {
		Log.init();
	}
	
	public void setDefaultLogFactory(ILogFactory defaultLogFactory) {
		Log.setDefaultLogFactory(defaultLogFactory);
		com.jfinal.kit.LogKit.synchronizeLog();
	}
	
	/**
	 * 切换到 slf4j 日志框架，需要引入 slf4j 相关依赖
	 * 切换过去以后的用法参考 slf4j 文档
	 */
	public void setToSlf4jLogFactory() {
		setDefaultLogFactory(new Slf4jLogFactory());
	}
}


