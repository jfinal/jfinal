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

/**
 * WriterBuffer
 */
public class WriterBuffer {
	
	private static final int MIN_BUFFER_SIZE = 64;					// 缓冲区最小 64 字节
	private static final int MAX_BUFFER_SIZE = 1024 * 1024 * 10;	// 缓冲区最大 10M 字节
	
	private int bufferSize = 2048;									// 缓冲区大小
	
	private EncoderFactory encoderFactory = new EncoderFactory();
	
	private final ThreadLocal<ByteWriter> byteWriters = new ThreadLocal<ByteWriter>() {
		protected ByteWriter initialValue() {
			return new ByteWriter(encoderFactory.getEncoder(), bufferSize);
		}
	};
	
	private final ThreadLocal<CharWriter> charWriters = new ThreadLocal<CharWriter>() {
		protected CharWriter initialValue() {
			return new CharWriter(bufferSize);
		}
	};
	
	private final ThreadLocal<FastStringWriter> fastStringWriters = new ThreadLocal<FastStringWriter>() {
		protected FastStringWriter initialValue() {
			return new FastStringWriter();
		}
	};
	
	public ByteWriter getByteWriter(java.io.OutputStream outputStream) {
		return byteWriters.get().init(outputStream);
	}
	
	public CharWriter getCharWriter(java.io.Writer writer) {
		return charWriters.get().init(writer);
	}
	
	public FastStringWriter getFastStringWriter() {
		return fastStringWriters.get();
	}
	
	public void setBufferSize(int bufferSize) {
		if (bufferSize < MIN_BUFFER_SIZE || bufferSize > MAX_BUFFER_SIZE) {
			throw new IllegalArgumentException("bufferSize must between " + (MIN_BUFFER_SIZE-1) + " and " + (MAX_BUFFER_SIZE+1));
		}
		this.bufferSize = bufferSize;
	}
	
	public void setEncoderFactory(EncoderFactory encoderFactory) {
		if (encoderFactory == null) {
			throw new IllegalArgumentException("encoderFactory can not be null");
		}
		this.encoderFactory = encoderFactory;
	}
	
	public void setEncoding(String encoding) {
		encoderFactory.setEncoding(encoding);
	}
}








