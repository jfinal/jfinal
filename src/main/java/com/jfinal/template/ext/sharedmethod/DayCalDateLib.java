package com.jfinal.template.ext.sharedmethod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

/**
 * sql模板日期计算类.
 *
 * @author liuyanwei
 * @date 2020/11/30
 */
public class DayCalDateLib {
    
    /**
     * 预设不同的时间格式.
     */
    public static final String FORMAT_STDAY1 = "yyyy-MM-dd";
    
    public static final String FORMAT_STDAY2 = "yyyy/MM/dd";
    
    public static final String FORMAT_STDAY = "yyyyMMdd";
    
    private static final String FORMAT_STR1 = "-";
    
    private static final String FORMAT_STR2 = "/";
    
    public static final String COMMA = ",";
    
    private static final Pattern IS_NUMBER = Pattern.compile("^-?[1-9]+[0-9]*$|^0$");
    
    private static final Pattern IS_MONTH = Pattern.compile("^(0?[1-9]|1[0-2])$");
    
    private static final Pattern IS_MONTH_DAY = Pattern.compile("^((0?[1-9])|((1|2)[0-9])|30|31)$");
    
    private static final Pattern IS_PERFIXDAY = Pattern.compile("^-?[0-9]*([DMY]?)$");
    
    private static final String TIME_TYPE_D = "D";
    
    private static final String TIME_TYPE_M = "M";
    
    private static final String TIME_TYPE_Y = "Y";
    
    private static final String MONTH_END = "ED";
    
    private static final int LENGTH_FORMATDAY8 = 8;
    
    private static final int LENGTH_FORMATDAY10 = 10;
    
    /**
     * 按day先计算YYYYMMDD后计算-1D.
     *
     * @param calRule : (day,-1D,YYYYMMDD)
     * @param day     : YYYYMMDD
     * @return dayArrary[2]
     */
    public static String dayCalculateFormatter(String calRule, String day) {
        String dateStr = "";
        String[] dayRule = calRule.split(COMMA);
        //检查参数是否合法
        checkRule(dayRule[1], dayRule[2]);
        //计算日期
        Date date = getRuleDate(day, dayRule[1], dayRule[2]);
        //格式化计算输出日期
        if (dayRule[2].length() == LENGTH_FORMATDAY8) {
            dateStr = format(date, FORMAT_STDAY);
        } else if (dayRule[2].length() == LENGTH_FORMATDAY10 && dayRule[2].contains(FORMAT_STR1)) {
            dateStr = format(date, FORMAT_STDAY1);
        } else if (dayRule[2].length() == LENGTH_FORMATDAY10 && dayRule[2].contains(FORMAT_STR2)) {
            dateStr = format(date, FORMAT_STDAY2);
        } else {
            throw new IllegalArgumentException("模板中日期计算格式错误,应符合形式 [day,-1D,YYYYMMDD]");
        }
        return dateStr;
    }
    
    /**
     * 格式校验 (day,-1D,YYYYMMDD).
     *
     * @param perfixDay 正则规则校验
     * @param formatDay 8位或10位日期格式校验
     */
    private static void checkRule(String perfixDay, String formatDay) {
        
        if (!(IS_PERFIXDAY.matcher(perfixDay).matches())) {
            throw new IllegalArgumentException("模板中日期计算格式错误,应符合形式 [day,-1D,YYYYMMDD]");
        }
        
        if (formatDay.length() != LENGTH_FORMATDAY8 && formatDay.length() != LENGTH_FORMATDAY10) {
            throw new IllegalArgumentException("模板中日期计算格式错误,应符合形式 [day,-1D,YYYYMMDD]");
        }
    }
    
    /**
     * 按规则计算 (day,-1D,YYYYMMDD) 日期.
     *
     * @param day       日期,默认格式 yyyyMMdd
     * @param perfixDay 日期规则
     * @param formatDay 格式规则
     * @return Date
     * @see #getFormatDay(Date, String)
     * @see #getPerfixDay(Date, String)
     */
    private static Date getRuleDate(String day, String perfixDay, String formatDay) {
        Date date = parse(day, FORMAT_STDAY);
        date = getFormatDay(date, formatDay);
        date = getPerfixDay(date, perfixDay);
        return date;
    }
    
