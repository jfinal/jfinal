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

package com.jfinal.kit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * TimeKit 用于简化 JDK 8 新增的时间 API
 * 
 * 新旧日期转换通过桥梁 Instant 进行，转成 LocalDate、LocalTime 需要先转成 LocalDateTime：
 *   新转旧：LocalDateTime.atZone(ZoneId).toInstant() -> Instant -> Date.from(Instant)
 *   旧转新：Date.toInstant() -> Instant -> LocalDateTime.ofInstant(Instant, ZoneId)
 * 
 * 经测试，SimpleDateFormat 比 DateTimeFormatter 对 pattern 的支持更好
 * 对于同样的 pattern 值 "yyyy-MM-dd HH:mm:ss"，前者可以转换 "2020-06-9 12:13:19"
 * 后者却不支持，原因是 pattern 的 dd 位置只有数字 9，必须要是两位数字才能支持
 * 
 * 
 * 所以：建议优先使用转换结果为 Date 的 parse 方法，使用 SimpleDateFormat 来转换
 */
public class TimeKit {
	
	/**
	 * 缓存线程安全的 DateTimeFormatter
	 */
	private static final Map<String, DateTimeFormatter> formaters = new SyncWriteMap<>();
	
	public static DateTimeFormatter getDateTimeFormatter(String pattern) {
		DateTimeFormatter ret = formaters.get(pattern);
		if (ret == null) {
			ret = DateTimeFormatter.ofPattern(pattern);
			formaters.put(pattern, ret);
		}
		return ret;
	}
	
	/**
	 * 结合 ThreadLocal 缓存 "非线程安全" 的 SimpleDateFormat
	 */
	private static final ThreadLocal<HashMap<String, SimpleDateFormat>> TL = ThreadLocal.withInitial(() -> new HashMap<>());
	
	public static SimpleDateFormat getSimpleDateFormat(String pattern) {
		SimpleDateFormat ret = TL.get().get(pattern);
		if (ret == null) {
			ret = new SimpleDateFormat(pattern);
			TL.get().put(pattern, ret);
		}
		return ret;
	}
	
	/**
	 * 按指定 pattern 将当前时间转换成 String
	 * 例如：now("yyyy-MM-dd HH:mm:ss")
	 */
	public static String now(String pattern) {
		return LocalDateTime.now().format(getDateTimeFormatter(pattern));
	}
	
	/**
	 * 按指定 pattern 将 LocalDateTime 转换成 String
	 * 例如：format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss")
	 */
	public static String format(LocalDateTime localDateTime, String pattern) {
		return localDateTime.format(getDateTimeFormatter(pattern));
	}
	
	/**
	 * 按指定 pattern 将 LocalDate 转换成 String
	 */
	public static String format(LocalDate localDate, String pattern) {
		return localDate.format(getDateTimeFormatter(pattern));
	}
	
	/**
	 * 按指定 pattern 将 LocalTime 转换成 String
	 */
	public static String format(LocalTime localTime, String pattern) {
		return localTime.format(getDateTimeFormatter(pattern));
	}
	
	/**
	 * 按指定 pattern 将 Date 转换成 String
	 * 例如：format(new Date(), "yyyy-MM-dd HH:mm:ss")
	 */
	public static String format(Date date, String pattern) {
		return getSimpleDateFormat(pattern).format(date);
	}
	
	/**
	 * 按指定 pattern 将 String 转换成 Date
	 */
	public static Date parse(String dateString, String pattern) {
		try {
			return getSimpleDateFormat(pattern).parse(dateString);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 按指定 pattern 将 String 转换成 LocalDateTime
	 */
	public static LocalDateTime parseLocalDateTime(String localDateTimeString, String pattern) {
		return LocalDateTime.parse(localDateTimeString, getDateTimeFormatter(pattern));
	}
	
	/**
	 * 按指定 pattern 将 String 转换成 LocalDate
	 */
	public static LocalDate parseLocalDate(String localDateString, String pattern) {
		return LocalDate.parse(localDateString, getDateTimeFormatter(pattern));
	}
	
	/**
	 * 按指定 pattern 将 String 转换成 LocalTime
	 */
	public static LocalTime parseLocalTime(String localTimeString, String pattern) {
		return LocalTime.parse(localTimeString, getDateTimeFormatter(pattern));
	}
	
	/**
	 * 判断 A 的时间是否在 B 的时间 "之后"
	 */
	public static boolean isAfter(ChronoLocalDateTime<?> self, ChronoLocalDateTime<?> other) {
		return self.isAfter(other);
	}
	
	/**
	 * 判断 A 的时间是否在 B 的时间 "之前"
	 */
	public static boolean isBefore(ChronoLocalDateTime<?> self, ChronoLocalDateTime<?> other) {
		return self.isBefore(other);
	}
	
	/**
	 * 判断 A 的时间是否与 B 的时间 "相同"
	 */
	public static boolean isEqual(ChronoLocalDateTime<?> self, ChronoLocalDateTime<?> other) {
		return self.isEqual(other);
	}
	
	/**
	 * java.util.Date --> java.time.LocalDateTime
	 */
	public static LocalDateTime toLocalDateTime(Date date) {
		// java.sql.Date 不支持 toInstant()，需要先转换成 java.util.Date
		if (date instanceof java.sql.Date) {
			date = new Date(date.getTime());
		}
		
		Instant instant = date.toInstant();
		ZoneId zone = ZoneId.systemDefault();
		return LocalDateTime.ofInstant(instant, zone);
	}
	
	/**
	 * java.util.Date --> java.time.LocalDate
	 */
	public static LocalDate toLocalDate(Date date) {
		// java.sql.Date 不支持 toInstant()，需要先转换成 java.util.Date
		if (date instanceof java.sql.Date) {
			date = new Date(date.getTime());
		}
		
		Instant instant = date.toInstant();
		ZoneId zone = ZoneId.systemDefault();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		return localDateTime.toLocalDate();
	}
	
	/**
	 * java.util.Date --> java.time.LocalTime
	 */
	public static LocalTime toLocalTime(Date date) {
		// java.sql.Date 不支持 toInstant()，需要先转换成 java.util.Date
		if (date instanceof java.sql.Date) {
			date = new Date(date.getTime());
		}
		
		Instant instant = date.toInstant();
		ZoneId zone = ZoneId.systemDefault();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		return localDateTime.toLocalTime();
	}
	
	/**
	 * java.time.LocalDateTime --> java.util.Date
	 */
	public static Date toDate(LocalDateTime localDateTime) {
		ZoneId zone = ZoneId.systemDefault();
		Instant instant = localDateTime.atZone(zone).toInstant();
		return Date.from(instant);
	}
	
	/**
	 * java.time.LocalDate --> java.util.Date
	 */
	public static Date toDate(LocalDate localDate) {
		ZoneId zone = ZoneId.systemDefault();
		Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
		return Date.from(instant);
	}
	
	/**
	 * java.time.LocalTime --> java.util.Date
	 */
	public static Date toDate(LocalTime localTime) {
		LocalDate localDate = LocalDate.now();
		LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
		ZoneId zone = ZoneId.systemDefault();
		Instant instant = localDateTime.atZone(zone).toInstant();
		return Date.from(instant);
	}
	
	/**
	 * java.time.LocalTime --> java.util.Date
	 */
	public static Date toDate(LocalDate localDate, LocalTime localTime) {
		LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
		ZoneId zone = ZoneId.systemDefault();
		Instant instant = localDateTime.atZone(zone).toInstant();
		return Date.from(instant);
	}
}





