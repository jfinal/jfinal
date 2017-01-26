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

import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ICaptchaCache 默认实现，可用于单实例部署
 * 集群部署需自行实现 ICaptchaCache 接口，并使用
 * CaptchaManager.setCaptchaCache(...) 进行配置
 */
public class CaptchaCache implements ICaptchaCache {
	
	private ConcurrentHashMap<String, Captcha> map = new ConcurrentHashMap<String, Captcha>();
	private int interval = 90 * 1000;	// timer 调度间隔为 90 秒
	private Timer timer;
	
	public CaptchaCache() {
		autoRemoveExpiredCaptcha();
	}
	
	/**
	 * 定期移除过期的验证码
	 */
	private void autoRemoveExpiredCaptcha() {
		timer = new Timer("CaptchaCache", true);
		timer.schedule(
			new TimerTask() {
				public void run() {
					for (Entry<String, Captcha> e : map.entrySet()) {
						if (e.getValue().isExpired()) {
							map.remove(e.getKey());
						}
					}
				}
			},
			interval,
			interval
		);
	}
	
	public void put(Captcha captcha) {
		map.put(captcha.getKey(), captcha);
	}
	
	public Captcha get(String key) {
		return key != null ? map.get(key) : null;
	}
	
	public void remove(String key) {
		map.remove(key);
	}
	
	public void removeAll() {
		map.clear();
	}
	
	public boolean contains(String key) {
		return map.containsKey(key);
	}
}



