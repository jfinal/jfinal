package com.jfinal.core.paragetter;

import com.jfinal.core.Action;
import com.jfinal.core.Controller;

public class RawDataGetter extends ParaGetter<RawData>{

	public RawDataGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public RawData get(Action action, Controller c) {
		return new RawData(c.getRawData());
	}

	@Override
	protected RawData to(String v) {
		return new RawData(v);
	}
}
