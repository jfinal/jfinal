package com.jfinal.core.param;

import com.jfinal.core.Controller;

public class LongParameterGetter extends AbstractParameterGetter<Long> {

	public LongParameterGetter(String parameterName) {
		super(parameterName, 0L);
	}
	
	public LongParameterGetter(String parameterName, String defaultValue) {
		super(parameterName,Long.parseLong(defaultValue));
	}
	
	public LongParameterGetter(String parameterName, Long defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Long get(Controller c) {
		return c.getParaToLong(getParameterName(),getDefaultValue());
	}
}
