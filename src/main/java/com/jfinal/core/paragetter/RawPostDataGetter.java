package com.jfinal.core.paragetter;

import com.jfinal.core.Action;
import com.jfinal.core.Controller;

public class RawPostDataGetter extends ParaGetter<RawPostData>{

	public RawPostDataGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public RawPostData get(Action action, Controller c) {
		return new RawPostData(c.getRawData());
	}

	@Override
	protected RawPostData to(String v) {
		return new RawPostData(v);
	}
}
