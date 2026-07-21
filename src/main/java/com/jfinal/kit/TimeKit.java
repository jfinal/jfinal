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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.IsoEra;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TimeKit 用于简化 JDK 8 新增的时间 API
 *
 * <pre>
 * 新旧日期转换通过桥梁 Instant 进行，转成 LocalDate、LocalTime 需要先转成 LocalDateTime：
 *   新转旧：LocalDateTime.atZone(ZoneId).toInstant() -> Instant -> Date.from(Instant)
 *   旧转新：Date.toInstant() -> Instant -> LocalDateTime.ofInstant(Instant, ZoneId)
 *
 * getDateTimeFormatter 返回的格式化器在解析数字字段时不强制匹配 pattern 的位数，例如
 * pattern 为 "yyyy-MM-dd HH:mm:ss" 时可以解析 "2026-1-2 3:4:5"，但会拒绝不存在的日期
 *
 * 备忘：不要提供 java.util.Date toDate(java.time.LocalTime)
 * </pre>
 */
public class TimeKit {

    /**
     * 缓存线程安全的 DateTimeFormatter
     */
    private static final ConcurrentHashMap<String, DateTimeFormatter> formatters = new ConcurrentHashMap<>();

    /**
     * 结合 ThreadLocal 缓存 "非线程安全" 的 SimpleDateFormat，使用严格解析模式
     */
    private static final ThreadLocal<HashMap<String, SimpleDateFormat>> TL = ThreadLocal.withInitial(HashMap::new);

    public static DateTimeFormatter getDateTimeFormatter(String pattern) {
        return formatters.computeIfAbsent(pattern, TimeKit::createDateTimeFormatter);
    }