    /**
     * 根据日期格式设置日期,支持固定日期
     * 最后两位YYYYMMED代表月末.
     *
     * @param date      日期,默认格式 yyyyMMdd
     * @param formatDay 格式规则
     * @return Date
     */
    private static Date getFormatDay(Date date, String formatDay) {
        String year = "";
        String month = "";
        String day = "";
        if (formatDay.length() == LENGTH_FORMATDAY8) {
            year = formatDay.substring(0, 4);
            month = formatDay.substring(4, 6);
            day = formatDay.substring(6, 8);
        }
        if (formatDay.length() == LENGTH_FORMATDAY10) {
            year = formatDay.substring(0, 4);
            month = formatDay.substring(5, 7);
            day = formatDay.substring(8, 10);
        }
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        //设置年
        if (IS_NUMBER.matcher(year).matches()) {
            gc.set(Calendar.YEAR, Integer.valueOf(year));
        }
        //设置月
        if (IS_MONTH.matcher(month).matches()) {
            gc.set(Calendar.MONTH, Integer.valueOf(month) - 1);
        }
        //设置日
        if (IS_MONTH_DAY.matcher(day).matches()) {
            gc.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
        }
        //设置月末
        if (day.equals(MONTH_END)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(gc.getTime());
            int maxDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            gc.set(Calendar.DAY_OF_MONTH, maxDayOfMonth);
        }
        return gc.getTime();
    }
    
    /**
     * 根据日期偏移量设置日期,依据正则: ^-?[0-9]*([DMY]?)$
     * -1 或 -1D 前一日,30 或 30D 后30日,
     * -1M 前一月,1M 后一月,
     * -1Y 前一年,1Y 后一年.
     *
     * @param date      日期,默认格式 yyyyMMdd
     * @param perfixDay 日期规则
     * @return Date
     */
    private static Date getPerfixDay(Date date, String perfixDay) {
        
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        int perfixDayLength = perfixDay.length();
        //判断是否是纯数字
        if (IS_NUMBER.matcher(perfixDay).matches()) {
            int days = Integer.valueOf(perfixDay);
            gc.add(Calendar.DAY_OF_MONTH, days);
        } else {
            String timeType = perfixDay.substring(perfixDayLength - 1, perfixDayLength);
            if (timeType.equals(TIME_TYPE_D)) {
                int days = Integer.valueOf(perfixDay.substring(0, perfixDayLength - 1));
                gc.add(Calendar.DAY_OF_MONTH, days);
            } else if (timeType.equals(TIME_TYPE_M)) {
                int months = Integer.valueOf(perfixDay.substring(0, perfixDayLength - 1));
                gc.add(Calendar.MONTH, months);
            } else if (timeType.equals(TIME_TYPE_Y)) {
                int years = Integer.valueOf(perfixDay.substring(0, perfixDayLength - 1));
                gc.add(Calendar.YEAR, years);
            }
        }
        return gc.getTime();
    }
    
    /**
     * 自定义时间格式：Stirng->Date.
     *
     * @param strDate stirng日期
     * @param format  stirng日期格式
     * @return Date
     */
    public static Date parse(String strDate, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 自定义格式格式化日期 Date->Stirng.
     *
     * @param date   日期
     * @param format 日期格式
     * @return String
     */
    public static String format(Date date, String format) {
        String value = "";
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            value = sdf.format(date);
        }
        return value;
    }
    
    /**
     * 返回默认日期格式.
     *
     * @param date 支持格式 yyyy-MM-dd ,yyyy/MM/dd ,yyyyMMdd
     * @return 返回默认日期格式 yyyyMMdd
     */
    public static String formatPattern(String date) {
        String value = "";
        //格式化计算输出日期
        if (date.length() == LENGTH_FORMATDAY8) {
            value = date.trim();
        } else if (date.length() == LENGTH_FORMATDAY10 && date.contains(FORMAT_STR1)) {
            value = date.trim().replace(FORMAT_STR1, "");
        } else if (date.length() == LENGTH_FORMATDAY10 && date.contains(FORMAT_STR2)) {
            value = date.trim().replace(FORMAT_STR2, "");
        } else {
            throw new IllegalArgumentException("模板中日期计算格式错误,应符合形式 [day,-1D,YYYYMMDD]");
        }
        return value;
    }
}
