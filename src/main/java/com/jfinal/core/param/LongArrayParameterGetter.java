package com.jfinal.core.param;

import com.jfinal.core.Controller;

public class LongArrayParameterGetter extends AbstractParameterGetter<Long[]> {

	public LongArrayParameterGetter(String parameterName) {
		super(parameterName);
	}

	@Override
	public Long[] get(Controller c) {
		return c.getParaValuesToLong(getParameterName());
	}
}
