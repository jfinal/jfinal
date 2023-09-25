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
 * The six logging levels used by Log are (in order):
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
	 * 以下为 jfinal 4.8 新增的支持可变参数的方法
	 * 1：为了兼容用户已经扩展出来的 Log 实现，给出了默认实现
	 * 
	 * 2：默认实现通过 String.format(...) 实现的占位符功能与
	 *   slf4j 的占位符用法不同，因此在使用中要保持使用其中某一种，
	 *   否则建议使用其它方法替代下面的 6 个方法
	 *   注意：jfinal 内部未使用日志的可变参数方法，所以支持各类
	 *        日志切换使用
	 *   
	 * 3：某些日志系统（如 log4j，参考 Log4jLog）需要覆盖以下方法，才能
	 *    保障日志信息中的类名正确
	 */
	
	/**
	 * 判断可变参数是否以 Throwable 结尾
	 */
	protected boolean endsWithThrowable(Object... args) {
		return	args != null && args.length != 0 &&
				args[args.length - 1] instanceof Throwable;
	}
	
	/**
	 * parse(...) 方法必须与 if (endsWithThrowable(...)) 配合使用，
	 * 确保可变参数 Object... args 中的最后一个元素为 Throwable 类型
	 */
	protected LogInfo parse(String format, Object... args) {
		LogInfo li = new LogInfo();
		
		// 最后一个参数已确定为 Throwable
		li.throwable = (Throwable)args[args.length - 1];
		
		// 其它参数与 format 一起格式化成 message
		if (args.length > 1) {
			Object[] temp = new Object[args.length - 1];
			for (int i=0; i<temp.length; i++) {
				temp[i] = args[i];
			}
			
			li.message = String.format(format, temp);
		} else {
			li.message = format;
		}
		
		return li;
	}
	
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

