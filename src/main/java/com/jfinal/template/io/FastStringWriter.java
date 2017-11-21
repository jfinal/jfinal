/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.template.io;

import java.io.Writer;

/**
 * FastStringWriter
 * 
 * <pre>
 * 由 JDK 中 StringWriter 改造而来，在其基础之上做了如下改变：
 * 1：StringBuffer 属性改为 StringBuilder，避免了前者的 synchronized 操作
 * 2：添加了 MAX_SIZE 属性
 * 3：去掉了 close() 方法声明中的 throws IOException，并添加了代码，原先该方法中无任何代码
 * </pre>
 */
public class FastStringWriter extends Writer {
	
	private StringBuilder buf;
	
    public FastStringWriter() {
        buf = new StringBuilder();
    }
    
    public FastStringWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative buffer size");
        }
        buf = new StringBuilder(initialSize);
    }
    
    public void write(int c) {
        buf.append((char) c);
    }
    
    public void write(char cbuf[], int off, int len) {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        buf.append(cbuf, off, len);
    }
    
    public void write(String str) {
        buf.append(str);
    }
    
    public void write(String str, int off, int len)  {
        buf.append(str.substring(off, off + len));
    }
    
    public FastStringWriter append(CharSequence csq) {
        if (csq == null) {
            write("null");
        } else {
            write(csq.toString());
        }
        return this;
    }
    
    public FastStringWriter append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }
    
    public FastStringWriter append(char c) {
        write(c);
        return this;
    }
    
    public String toString() {
        return buf.toString();
    }
    
    public StringBuilder getBuffer() {
        return buf;
    }
    
    public void flush() {
    	
    }
    
    static int MAX_SIZE = 1024 * 128;
    
    /**
     * 由 StringWriter.close() 改造而来，原先该方法中无任何代码 ，改造如下：
     * 1：去掉 throws IOException
     * 2：添加 buf 空间释放处理逻辑
     * 3：添加 buf.setLength(0)，以便于配合 ThreadLocal 回收利用
     */
    public void close() {
    	if (buf.length() > MAX_SIZE) {
    		buf = new StringBuilder();	// 释放空间占用过大的 buf
		} else {
			buf.setLength(0);
		}
    }
}





