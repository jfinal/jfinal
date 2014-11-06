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

package com.jfinal.log;

/**
 * NullLoggerFactory.
 */
public class NullLoggerFactory implements ILoggerFactory {
	
	public com.jfinal.log.Logger getLogger(Class<?> clazz) {
		return INSTANCE;
	}

	public com.jfinal.log.Logger getLogger(String name) {
		return INSTANCE;
	}
	
	private static final Logger INSTANCE = new Logger() {
		
		public void debug(String message) {
		}
		
		public void debug(String message, Throwable t) {
		}
		
		public void error(String message) {
		}
		
		public void error(String message, Throwable t) {
		}
		
		public void info(String message) {
		}
		
		public void info(String message, Throwable t) {
		}
		
		public boolean isDebugEnabled() {
			return false;
		}

		public boolean isInfoEnabled() {
			return false;
		}

		public boolean isWarnEnabled() {
			return false;
		}
		
		public boolean isErrorEnabled() {
			return false;
		}
		
		public boolean isFatalEnabled() {
			return false;
		}
		
		public void warn(String message) {
		}
		
		public void warn(String message, Throwable t) {
		}
		
		public void fatal(String message) {
		}
		
		public void fatal(String message, Throwable t) {
		}
	};
}
