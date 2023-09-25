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

package com.jfinal.template.io;

/**
 * JdkEncoderFactory
 * 
 * 支持 utf8mb4，支持 emoji 表情字符，支持各种罕见字符编码
 * 
 * <pre>
 * 配置方法：
 * engine.setToJdkEncoderFactory();
 * </pre>
 */
public class JdkEncoderFactory extends EncoderFactory {
	
	@Override
	public Encoder getEncoder() {
		return new JdkEncoder(charset);
	}
}



