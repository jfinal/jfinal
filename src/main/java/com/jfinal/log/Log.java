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

/**
 * The five logging levels used by Log are (in order):
 * 1. TRACE (the least serious)
 * 2. DEBUG
 * 3. INFO
 * 4. WARN
 * 5. ERROR
 * 6. FATAL (the most serious)
 */
public abstract class Log {
	
	private static ILogFactory defaultLogFactory = null;
	
	static {
		init();
	}
	
	static void init() {
		if (defaultLogFactory == null) {
			try {
				Class.forName("org.apache.log4j.Logger");
				Class<?> log4jLogFactoryClass = Class.forName("com.jfinal.log.Log4jLogFactory");
				defaultLogFactory = (ILogFactory)log4jLogFactoryClass.newInstance();	// return new Log4jLogFactory();
			} catch (Exception e) {
				defaultLogFactory = new JdkLogFactory();
			}
		}
	}
	
	static void setDefaultLogFactory(ILogFactory defaultLogFactory) {
		if (defaultLogFactory == null) {
			throw new IllegalArgumentException("defaultLogFactory can not be null.");
		}
		Log.defaultLogFactory = defaultLogFactory;
	}
	
	public static Log getLog(Class<?> clazz) {
		return defaultLogFactory.getLog(clazz);
	}
	
	public static Log getLog(String name) {
		return defaultLogFactory.getLog(name);
	}
	
	public abstract void debug(String message);
	
	public abstract void debug(String message, Throwable t);
	
	public abstract void info(String message);
	
	public abstract void info(String message, Throwable t);
	
	public abstract void warn(String message);
	
	public abstract void warn(String message, Throwable t);
	
	public abstract void error(String message);
	
	public abstract void error(String message, Throwable t);
	
	public abstract void fatal(String message);
	
	public abstract void fatal(String message, Throwable t);
	
	public abstract boolean isDebugEnabled();

	public abstract boolean isInfoEnabled();

	public abstract boolean isWarnEnabled();

	public abstract boolean isErrorEnabled();
	
	public abstract boolean isFatalEnabled();
	
	// -------------------------------------------------------
	
	/*
	 * 以下 3 个方法为 jfinal 4.8 新增日志级别：trace
	 * 1：为了兼容用户已经扩展出来的 Log 实现，给出了默认实现
	 * 2：某些日志系统（如 log4j，参考 Log4jLog）需要覆盖以下
	 *    方法，才能保障日志信息中的类名正确
	 */
	
	public boolean isTraceEnabled() {
		return isDebugEnabled();
	}
	
	public void trace(String message) {
		debug(message);
	}
	
	public void trace(String message, Throwable t) {
		debug(message, t);
	}
	
	// -------------------------------------------------------
	
	/*
	 * 以下 6 个方法为 jfinal 4.8 新增的支持可变参数的方法
	 * 1：为了兼容用户已经扩展出来的 Log 实现，给出了默认实现
	 * 2：默认实现通过 String.format(...) 实现的占位符功能与
	 *   slf4j 的占位符用法不同，因此在使用中要保持使用其中某一种，
	 *   否则建议使用其它方法替代下面的 6 个方法
	 * 3：某些日志系统（如 log4j，参考 Log4jLog）需要覆盖以下方法，才能
	 *    保障日志信息中的类名正确
	 */
	
	public void trace(String format, Object... args) {
		if (isTraceEnabled()) {
			trace(String.format(format, args));
		}
	}
	
	public void debug(String format, Object... args) {
		if (isDebugEnabled()) {
			debug(String.format(format, args));
		}
	}
	
	public void info(String format, Object... args) {
		if (isInfoEnabled()) {
			info(String.format(format, args));
		}
	}
	
	public void warn(String format, Object... args) {
		if (isWarnEnabled()) {
			warn(String.format(format, args));
		}
	}
	
	public void error(String format, Object... args) {
		if (isErrorEnabled()) {
			error(String.format(format, args));
		}
	}
	
	public void fatal(String format, Object... args) {
		if (isFatalEnabled()) {
			fatal(String.format(format, args));
		}
	}
}

