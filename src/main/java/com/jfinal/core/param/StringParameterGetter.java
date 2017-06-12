package com.jfinal.core.param;

import com.jfinal.core.Controller;

public class StringParameterGetter extends AbstractParameterGetter<String> {
	
	public StringParameterGetter(String parameterName){
		super(parameterName, "");
	}
	
	public StringParameterGetter(String parameterName, String defaultValue){
		super(parameterName, defaultValue);
	}

	@Override
	public String get(Controller c) {
		return c.getPara(getParameterName(), getDefaultValue());
	}
}
