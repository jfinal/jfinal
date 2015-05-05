/**
 * 
 */
package com.jfinal.ext.kit;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author 朱丛启  2015年5月3日 下午1:59:42
 *
 */
public class DateTimeKit {
	
	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
	public static String FULL_DATE_24HR_STYLE = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * yyyy-MM-dd hh:mm:ss
	 */
	public static String FULL_DATE_12HR_STYLE = "yyyy-MM-dd hh:mm:ss";
	
	/**
	 * yyyy-MM-dd HH:mm
	 */
	public static String DATE_TIME_24HR_STYLE = "yyyy-MM-dd HH:mm";
	
	/**
	 * yyyy-MM-dd hh:mm
	 */
	public static String DATE_TIME_12HR_STYLE = "yyyy-MM-dd hh:mm";
	
	/**
	 * yyyy-MM-dd
	 */
	public static String DATE_STYLE = "yyyy-MM-dd";
	
	/**
	 * HH:mm
	 */
	public static String TIME_24HR_STYLE = "HH:mm";
	
	/**
	 * hh:mm
	 */
	public static String TIME_12HR_STYLE = "hh:mm";
	

	/**
	 * 时间进制
	 * @author 朱丛启  2015年5月4日 上午10:24:46
	 *
	 */
	private enum HR{
		HR24,
		HR12,
	};
	
	private static String formatDateToFULLHRStyle(HR hr, String spacer, Date date){
		if (null == date) {
			return "";
		}
		if (null == spacer) {
			spacer = "-";
		}
		String hh = "HH";
		if (hr == HR.HR12) {
			 hh = "hh";
		}
		return (new SimpleDateFormat("yyyy"+spacer+"MM"+spacer+"dd "+hh+":mm:ss").format(date));
	}
	
	/**
	 * Calendar
	 */
	private static Calendar cal = Calendar.getInstance();
	
	private static void setTime(){
		cal.setTime(now()); 
	}
	
	private static Date now(){
		return (new Date());
	}
	
	/*=============================================TO int ===================*/
	
	/**
	 * 年
	 * @return
	 */
	public static int year(){
		setTime();
		return cal.get(Calendar.YEAR);
	}
	
	/**
	 * 月
	 */
	public static int month(){  
		setTime();  
		return cal.get(Calendar.MONTH)+1;
	}
	
	/**
	 * 某月的第几日
	 * @return
	 */
	public static int dayOfMonth(){
		setTime();
		return cal.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 某年的第几天
	 * @return
	 */
	public static int dayOfYear(){
		setTime();
		return cal.get(Calendar.DAY_OF_YEAR);
	}
	
	/**
	 * 某周的第几天
	 * 	SUN	= 7 MON = 1	TUE = 2	WED = 3	
	 * THU = 4	FRI = 5	SAT = 6
	 * @return
	 */
	public static int dayOfWeek(){
		setTime();
		int dayofweek = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayofweek == 0) {
			dayofweek = 7;
		}
		return dayofweek;
	}
	
	/*=============================================TO String===================*/
	
	/**
	 * 格式化时间
	 * @param style 
	 * @param date
	 * @see DateFormat
	 * @return
	 */
	public static String formatDate(int style,Date date){
		if (date == null) {
			return "";
		}
		if (style < 0) {
			style = DateFormat.DEFAULT;
		}
		return DateFormat.getDateInstance(style).format(date);
	}
	
	/**
	 * 格式化时间
	 * @param style 
	 * @see DateFormat
	 * @return
	 */
	public static String formatNow(int style){
		return formatDate(style, now());
	}
	
	/**
	 * 格式化时间为DateFormat.FULL风格的String => 2015年5月4日 星期一
	 * @param date
	 * @return
	 */
	public static String formatDateToFull(Date date){
		return formatDate(DateFormat.FULL, date);
	}
	
	/**
	 * 格式化当前时间为DateFormat.FULL风格的String => 2015年5月4日 星期一
	 * @return
	 */
	public static String formatNowToFull(){
		return formatNow(DateFormat.FULL);
	}
	
	/**
	 * 格式化时间为DateFormat.MEDIUM风格的String => 2015-5-4
	 * @param date
	 * @return
	 */
	public static String formatDateToMedium(Date date){
		return formatDate(DateFormat.MEDIUM, date);
	}
	
	/**
	 * 格式化当前时间为DateFormat.MEDIUM风格的String => 2015-5-4
	 * @return
	 */
	public static String formatNowToMedium(){
		return formatNow(DateFormat.MEDIUM);
	}
	
	/**
	 * 格式化时间为DateFormat.SHORT风格的String => 15-5-4 
	 * @param date
	 * @return
	 */
	public static String formatDateToShort(Date date){
		return formatDate(DateFormat.SHORT, date);
	}
	
