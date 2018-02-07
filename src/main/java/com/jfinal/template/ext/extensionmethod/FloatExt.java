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

package com.jfinal.template.ext.extensionmethod;

/**
 * 针对 java.lang.Float 的扩展方法
 * 
 * 用法：
 * #if(value.toInt() == 123)
 */
public class FloatExt {
	
	public Boolean toBoolean(Float self) {
		return self != 0;
	}
	
	public Integer toInt(Float self) {
		return self.intValue();
	}
	
	public Long toLong(Float self) {
		return self.longValue();
	}
	
	public Float toFloat(Float self) {
		return self;
	}
	
	public Double toDouble(Float self) {
		return self.doubleValue();
	}
	
	public Short toShort(Float self) {
		return self.shortValue();
	}
	
	public Byte toByte(Float self) {
		return self.byteValue();
	}
}



