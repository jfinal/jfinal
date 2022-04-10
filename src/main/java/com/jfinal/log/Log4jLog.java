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

import org.apache.log4j.Level;

/**
 * Log4jLog.
 */
public class Log4jLog extends Log {
	
	private org.apache.log4j.Logger log;
	private static final String callerFQCN = Log4jLog.class.getName();
	
	Log4jLog(Class<?> clazz) {
		log = org.apache.log4j.Logger.getLogger(clazz);
	}
	
	Log4jLog(String name) {
		log = org.apache.log4j.Logger.getLogger(name);
	}
	
	public static Log4jLog getLog(Class<?> clazz) {
		return new Log4jLog(clazz);
	}
	
	public static Log4jLog getLog(String name) {
		return new Log4jLog(name);
	}
	
	public void trace(String message) {
		log.log(callerFQCN, Level.TRACE, message, null);
	}
	
	public void trace(String message, Throwable t) {
		log.log(callerFQCN, Level.TRACE, message, t);
	}
	
	public void debug(String message) {
		log.log(callerFQCN, Level.DEBUG, message, null);
	}
	
	public void debug(String message, Throwable t) {
		log.log(callerFQCN, Level.DEBUG, message, t);
	}
	
	public void info(String message) {
		log.log(callerFQCN, Level.INFO, message, null);
	}
	
	public void info(String message, Throwable t) {
		log.log(callerFQCN, Level.INFO, message, t);
	}
	
	public void warn(String message) {
		log.log(callerFQCN, Level.WARN, message, null);
	}
	
	public void warn(String message, Throwable t) {
		log.log(callerFQCN, Level.WARN, message, t);
	}
	
	public void error(String message) {
		log.log(callerFQCN, Level.ERROR, message, null);
	}
	
	public void error(String message, Throwable t) {
		log.log(callerFQCN, Level.ERROR, message, t);
	}
	
	public void fatal(String message) {
		log.log(callerFQCN, Level.FATAL, message, null);
	}
	
	public void fatal(String message, Throwable t) {
		log.log(callerFQCN, Level.FATAL, message, t);
	}
	
	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}
	
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}
	
	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}
	
	public boolean isWarnEnabled() {
		return log.isEnabledFor(Level.WARN);
	}
	
	public boolean isErrorEnabled() {
		return log.isEnabledFor(Level.ERROR);
	}
	
	public boolean isFatalEnabled() {
		return log.isEnabledFor(Level.FATAL);
	}
	
	// -------------------------------------------------------
	
	/*
	 * 以下方法与前面的两个 trace 方法必须覆盖父类中的实现，否则日志中的类名为
	 * com.jfinal.log.Log 而非所需要的日志发生地点的类名
	 */
	
	public void trace(String format, Object... args) {
		if (isTraceEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				trace(li.message, li.throwable);
			} else {
				trace(String.format(format, args));
			}
		}
	}
	
	public void debug(String format, Object... args) {
		if (isDebugEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				debug(li.message, li.throwable);
			} else {
				debug(String.format(format, args));
			}
		}
	}
	
	public void info(String format, Object... args) {
		if (isInfoEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				info(li.message, li.throwable);
			} else {
				info(String.format(format, args));
			}
		}
	}
	
	public void warn(String format, Object... args) {
		if (isWarnEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				warn(li.message, li.throwable);
			} else {
				warn(String.format(format, args));
			}
		}
	}
	
	public void error(String format, Object... args) {
		if (isErrorEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				error(li.message, li.throwable);
			} else {
				error(String.format(format, args));
			}
		}
	}
	
	public void fatal(String format, Object... args) {
		if (isFatalEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				fatal(li.message, li.throwable);
			} else {
				fatal(String.format(format, args));
			}
		}
	}
}

