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
 * CaptchaManager
 */
public class CaptchaManager {
	
	private static final CaptchaManager me = new CaptchaManager();
	private volatile ICaptchaCache captchaCache = null;
	
	private CaptchaManager() {}
	
	public static CaptchaManager me() {
		return me;
	}
	
	public void setCaptchaCache(ICaptchaCache captchaCache) {
		if (captchaCache == null) {
			throw new IllegalArgumentException("captchaCache can not be null");
		}
		this.captchaCache = captchaCache;
	}
	
	public ICaptchaCache getCaptchaCache() {
		ICaptchaCache cache = captchaCache;
		if (cache == null) {
			synchronized (this) {
				cache = captchaCache;
				if (cache == null) {
					cache = new CaptchaCache();
					captchaCache = cache;
				}
			}
		}
		return cache;
	}
}


