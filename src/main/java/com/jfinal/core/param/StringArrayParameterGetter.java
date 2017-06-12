package com.jfinal.core.param;

import com.jfinal.core.Controller;

public class StringArrayParameterGetter extends AbstractParameterGetter<String[]> {
	
	public StringArrayParameterGetter(String parameterName) {
		super(parameterName);
	}
	@Override
	public String[] get(Controller c) {
		return c.getParaValues(getParameterName());
	}
}
