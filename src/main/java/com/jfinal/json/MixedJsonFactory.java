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

package com.jfinal.json;

/**
 * IJsonFactory 的 jfinal + fastjson 组合实现
 * 
 * 1：toJson 用 JFinalJson，parse 用 FastJson
 * 2：需要添加 fastjson 相关 jar 包
 * 3：parse 方法转对象依赖于 setter 方法
 */
public class MixedJsonFactory implements IJsonFactory {
	
	private static final MixedJsonFactory me = new MixedJsonFactory();
	
	public MixedJsonFactory() {
	    // 尽早触发 fastjson 的配置代码
        new FastJson();
	}
	
	public static MixedJsonFactory me() {
		return me;
	}
	
	public Json getJson() {
		return new MixedJson();
	}
}
