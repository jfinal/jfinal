package com.jfinal.core.param;

import com.jfinal.core.Controller;

public class NullParameterGetter extends AbstractParameterGetter<Object> {

	public NullParameterGetter(String parameterName) {
		super(parameterName);
	}

	@Override
	public Object get(Controller c) {
		return null;
	}
}
