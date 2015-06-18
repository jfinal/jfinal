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

package com.jfinal.plugin.redis;

/**
 * IKeyNamingPolicy.
 * 架构师可以通过实现此类制定全局性的 key 命名策略，
 * 例如 Integer、String、OtherType 这些不同类型的对象
 * 选择不同的命名方式，默认命名方式是  Object.toString()
 */
public interface IKeyNamingPolicy {
	
	String getKeyName(Object key);
	
	static final IKeyNamingPolicy defaultKeyNamingPolicy = new IKeyNamingPolicy() {
		public String getKeyName(Object key) {
			return key.toString();
		}
	};
}




