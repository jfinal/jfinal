package com.jfinal.template;

import java.util.Date;

import com.jfinal.ext.kit.DateKit;

public class DateTest {
	public static void main(String[] args) {
       System.out.println(DateKit.toStr(new Date()));
       System.out.println(DateKit.toDate("2016-01-02 12:23:56"));
       System.out.println(DateKit.toDate("2016-01-02"));
       System.out.println(DateKit.toDate("2016.01.02 12:23:56"));
       System.out.println(DateKit.toDate("2016/01/02 12:23:56"));
       System.out.println(DateKit.toDate("2016.01.02"));
       System.out.println(DateKit.toDate("2016/01/02"));
       System.out.println(DateKit.toDate("20160102"));
	}
}
