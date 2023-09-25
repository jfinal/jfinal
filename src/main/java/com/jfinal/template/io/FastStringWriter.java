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

package com.jfinal.template.io;

import java.io.IOException;
import java.io.Writer;

/**
 * FastStringWriter
 * 
 * <pre>
 * 由 JDK 中 Writer 改造而来，在其基础之上做了如下改变：
 * 1：添加 char[] value 直接保存 char 值
 * 2：添加 int len 记录数据长度
 * 3：去掉 synchronized 操作
 * 4：添加 MAX_BUFFER_SIZE，限定 value 被重用的最大长度
 * 5：去掉了 close() 方法声明中的 throws IOException，并添加缓存回收逻辑
 * </pre>
 */
public class FastStringWriter extends Writer {
	
	private char[] value;
	private int len;
	
	boolean inUse;	// 支持 reentrant
	
	private static int MAX_BUFFER_SIZE = 1024 * 512;		// 1024 * 64;
	
	public static void setMaxBufferSize(int maxBufferSize) {
		int min = 256;
		if (maxBufferSize < min) {
			throw new IllegalArgumentException("maxBufferSize must more than " + min);
		}
		MAX_BUFFER_SIZE = maxBufferSize;
	}
	
	public FastStringWriter init() {
		inUse = true;
		return this;
	}
	
	@Override
	public void close() /* throws IOException */ {
		inUse = false;
		len = 0;
		
		// 释放空间占用过大的缓存
		if (value.length > MAX_BUFFER_SIZE) {
			value = new char[Math.max(256, MAX_BUFFER_SIZE / 2)];
		}
	}
	
	public boolean isInUse() {
		return inUse;
	}
	
	public String toString() {
		return new String(value, 0, len);
	}
	
	public StringBuilder toStringBuilder() {
		return new StringBuilder(len + 64).append(value, 0, len);
	}
	
	public FastStringWriter(int capacity) {
		value = new char[capacity];
	}
	
	public FastStringWriter() {
		this(128);
	}
	
	/**
	 * 扩容
	 */
	protected void expandCapacity(int newLen) {
		int newCapacity = Math.max(newLen, value.length * 2);
		char[] newValue = new char[newCapacity];
		
		// 复制 value 中的值到 newValue
		if (len > 0) {
			System.arraycopy(value, 0, newValue, 0, len);
		}
		value = newValue;
	}
	
	@Override
	public void write(char buffer[], int offset, int len) throws IOException {
		int newLen = this.len + len;
		if (newLen > value.length) {
			expandCapacity(newLen);
		}
		
		System.arraycopy(buffer, offset, value, this.len, len);
		this.len = newLen;
	}
	
	@Override
	public void write(String str, int offset, int len) throws IOException {
		int newLen = this.len + len;
		if (newLen > value.length) {
			expandCapacity(newLen);
		}
		
		str.getChars(offset, (offset + len), value, this.len);
		this.len = newLen;
	}
	
	@Override
	public void write(int c) throws IOException {
		char[] buffer = {(char)c};
		write(buffer, 0, 1);
	}
	
	@Override
	public void write(char buffer[]) throws IOException {
		write(buffer, 0, buffer.length);
	}
	
	@Override
	public void write(String str) throws IOException {
		write(str, 0, str.length());
	}
	
	@Override
	public Writer append(CharSequence csq) throws IOException {
		if (csq instanceof String) {
			String str = (String)csq;
			write(str, 0, str.length());
			return this;
		}
		
		if (csq == null)
			write("null");
		else
			write(csq.toString());
		return this;
	}
	
	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		if (csq instanceof String) {
			String str = (String)csq;
			write(str, start, (end - start));
			return this;
		}
		
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());
		return this;
	}
	
	@Override
	public Writer append(char c) throws IOException {
		char[] buffer = {c};
		write(buffer, 0, 1);
		return this;
	}
	
	@Override
	public void flush() throws IOException {
		
	}
}





