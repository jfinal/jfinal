/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
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

import org.slf4j.spi.LocationAwareLogger;

/**
 * Slf4jLog
 */
public class Slf4jLog extends Log {
	
	private LocationAwareLogger log;
	
	private static final Object[] NULL_ARGS = new Object[0];
	private static final String callerFQCN = Slf4jLog.class.getName();
	
	Slf4jLog(LocationAwareLogger log) {
		this.log = log;
	}
	
	@Override
	public void trace(String message) {
		if (isTraceEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.TRACE_INT, message, NULL_ARGS, null);
		}
	}
	
	@Override
	public void trace(String message, Throwable t) {
		if (isTraceEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.TRACE_INT, message, NULL_ARGS, t);
		}
	}
	
	@Override
	public void debug(String message) {
		if (isDebugEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.DEBUG_INT, message, NULL_ARGS, null);
		}
	}
	
	@Override
	public void debug(String message, Throwable t) {
		if (isDebugEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.DEBUG_INT, message, NULL_ARGS, t);
		}
	}
	
	@Override
	public void info(String message) {
		if (isInfoEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.INFO_INT, message, NULL_ARGS, null);
		}
	}
	
	@Override
	public void info(String message, Throwable t) {
		if (isInfoEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.INFO_INT, message, NULL_ARGS, t);
		}
	}
	
	@Override
	public void warn(String message) {
		if (isWarnEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.WARN_INT, message, NULL_ARGS, null);
		}
	}

	@Override
	public void warn(String message, Throwable t) {
		if (isWarnEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.WARN_INT, message, NULL_ARGS, t);
		}
	}
	
	@Override
	public void error(String message) {
		if (isErrorEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.ERROR_INT, message, NULL_ARGS, null);
		}
	}
	
	@Override
	public void error(String message, Throwable t) {
		if (isErrorEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.ERROR_INT, message, NULL_ARGS, t);
		}
	}
	
	@Override
	public void fatal(String message) {
		// throw new UnsupportedOperationException("slf4j logger does not support fatal level");
		if (isErrorEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.ERROR_INT, message, NULL_ARGS, null);
		}
	}
	
	@Override
	public void fatal(String message, Throwable t) {
		// throw new UnsupportedOperationException("slf4j logger does not support fatal level");
		if (isErrorEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.ERROR_INT, message, NULL_ARGS, t);
		}
	}
	
	@Override
	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
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
		// throw new UnsupportedOperationException("slf4j logger does not support fatal level");
		return log.isErrorEnabled();
	}
	
	// -------------------------------------------------------
	
	public void trace(String format, Object... args) {
		if (isTraceEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.TRACE_INT, format, args, null);
		}
	}
	
	public void debug(String format, Object... args) {
		if (isDebugEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.DEBUG_INT, format, args, null);
		}
	}
	
	public void info(String format, Object... args) {
		if (isInfoEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.INFO_INT, format, args, null);
		}
	}
	
	public void warn(String format, Object... args) {
		if (isWarnEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.WARN_INT, format, args, null);
		}
	}
	
	public void error(String format, Object... args) {
		if (isErrorEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.ERROR_INT, format, args, null);
		}
	}
	
	public void fatal(String format, Object... args) {
		// throw new UnsupportedOperationException("slf4j logger does not support fatal level");
		if (isFatalEnabled()) {
			log.log(null, callerFQCN, LocationAwareLogger.ERROR_INT, format, args, null);
		}
	}
}