	/**
	 * 格式化当前时间为DateFormat.SHORT风格的String => 15-5-4 
	 * @param date
	 * @return
	 */
	public static String formatNowToShort(){
		return formatNow(DateFormat.SHORT);
	}
	
	/**
	 * 格式化时间
	 * 
	 * @param style 格式风格 
	 * @param date
	 * @return
	 */
	public static String formatDateToStyle(String style, Date date){
		if (null == date) {
			return "";
		}
		if (null == style) {
			style = DateTimeKit.FULL_DATE_24HR_STYLE;
		}
		return (new SimpleDateFormat(style).format(date));
	}
	
	/**
	 * 格式化时间
	 * 
	 * @param style 格式风格 
	 * @return
	 */
	public static String formatNowToStyle(String style){
		return formatDateToStyle(style, now());
	}
	
	/**
	 * 格式化时间为Full24HR，使用spacer间隔。如：spacer为/，在return=> 2015/05/05 10:20:20
	 * @param spacer 间隔号
	 * @param date
	 * @return
	 */
	public static String formatDateToFULL24HRStyle(String spacer, Date date){
		return formatDateToFULLHRStyle(HR.HR24, spacer, date);
	}
	
	/**
	 * 格式化当前时间为Full24HR，使用spacer间隔。如：spacer为/，在return=> 2015/05/05 10:20:20
	 * @param spacer 间隔号
	 * @return
	 */
	public static String formatNowToFULL24HRStyle(String spacer){
		return formatDateToFULL24HRStyle(spacer, now());
	}
	
	/**
	 * 格式化时间为Full12HR，使用spacer间隔。如：spacer为/，在return=> 2015/05/05 18:20:20
	 * @param spacer 间隔号
	 * @param date
	 * @return
	 */
	public static String formatDateToFULL12HRStyle(String spacer, Date date){
		return formatDateToFULLHRStyle(HR.HR12, spacer, date);
	}
	
	/**
	 * 格式化当前时间为Full12HR，使用spacer间隔。如：spacer为/，在return=> 2015/05/05 18:20:20
	 * @param spacer 间隔号
	 * @param date
	 * @return
	 */
	public static String formatNowToFULL12HRStyle(String spacer){
		return formatDateToFULL12HRStyle(spacer, now());
	}
	
	/**
	 * 格式化日期为时间格式
	 * @param hr
	 * @param date
	 * @return
	 */
	public static String formatDateToHRStyle(HR hr, Date date){
		if (null == date) {
			return "";
		}
		String style = DateTimeKit.TIME_24HR_STYLE;
		if (hr == HR.HR12) {
			style = DateTimeKit.TIME_12HR_STYLE;
		}
		return formatDateToStyle(style, date);
	}
	
	/**
	 * 格式化当前日期为时间格式
	 * @param hr
	 * @param date
	 * @return
	 */
	public static String formatNowToHRStyle(HR hr){
		return formatDateToHRStyle(hr, now());
	}

	/**
	 * 格式化时间戳为制定的style
	 * @param style
	 * @param unixtime
	 * @return
	 */
	public static String formatUnixTime(String style, BigInteger unixtime){
		SimpleDateFormat sdf = new SimpleDateFormat(style);
		return sdf.format(unixtime);
	}
	
	/*=============================================TO Date ===================*/
	
	/**
	 * 时间转换
	 * @param style， DateKit.FULL_DATE_24HR_STYLE etc
	 * @param dateString
	 * @return
	 */
	public static Date dateStringToDate(String dateString){
		SimpleDateFormat sdf=new SimpleDateFormat(DateTimeKit.FULL_DATE_24HR_STYLE);
		Date date = null;
		try {
			date = sdf.parse(dateString);
		} catch (ParseException e) {}  
		return date;
	}
	
	/**
	 * BigInteger 转Date
	 * @param style
	 * @param unixtime
	 * @return
	 */
	public static Date formatUnixTimeToDate(BigInteger unixtime){
		return dateStringToDate(formatUnixTime(DateTimeKit.FULL_DATE_24HR_STYLE, unixtime));
	}

	/*=============================================TO long ===================*/

	/**
	 * 获取本月的第一天的时间戳
	 * @return
	 */
	public static Long getMonth1stDay() {
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.DATE, 1);
		return cal.getTimeInMillis();
	}

	/**
	 * 获取本月的最后一天的时间戳
	 * @return
	 */
	public static Long getMonthLastDay() {
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.DATE, 1);
		cal.roll(Calendar.DATE, -1);
		return cal.getTimeInMillis();
	}

	/**
	 * 获取给定时间的周一的长整形表示
	 * @return
	 */
	public static Long getMonday() {
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.DATE, -dayOfWeek() + 1);
		return cal.getTimeInMillis();
	}

	/**
	 * 获取给定时间的周日的长整形表示
	 * @return
	 */
	public static Long getSunday() {
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.DATE, -dayOfWeek() + 7);
		return cal.getTimeInMillis();
	}
}
