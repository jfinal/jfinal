/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jfinal.log;


public class Slf4jLogger extends Log {

    private org.slf4j.Logger log;


    Slf4jLogger(Class<?> clazz) {
        log = org.slf4j.LoggerFactory.getLogger(clazz);
    }


    Slf4jLogger(String name) {
        log = org.slf4j.LoggerFactory.getLogger(name);
    }


    // (marker, this, level, msg,params, t);注意参数顺序
    @Override
    public void info(String message) {
        log.info(message);
    }


    @Override
    public void info(String message, Throwable t) {
        log.info(message, t);
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
}