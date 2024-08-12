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
 * Log4j2Log
 */
public class Log4j2Log extends Log {

    private final transient org.apache.logging.log4j.Logger logger;

    public Log4j2Log(org.apache.logging.log4j.Logger logger) {
        this.logger = logger;
    }

    public Log4j2Log(Class<?> clazz) {
        this(org.apache.logging.log4j.LogManager.getLogger(clazz));
    }

    public Log4j2Log(String name) {
        this(org.apache.logging.log4j.LogManager.getLogger(name));
    }

    //-------------------------------------------------------

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void debug(String message, Throwable t) {
        logger.debug(message, t);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Throwable t) {
        logger.info(message, t);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable t) {
        logger.warn(message, t);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        logger.error(message, t);
    }

    @Override
    public void fatal(String message) {
        logger.fatal(message);
    }

    @Override
    public void fatal(String message, Throwable t) {
        logger.fatal(message, t);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }

    @Override
    public void trace(String message, Throwable t) {
        logger.trace(message, t);
    }

    //-------------------------------------------------------

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isFatalEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    //-------------------------------------------------------

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
