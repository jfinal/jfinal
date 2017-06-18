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

import com.jfinal.template.expr.ast.Logic;

/**
 * 针对 java.lang.Double 的扩展方法
 * 
 * 用法：
 * #if(value.toInt() == 123)
 */
public class DoubleExt {
	
	public Boolean toBoolean(Double self) {
		return Logic.isTrue(self);
	}
	
	public Integer toInt(Double self) {
		return self != null ? self.intValue() : null;
	}
	
	public Long toLong(Double self) {
		return self != null ? self.longValue() : null;
	}
	
	public Float toFloat(Double self) {
		return self != null ? self.floatValue() : null;
	}
	
	public Double toDouble(Double self) {
		return self;
	}
}



