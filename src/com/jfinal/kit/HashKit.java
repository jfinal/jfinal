/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

import java.security.MessageDigest;

public class HashKit {
	
	private static java.security.SecureRandom random = new java.security.SecureRandom();
	
	public static String md5(String srcStr){
		return hash("MD5", srcStr);
	}
	
	public static String sha1(String srcStr){
		return hash("SHA-1", srcStr);
	}
	
	public static String sha256(String srcStr){
		return hash("SHA-256", srcStr);
	}
	
	public static String sha384(String srcStr){
		return hash("SHA-384", srcStr);
	}
	
	public static String sha512(String srcStr){
		return hash("SHA-512", srcStr);
	}
	
	public static String hash(String algorithm, String srcStr) {
		try {
			StringBuilder result = new StringBuilder();
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] bytes = md.digest(srcStr.getBytes("utf-8"));
			for (byte b : bytes) {
				String hex = Integer.toHexString(b&0xFF);
				if (hex.length() == 1)
					result.append("0");
				result.append(hex);
			}
			return result.toString();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String toHex(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(b&0xFF);
			if (hex.length() == 1)
				result.append("0");
			result.append(hex);
		}
		return result.toString();
	}
	
	/**
	 * md5 128bit 16bytes
	 * sha1 160bit 20bytes
	 * sha256 256bit 32bytes
	 * sha384 384bit 48bites
	 * sha512 512bit 64bites
	 */
	public static String generateSalt(int numberOfBytes) {
		byte[] salt = new byte[numberOfBytes];
		random.nextBytes(salt);
		return toHex(salt);
	}
}




