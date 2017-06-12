package com.jfinal.core.param;

import com.jfinal.core.Controller;

public class IntParameterGetter extends AbstractParameterGetter<Integer> {

	public IntParameterGetter(String parameterName) {
		super(parameterName, 0);
	}
	
	public IntParameterGetter(String parameterName, String defaultValue) {
		super(parameterName,Integer.parseInt(defaultValue));
	}
	
	public IntParameterGetter(String parameterName, Integer defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Integer get(Controller c) {
		return c.getParaToInt(getParameterName(),getDefaultValue());
	}

}
