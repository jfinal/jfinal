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
package com.jfinal.core.typeconverter;

public class BooleanConverter implements IConverter<Boolean> {

	@Override
	public Boolean convert(String s) {
		String value = s.toLowerCase();
		if ("1".equals(value) || "true".equals(value)) {
			return Boolean.TRUE;
		}
		else if ("0".equals(value) || "false".equals(value)) {
			return Boolean.FALSE;
		}
		else {
			throw new RuntimeException("Can not parse to boolean type of value: " + s);
		}
	}
	
}
