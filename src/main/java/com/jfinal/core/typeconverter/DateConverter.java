package com.jfinal.core.typeconverter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateConverter implements IConverter<java.util.Date> {
	private static final String timeStampPattern = "yyyy-MM-dd HH:mm:ss";
	private static final String datePattern = "yyyy-MM-dd";
	private static final int dateLen = datePattern.length();
	private static final int timeStampWithoutSecPatternLen = "yyyy-MM-dd HH:mm".length();
	@Override
	public java.util.Date convert(String s) throws ParseException {
		if(timeStampWithoutSecPatternLen == s.length()){
			s = s + ":00";
		}
		if (s.length() > dateLen) {	// if (x < timeStampLen) 改用 datePattern 转换，更智能
			// Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]
			// return new java.util.Date(java.sql.Timestamp.valueOf(s).getTime());	// error under jdk 64bit(maybe)
			return new SimpleDateFormat(timeStampPattern).parse(s);
		}
		else {
			// return new java.util.Date(java.sql.Date.valueOf(s).getTime());	// error under jdk 64bit
			return new SimpleDateFormat(datePattern).parse(s);
		}
	}
	
}
