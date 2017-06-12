package com.jfinal.core.param;

import java.text.ParseException;
import java.util.Date;

import com.jfinal.core.Controller;

public class DateParameterGetter extends AbstractParameterGetter<Date> {
	
	public DateParameterGetter(String parameterName) {
		super(parameterName, null);
	}
	
	public DateParameterGetter(String parameterName, String defaultValue) {
		super(parameterName, toDate(defaultValue));
	}

	public DateParameterGetter(String parameterName, Date defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public Date get(Controller c) {
		return c.getParaToDate(getParameterName(), getDefaultValue());
	}
	
	private static Date toDate(String value) {
		if (value == null || "".equals(value.trim()))
			return null;
		try {
			return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(value);
		} catch (ParseException e) {
			return null;
		}
	}

}
