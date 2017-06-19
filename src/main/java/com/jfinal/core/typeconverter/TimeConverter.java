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

import java.text.ParseException;

public class TimeConverter implements IConverter<java.sql.Time> {
	private static final int timePatternLen = "hh:mm:ss".length();
	private static final int timeWithoutSecPatternLen = "hh:mm".length();
	@Override
	public java.sql.Time convert(String s) throws ParseException {
		int len = s.length();
		if(len == timeWithoutSecPatternLen){
			s = s + ":00";
		}
		if(len > timePatternLen){
			s = s.substring(0, timePatternLen);
		}
		return java.sql.Time.valueOf(s);
	}
}
