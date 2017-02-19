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
 * Updated by Lemond on Feb 19, 2017
 */
public class DateKit {

	public static String dateFormat = "yyyy-MM-dd";
	public static String timeFormat = "yyyy-MM-dd HH:mm:ss";

	public static void setDateFromat(String dateFormat) {
		if (StrKit.isBlank(dateFormat)) {
			throw new IllegalArgumentException("dateFormat can not be blank.");
		}
		DateKit.dateFormat = dateFormat;
	}

	public static void setTimeFromat(String timeFormat) {
		if (StrKit.isBlank(timeFormat)) {
			throw new IllegalArgumentException("timeFormat can not be blank.");
		}
		DateKit.timeFormat = timeFormat;
	}

    /**
     * 按照默认日期格式(yyyy-MM-dd)处理 dateStr 并返回{@link java.util.Date} 对象
     */
    public static Date toDate(String dateStr) {
        return toDate(dateStr, DateKit.dateFormat);
    }

    /**
     * 按照默认时间格式(yyyy-MM-dd HH:mm:ss)处理 dateStr 并返回{@link java.util.Date} 对象
     */
    public static Date toDateWithTime(String timeStr){
        return toDate(timeStr, DateKit.timeFormat);
    }

    /**
     * 按照默认/自定义格式 接受日期/时间字符串并处理为 {@link java.util.Date} 对象
     *
     * @param dateOrTimeStr 日期/时间字符串,支持自定义格式
     * @param sourceFormat dateOrTimeStr 格式
     * @return {@link java.util.Date} 对象
     */
    public static Date toDate(String dateOrTimeStr, String sourceFormat) {
        if (StrKit.isBlank(dateOrTimeStr)) {
            throw new IllegalArgumentException("dateStr can not be blank.");
        }

        if (StrKit.isBlank(sourceFormat)) {
            throw new IllegalArgumentException("sourceFormat can not be blank.");
        }

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sourceFormat);
            return simpleDateFormat.parse(dateOrTimeStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("date or time string not match format:" + sourceFormat);
        }
    }

    /**
     * 按照默认格式返回格式化后的日期字符串
     */
    public static String toStr(Date date) {
        return toStr(date, DateKit.dateFormat);
    }

    /**
     * 按照默认/自定义格式 返回格式化的日期/时间字符串
     *
     * @param date   {@link java.util.Date} 类实例
     * @param format 日期/时间格式 如果留空，返回默认格式(yyyy-MM-dd)的日期字符串，传入DateKit.timeFormat 返回格式化的时间字符串
     *               支持自定义格式
     * @return 格式化的日期字符串
     */
    public static String toStr(Date date, String format) {
        if (date == null) {
            throw new IllegalArgumentException("date can not be blank.");
        }

        if (StrKit.isBlank(format)) {
            return toStr(date);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        String dateString = simpleDateFormat.format(date);
        return dateString;
    }
}