    /**
     * 创建数字字段输入宽度宽松、日期合法性校验严格的 DateTimeFormatter
     */
    private static DateTimeFormatter createDateTimeFormatter(String pattern) {
        return new DateTimeFormatterBuilder()
                // 作用于后续追加的解析规则，允许数字字段使用较少位数
                .parseLenient()
                .appendPattern(pattern)
                // yyyy 对应 YEAR_OF_ERA，严格模式下需要默认补充公元纪元
                .parseDefaulting(ChronoField.ERA, IsoEra.CE.getValue())
                // 仅有日期时默认为当天零点，分、秒和纳秒由解析器自动补零
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .toFormatter()
                // 拒绝越界值和不存在的日期，如 "2020-2-30"
                .withResolverStyle(ResolverStyle.STRICT);
    }

    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        SimpleDateFormat ret = TL.get().get(pattern);
        if (ret == null) {
            ret = new SimpleDateFormat(pattern);
            // 允许数字字段不补零，但拒绝越界值和不存在的日期
            ret.setLenient(false);
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
     * 按 pattern "yyyy-MM-dd HH:mm:ss" 将当前时间转换成 String
     */
    public static String now() {
        return now("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 按 pattern "yyyyMMddHHmmssSSS" 将当前时间精确到毫秒转换成 String，常用于生成订单号等等单据的部分字符
     */
    public static String nowWithMillisecond() {
        return now("yyyyMMddHHmmssSSS");
    }

    /**
     * 按指定 pattern 将 LocalDateTime 转换成 String
     * 例如：format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss")
     */
    public static String format(LocalDateTime localDateTime, String pattern) {
        return localDateTime.format(getDateTimeFormatter(pattern));
    }

    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, "yyyy-MM-dd HH:mm:ss");
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

    public static String format(Date date) {
        return format(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 按指定 pattern 将整个 String 转换成 Date
     */
    public static Date parse(String dateString, String pattern) {
        ParsePosition position = new ParsePosition(0);
        Date ret = getSimpleDateFormat(pattern).parse(dateString, position);
        if (ret == null || position.getIndex() != dateString.length()) {
            throw new IllegalArgumentException("Invalid date string \"" + dateString + "\" for pattern \"" + pattern + "\".");
        }
        return ret;
    }

    /**
     * 自动探测 pattern 将 String 转换成 Date
     */
    public static Date parse(String dateString) {
        String pattern = detectDatePattern(dateString);
        return parse(dateString, pattern);
    }

    private static String detectDatePattern(String dateString) {
        int blankIndex = dateString.indexOf(' ');
        if (blankIndex == -1) {
            return "yyyy-MM-dd";
        }
        int firstColonIndex = dateString.indexOf(':', blankIndex + 1);
        if (firstColonIndex == -1) {
            return "yyyy-MM-dd HH";
        }
        int secondColonIndex = dateString.indexOf(':', firstColonIndex + 1);
        if (secondColonIndex == -1) {
            return "yyyy-MM-dd HH:mm";
        }
        int dotIndex = dateString.indexOf('.', secondColonIndex + 1);
        if (dotIndex == -1) {
            return "yyyy-MM-dd HH:mm:ss";
        }
        if (dateString.length() - dotIndex - 1 != 3) {
            throw new IllegalArgumentException("Millisecond precision must contain exactly 3 digits: \"" + dateString + "\"");
        }
        return "yyyy-MM-dd HH:mm:ss.SSS";
    }

    /**
     * 按指定 pattern 将 String 转换成 LocalDateTime
     */
    public static LocalDateTime parseLocalDateTime(String localDateTimeString, String pattern) {
        return LocalDateTime.parse(localDateTimeString, getDateTimeFormatter(pattern));
    }

    /**
     * 自动探测 pattern 将 String 转换成 LocalDateTime
     */
    public static LocalDateTime parseLocalDateTime(String localDateTimeString) {
        String pattern = detectDatePattern(localDateTimeString);
        return parseLocalDateTime(localDateTimeString, pattern);
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
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate().atStartOfDay();
        } else if (date instanceof java.sql.Time) {
            throw new IllegalArgumentException("Cannot convert java.sql.Time to LocalDateTime without a date.");
        }

        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * java.util.Date --> java.time.LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        } else if (date instanceof java.sql.Time) {
            throw new IllegalArgumentException("Cannot convert java.sql.Time to LocalDate without a date.");
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
        if (date instanceof java.sql.Time) {
            return ((java.sql.Time) date).toLocalTime();
        } else if (date instanceof java.sql.Date) {
            throw new IllegalArgumentException("Cannot convert java.sql.Date to LocalTime without a time.");
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
        Instant instant = localDate.atStartOfDay(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * java.time.LocalDate + java.time.LocalTime --> java.util.Date
     */
    public static Date toDate(LocalDate localDate, LocalTime localTime) {
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * 将 LocalDateTime 转为 long 类型
     * <pre>
     * 例子：
     *     toLong(LocalDateTime.now(), 3);
     * </pre>
     * @param localDateTime LocalDateTime 类型数据
     * @param type 转换类型，取值必须为：1、2、3、4、5、6、7 的其中一个，分别表示转换精度为：年、月、日、时、分、秒、毫秒
     */
    public static long toLong(LocalDateTime localDateTime, int type) {
        String pattern;
        switch (type) {
            case 1: pattern = "yyyy";break;
            case 2: pattern = "yyyyMM";break;
            case 3: pattern = "yyyyMMdd";break;
            case 4: pattern = "yyyyMMddHH";break;
            case 5: pattern = "yyyyMMddHHmm";break;
            case 6: pattern = "yyyyMMddHHmmss";break;
            case 7: pattern = "yyyyMMddHHmmssSSS";break;
            default : throw new IllegalArgumentException("参数 type 必须为 1 到 7 的整数，分别表示从年到毫秒级别的转换");
        }
        return Long.parseLong(TimeKit.format(localDateTime, pattern));
    }

    /**
     * 当前时间转换为 long 值。例如 nowToLong(7) 转换为 20260123112233123
     */
    public static long nowToLong(int type) {
        return toLong(LocalDateTime.now(), type);
    }
}
