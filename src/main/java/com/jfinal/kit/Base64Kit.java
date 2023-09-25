/**
 * Copyright (c) 2011-2017, 玛雅牛 (myaniu AT gmail dot com).
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

import java.nio.charset.Charset;

public class Base64Kit {

	public static final Charset UTF_8 = Charset.forName("UTF-8");
	private static IBase64 delegate;

	private Base64Kit() {
	}

	static {
		delegate = new Base64();
	}

	/**
	 * 编码
	 * 
	 * @param value byte数组
	 * @return {String}
	 */
	public static String encode(byte[] value) {
		return delegate.encode(value);
	}

	/**
	 * 编码
	 * 
	 * @param value 字符串
	 * @return {String}
	 */
	public static String encode(String value) {
		byte[] val = value.getBytes(UTF_8);
		return delegate.encode(val);
	}

	/**
	 * 编码
	 * 
	 * @param value       字符串
	 * @param charsetName charSet
	 * @return {String}
	 */
	public static String encode(String value, String charsetName) {
		byte[] val = value.getBytes(Charset.forName(charsetName));
		return delegate.encode(val);
	}

	/**
	 * 解码
	 * 
	 * @param value 字符串
	 * @return {byte[]}
	 */
	public static byte[] decode(String value) {
		return delegate.decode(value);
	}

	/**
	 * 解码
	 * 
	 * @param value 字符串
	 * @return {String}
	 */
	public static String decodeToStr(String value) {
		byte[] decodedValue = delegate.decode(value);
		return new String(decodedValue, UTF_8);
	}

	/**
	 * 解码
	 * 
	 * @param value       字符串
	 * @param charsetName 字符集
	 * @return {String}
	 */
	public static String decodeToStr(String value, String charsetName) {
		byte[] decodedValue = delegate.decode(value);
		return new String(decodedValue, Charset.forName(charsetName));
	}

	static interface IBase64 {
		public String encode(byte[] value);

		public byte[] decode(String value);
	}

	static class Base64 implements IBase64 {
		@Override
		public String encode(byte[] value) {
			return java.util.Base64.getEncoder().encodeToString(value);
		}

		@Override
		public byte[] decode(String value) {
			return java.util.Base64.getDecoder().decode(value);
		}
	}
}