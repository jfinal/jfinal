package com.jfinal.core.paragetter;

import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;

public class RawPostDataGetter extends ParaGetter<RawPostData>{

	public RawPostDataGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public RawPostData get(Controller c) {
		return new RawPostData(HttpKit.readData(c.getRequest()));
	}

	@Override
	protected RawPostData to(String v) {
		return new RawPostData(v);
	}
}
