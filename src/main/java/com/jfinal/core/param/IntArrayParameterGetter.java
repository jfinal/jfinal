package com.jfinal.core.param;

import com.jfinal.core.Controller;

public class IntArrayParameterGetter extends AbstractParameterGetter<Integer[]> {
	
	public IntArrayParameterGetter(String parameterName) {
		super(parameterName);
	}

	@Override
	public Integer[] get(Controller c) {
		return c.getParaValuesToInt(getParameterName());
	}
}
