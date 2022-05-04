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

package com.jfinal.kit;

import com.jfinal.log.Log;

/**
 * LogKit.
 * 
 * 注意：LogKit 是专门针对 jfinal 内部使用来设计的，不建议大家在项目中使用 LogKit
 *      因为 LogKit 输出不带 Throwable 参数的日志时，将不会记录日志发生地点的类信息
 *      与方法信息，LogKit 可以做成通过反射机制来输出类信息与方法信息，但会损失性能
 *      
 *      用户自己的代码中应该使用如下形式来做日志：
 *        Log.getLog(...).error(...);
 */
public class LogKit {
	
	private static class Holder {
		private static Log log = Log.getLog(LogKit.class);
	}
	
	/**
	 * 当通过 Constants.setLogFactory(...) 或者 
	 * LogManager.me().setDefaultLogFacotyr(...)
	 * 指定默认日志工厂以后，重置一下内部 Log 对象，以便使内部日志实现与系统保持一致
	 */
	public static void synchronizeLog() {
		Holder.log = Log.getLog(LogKit.class);
	}
	
	/**
	 * Do nothing.
	 */
	public static void logNothing(Throwable t) {
		
	}
	
	public static void trace(String message) {
		Holder.log.trace(message);
	}
	
	public static void trace(String message, Throwable t) {
		Holder.log.trace(message, t);
	}
	
	public static void trace(String format, Object... args) {
		Holder.log.trace(format, args);
	}
	
	public static void debug(String message) {
		Holder.log.debug(message);
	}
	
	public static void debug(String message, Throwable t) {
		Holder.log.debug(message, t);
	}
	
	public static void debug(String format, Object... args) {
		Holder.log.debug(format, args);
	}
	
	public static void info(String message) {
		Holder.log.info(message);
	}
	
	public static void info(String message, Throwable t) {
		Holder.log.info(message, t);
	}
	
	public static void info(String format, Object... args) {
		Holder.log.info(format, args);
	}
	
	public static void warn(String message) {
		Holder.log.warn(message);
	}
	
	public static void warn(String message, Throwable t) {
		Holder.log.warn(message, t);
	}
	
	public static void warn(String format, Object... args) {
		Holder.log.warn(format, args);
	}
	
	public static void error(String message) {
		Holder.log.error(message);
	}
	
	public static void error(String message, Throwable t) {
		Holder.log.error(message, t);
	}
	
	public static void error(String format, Object... args) {
		Holder.log.error(format, args);
	}
	
	public static void fatal(String message) {
		Holder.log.fatal(message);
	}
	
	public static void fatal(String message, Throwable t) {
		Holder.log.fatal(message, t);
	}
	
	public static void fatal(String format, Object... args) {
		Holder.log.fatal(format, args);
	}
	
	public static boolean isTraceEnabled() {
		return Holder.log.isTraceEnabled();
	}
	
	public static boolean isDebugEnabled() {
		return Holder.log.isDebugEnabled();
	}
	
	public static boolean isInfoEnabled() {
		return Holder.log.isInfoEnabled();
	}
	
	public static boolean isWarnEnabled() {
		return Holder.log.isWarnEnabled();
	}
	
	public static boolean isErrorEnabled() {
		return Holder.log.isErrorEnabled();
	}
	
	public static boolean isFatalEnabled() {
		return Holder.log.isFatalEnabled();
	}
}

