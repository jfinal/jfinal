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
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AesKit {
	private AesKit() {
	}
	public static String genRandomPassword(){
		return HashKit.generateSalt(32);
	}
	public static Encrypter getAes128Encrypter(String password){
		return new Encrypter(128,password);
	}
	public static Encrypter getAes192Encrypter(String password){
		return new Encrypter(192,password);
	}
	public static Encrypter getAes256Encrypter(String password){
		return new Encrypter(256,password);
	}
	public static Decrypter getAes128Decrypter(String password){
		return new Decrypter(128,password);
	}
	public static Decrypter getAes192Decrypter(String password){
		return new Decrypter(192,password);
	}
	public static Decrypter getAes256Decrypter(String password){
		return new Decrypter(256,password);
	}
	
	static abstract class Aes{
		protected static final Charset UTF_8 = Charset.forName("UTF-8");
		protected final int keyLen;
		protected final String password;
		Aes(int keyLen,String password){
			this.keyLen = keyLen;
			this.password = password;
		}
	}
	public static class Encrypter extends Aes{
		Encrypter(int keyLen, String password){
			super(keyLen, password);
		}
		public byte[] encrypt(byte[] content) {
			if(null == content || content.length == 0){
				return content;
			}
			return encrypt(keyLen,content, password);
		}
		public byte[] encrypt(String content) {
			if(null == content){
				return null;
			}
			return encrypt(keyLen,content.getBytes(UTF_8), password);
		}
		public byte[] encrypt(String content, Charset charset){
			if(null == content){
				return null;
			}
			return encrypt(keyLen, content.getBytes(charset), password);
		}
		private static byte[] encrypt(int keyLen, byte[] content, String password){
			try {
				KeyGenerator kgen = KeyGenerator.getInstance("AES");
				SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
	            secureRandom.setSeed(password.getBytes());
				kgen.init(keyLen, secureRandom);
				SecretKey secretKey = kgen.generateKey();
				byte[] enCodeFormat = secretKey.getEncoded();
				SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
				Cipher cipher = Cipher.getInstance("AES");// 创建密码器
				cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
				byte[] result = cipher.doFinal(content);
				return result; // 加密
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	}

	public static class Decrypter extends Aes{
		Decrypter(int keyLen, String password){
			super(keyLen,password);
		}
		public byte[] decrypt(byte[] content) {
			if(null == content || content.length == 0){
				return content;
			}
			return decrypt(keyLen, content, password);
		}
		public String decryptToStr(byte[] content) {
			return decryptToStr(content, UTF_8);
		}
		public String decryptToStr(byte[] content, Charset charset) {
			if(null == content || content.length == 0){
				return "";
			}
			return new String(decrypt(content), charset);
		}
		private static byte[] decrypt(int keyLen, byte[] content, String password){
			try {
				KeyGenerator kgen = KeyGenerator.getInstance("AES");
				SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
	            secureRandom.setSeed(password.getBytes());
				kgen.init(keyLen, secureRandom);
				SecretKey secretKey = kgen.generateKey();
				byte[] enCodeFormat = secretKey.getEncoded();
				SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
				Cipher cipher = Cipher.getInstance("AES");// 创建密码器
				cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
				byte[] result = cipher.doFinal(content);
				return result; // 加密
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	}
}
