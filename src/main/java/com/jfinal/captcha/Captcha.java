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

package com.jfinal.captcha;

/**
 * 验证码
 */
public class Captcha {
	
	/**
	 * 验证码默认过期时长 180 秒
	 */
	public static final int DEFAULT_EXPIRE_TIME = 180;
	
	/**
	 * 验证码 key，存放在 cookie，或者表单隐藏域中返回给客户端
	 */
	private String key;
	
	/**
	 * 验证码值
	 */
	private String value;
	
	/**
	 * 验证码过期时间
	 */
	private long expireAt;
	
	/**
	 * 验证码构造
	 * @param key
	 * @param value
	 * @param expireTime 过期时长，单位为秒
	 */
	public Captcha(String key, String value, int expireTime) {
		if (key == null || value == null) {
			throw new IllegalArgumentException("key and value can not be null");
		}
		this.key = key;
		this.value = value;
		long et = expireTime;
		this.expireAt = et * 1000 + System.currentTimeMillis();
	}
	
	public Captcha(String key, String value) {
		this(key, value, DEFAULT_EXPIRE_TIME);
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	public long getExpireAt() {
		return expireAt;
	}
	
	public boolean isExpired() {
		return expireAt < System.currentTimeMillis();
	}
	
	public boolean notExpired() {
		return !isExpired();
	}
	
	public String toString() {
		return key + " : " + value; 
	}
}


