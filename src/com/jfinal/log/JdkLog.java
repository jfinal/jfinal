/**
 * Copyright (c) 2011-2016, James Zhan 詹波 (jfinal@126.com).
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

import java.util.logging.Level;

/**
 * JdkLog.
 */
public class JdkLog extends Log {

	private java.util.logging.Logger log;
	private String clazzName;
	
	JdkLog(Class<?> clazz) {
		log = java.util.logging.Logger.getLogger(clazz.getName());
		clazzName = clazz.getName();
	}
	
	JdkLog(String name) {
		log = java.util.logging.Logger.getLogger(name);
		clazzName = name;
	}
	
	public static JdkLog getLog(Class<?> clazz) {
		return new JdkLog(clazz);
	}
	
	public static JdkLog getLog(String name) {
		return new JdkLog(name);
	}
	
	
	
//	public void debug(String message) {
//		log.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
//	}
//	
//	public void debug(String message,  Throwable t) {
//		log.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
//	}
//	
//	public void info(String message) {
//		log.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
//	}
//	
//	public void info(String message, Throwable t) {
//		log.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
//	}
//	
//	public void warn(String message) {
//		log.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
//	}
//	
//	public void warn(String message, Throwable t) {
//		log.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
//	}
//	
//	public void error(String message) {
//		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
//	}
//	
//	public void error(String message, Throwable t) {
//		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
//	}
//	
//	/**
//	 * JdkLog fatal is the same as the error.
//	 */
//	public void fatal(String message) {
//		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
//	}
//	
//	/**
//	 * JdkLog fatal is the same as the error.
//	 */
//	public void fatal(String message, Throwable t) {
//		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
//	}
	
	public boolean isDebugEnabled() {
		return log.isLoggable(Level.FINE);
	}
	
	public boolean isInfoEnabled() {
		return log.isLoggable(Level.INFO);
	}
	
	public boolean isWarnEnabled() {
		return log.isLoggable(Level.WARNING);
	}
	
	public boolean isErrorEnabled() {
		return log.isLoggable(Level.SEVERE);
	}
	
	public boolean isFatalEnabled() {
		return log.isLoggable(Level.SEVERE);
	}

	
    public void debug(String message) {
		if (isDebugEnabled()) {
			log.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
		}
	}

	
    public void debug(String message, Object arg) {
		if (isDebugEnabled()) {
	      FormattingTuple ft = MessageFormatter.format(message, arg);
	      log.logp(Level.FINE, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(), ft.getThrowable());
	    }
    }

	
    public void debug(String message, Object arg1,
            Object arg2) {
		if (isDebugEnabled()) {
	      FormattingTuple ft = MessageFormatter.format(message, arg1,arg2);
	      log.logp(Level.FINE, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(), ft.getThrowable());
	    }
    }

	
    public void debug(String message, Object... arg) {
		if (isDebugEnabled()) {
	      FormattingTuple ft = MessageFormatter.format(message, arg);
	      log.logp(Level.FINE, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(), ft.getThrowable());
	    }
    }

	
    public void debug(String message, Throwable t) {
		if (isDebugEnabled()) {
			log.logp(Level.FINE, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
		}
	}

	
    public void info(String message) {
		if (isInfoEnabled()) {
			log.logp(Level.INFO, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), message);
		}
    }

	
    public void info(String message, Object arg) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.format(message, arg);
			log.logp(Level.INFO, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(),ft.getThrowable());
		}
    }

	
    public void info(String message, Object arg1,
            Object arg2) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.format(message, arg1,arg2);
			log.logp(Level.INFO, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(),ft.getThrowable());
		}
    }

	
    public void info(String message, Object... arg) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.format(message,arg);
			log.logp(Level.INFO, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(),ft.getThrowable());
		}
    }

	
    public void info(String message, Throwable t) {
		if (isInfoEnabled()) {
			log.logp(Level.INFO, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), message,t);
		}
    }

	
    public void warn(String message) {
		if (isWarnEnabled()) {
			log.logp(Level.WARNING, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), message);
		}
    }

	
    public void warn(String message, Object arg) {
		if (isWarnEnabled()) {
			FormattingTuple ft = MessageFormatter.format(message,arg);
			log.logp(Level.WARNING, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(),ft.getThrowable());
		}
    }

	
    public void warn(String message, Object... arg) {
		if (isWarnEnabled()) {
			FormattingTuple ft = MessageFormatter.format(message, arg);
			log.logp(Level.WARNING, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(),ft.getThrowable());
		}
    }

	
    public void warn(String message, Object arg1,
            Object arg2) {
		if (isWarnEnabled()) {
			FormattingTuple ft = MessageFormatter.format(message, arg1,arg2);
			log.logp(Level.WARNING, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(),ft.getThrowable());
		}
    }

	
    public void warn(String message, Throwable t) {
		if (isWarnEnabled()) {
			log.logp(Level.WARNING, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), message,t);
		}
    }

	
    public void error(String message) {
		if (isErrorEnabled()) {
			log.logp(Level.SEVERE, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), message);
		}
    }

	
    public void error(String message, Object arg) {
		if (isErrorEnabled()) {
			FormattingTuple ft = MessageFormatter.format(message,arg);
			log.logp(Level.SEVERE, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(),ft.getThrowable());
		}
    }

	
    public void error(String message, Object arg1,
            Object arg2) {
		if (isErrorEnabled()) {
			FormattingTuple ft = MessageFormatter.format(message, arg1,arg2);
			log.logp(Level.SEVERE, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(),ft.getThrowable());
		}
    }

	
    public void error(String message, Object... arg) {
		if (isErrorEnabled()) {
			FormattingTuple ft = MessageFormatter.format(message, arg);
			log.logp(Level.SEVERE, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), ft.getMessage(),ft.getThrowable());
		}
    }

	
    public void error(String message, Throwable t) {
		if (isErrorEnabled()) {
			log.logp(Level.SEVERE, clazzName,Thread.currentThread().getStackTrace()[1].getMethodName(), message,t);
		}
    }
}



