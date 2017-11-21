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

import java.io.IOException;
import java.io.OutputStream;

/**
 * ByteWriter
 */
public class ByteWriter extends Writer {
	
	OutputStream out;
	Encoder encoder;
	
	char[] chars;
	byte[] bytes;
	
	public ByteWriter(Encoder encoder, int bufferSize) {
		this.encoder = encoder;
		this.chars = new char[bufferSize];
		this.bytes = new byte[bufferSize * ((int)encoder.maxBytesPerChar())];
	}
	
	public ByteWriter init(OutputStream outputStream) {
		this.out = outputStream;
		return this;
	}
	
	public void flush() throws IOException {
		out.flush();
	}
	
	public void close() {
		try {
			if (out != null) {
				out.flush();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			out = null;
		}
	}
	
	public void write(String str, int offset, int len) throws IOException {
		while (len > chars.length) {
			write(str, offset, chars.length);
			offset += chars.length;
			len -= chars.length;
		}
		
		str.getChars(offset, offset + len, chars, 0);
		int byteLen = encoder.encode(chars, 0, len, bytes);
		out.write(bytes, 0, byteLen);
	}
	
	public void write(String str) throws IOException {
		write(str, 0, str.length());
	}
	
	public void write(StringBuilder stringBuilder, int offset, int len) throws IOException {
		while (len > chars.length) {
			write(stringBuilder, offset, chars.length);
			offset += chars.length;
			len -= chars.length;
		}
		
		stringBuilder.getChars(offset, offset + len, chars, 0);
		int byteLen = encoder.encode(chars, 0, len, bytes);
		out.write(bytes, 0, byteLen);
	}
	
	public void write(StringBuilder stringBuilder) throws IOException {
		write(stringBuilder, 0, stringBuilder.length());
	}
	
	public void write(IWritable writable) throws IOException {
		byte[] data = writable.getBytes();
		out.write(data, 0, data.length);
	}
	
	public void write(int intValue) throws IOException {
		IntegerWriter.write(this, intValue);
	}
	
	public void write(long longValue) throws IOException {
		LongWriter.write(this, longValue);
	}
	
	public void write(double doubleValue) throws IOException {
		FloatingWriter.write(this, doubleValue);
	}
	
	public void write(float floatValue) throws IOException {
		FloatingWriter.write(this, floatValue);
	}
	
	private static final byte[] TRUE_BYTES = "true".getBytes();
	private static final byte[] FALSE_BYTES = "false".getBytes();
	
	public void write(boolean booleanValue) throws IOException {
		out.write(booleanValue ? TRUE_BYTES : FALSE_BYTES);
	}
}



