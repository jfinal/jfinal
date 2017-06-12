package com.jfinal.core.param;

import com.jfinal.core.Controller;

public class BooleanParameterGetter extends AbstractParameterGetter<Boolean> {
	
	public BooleanParameterGetter(String parameterName) {
		super(parameterName,Boolean.FALSE);
	}
	
	public BooleanParameterGetter(String parameterName, String defaultValue) {
		super(parameterName,Boolean.parseBoolean(defaultValue));
	}

	public BooleanParameterGetter(String parameterName, Boolean defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Boolean get(Controller c) {
		return c.getParaToBoolean(getParameterName(),getDefaultValue());
	}

}
