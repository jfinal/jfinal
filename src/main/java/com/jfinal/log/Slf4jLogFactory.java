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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * Slf4jLogFactory
 */
public class Slf4jLogFactory implements ILogFactory {
	
	@Override
	public Log getLog(Class<?> clazz) {
		Logger log = LoggerFactory.getLogger(clazz);
		return log instanceof LocationAwareLogger ? new Slf4jLog((LocationAwareLogger)log) : new Slf4jSimpleLog(log);
	}
	
	@Override
	public Log getLog(String name) {
		Logger log = LoggerFactory.getLogger(name);
		return log instanceof LocationAwareLogger ? new Slf4jLog((LocationAwareLogger)log) : new Slf4jSimpleLog(log);
	}
	
	/**
	 * Slf4jSimpleLog 支持 slf4j-simple
	 */
	public static class Slf4jSimpleLog extends Log {
		
		private org.slf4j.Logger log;
		
		Slf4jSimpleLog(org.slf4j.Logger log) {
			this.log = log;
		}
		
		@Override
		public void debug(String message) {
			log.debug(message);
		}
		
		@Override
		public void debug(String message, Throwable t) {
			log.debug(message, t);
		}
		
		@Override
		public void info(String message) {
			log.info(message);
		}
		
		@Override
		public void info(String message, Throwable t) {
			log.info(message, t);
		}
		
		@Override
		public void warn(String message) {
			log.warn(message);
		}
		
		@Override
		public void warn(String message, Throwable t) {
			log.warn(message, t);
		}
		
		@Override
		public void error(String message) {
			log.error(message);
		}
		
		@Override
		public void error(String message, Throwable t) {
			log.error(message, t);
		}
		
		@Override
		public void fatal(String message) {
			log.error(message);
		}
		
		@Override
		public void fatal(String message, Throwable t) {
			log.error(message, t);
		}
		
		@Override
		public boolean isDebugEnabled() {
			return log.isDebugEnabled();
		}
		
		@Override
		public boolean isInfoEnabled() {
			return log.isInfoEnabled();
		}
		
		@Override
		public boolean isWarnEnabled() {
			return log.isWarnEnabled();
		}
		
		@Override
		public boolean isErrorEnabled() {
			return log.isErrorEnabled();
		}
		
		@Override
		public boolean isFatalEnabled() {
			return log.isErrorEnabled();
		}
		
		// -------------------------------------------------------
		
		public boolean isTraceEnabled() {
			return log.isTraceEnabled();
		}
		
		public void trace(String message) {
			log.trace(message);
		}
		
		public void trace(String message, Throwable t) {
			log.trace(message, t);
		}
		
		// -------------------------------------------------------
		
		public void trace(String format, Object... args) {
			if (isTraceEnabled()) {
				log.trace(format, args);
			}
		}
		
		public void debug(String format, Object... args) {
			if (isDebugEnabled()) {
				log.debug(format, args);
			}
		}
		
		public void info(String format, Object... args) {
			if (isInfoEnabled()) {
				log.info(format, args);
			}
		}
		
		public void warn(String format, Object... args) {
			if (isWarnEnabled()) {
				log.warn(format, args);
			}
		}
		
		public void error(String format, Object... args) {
			if (isErrorEnabled()) {
				log.error(format, args);
			}
		}
		
		public void fatal(String format, Object... args) {
			if (isFatalEnabled()) {
				log.error(format, args);
			}
		}
	}
}






