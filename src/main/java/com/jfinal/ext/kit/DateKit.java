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

package com.jfinal.ext.kit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.jfinal.kit.StrKit;

/**
 * DateKit.
 */
public class DateKit {
	
	public static String datePattern = "yyyy-MM-dd";
	public static String timeStampPattern = "yyyy-MM-dd HH:mm:ss";
	
	public static void setDatePattern(String datePattern) {
		if (StrKit.isBlank(datePattern)) {
			throw new IllegalArgumentException("datePattern can not be blank");
		}
		DateKit.datePattern = datePattern;
	}
	
	public static void setTimeStampPattern(String timeStampPattern) {
		if (StrKit.isBlank(timeStampPattern)) {
			throw new IllegalArgumentException("timeStampPattern can not be blank");
		}
		DateKit.timeStampPattern = timeStampPattern;
	}
	
	public static Date toDate(String dateStr) {
		if (StrKit.isBlank(dateStr)) {
			return null;
		}
		
		dateStr = dateStr.trim();
		int length = dateStr.length();
		try {
			if (length == timeStampPattern.length()) {
				SimpleDateFormat sdf = new SimpleDateFormat(timeStampPattern);
				try {
					return sdf.parse(dateStr);
				} catch (ParseException e) {
					dateStr = dateStr.replace(".", "-");
					dateStr = dateStr.replace("/", "-");
					return sdf.parse(dateStr);
				}
			} else if (length == datePattern.length()) {
				SimpleDateFormat sdfDate = new SimpleDateFormat(datePattern);
				try {
					return sdfDate.parse(dateStr);
				} catch (ParseException e) {
					dateStr = dateStr.replace(".", "-");
					dateStr = dateStr.replace("/", "-");
					return sdfDate.parse(dateStr);
				}
			} else {
				throw new IllegalArgumentException("The date format is not supported for the time being");
			}
		} catch (ParseException e) {
			throw new IllegalArgumentException("The date format is not supported for the time being");
		}
	}
	
	public static String toStr(Date date) {
		return toStr(date, DateKit.datePattern);
	}
	
	public static String toStr(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
}






