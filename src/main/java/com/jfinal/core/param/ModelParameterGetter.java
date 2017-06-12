package com.jfinal.core.param;

import com.jfinal.core.Controller;

public class ModelParameterGetter<T> extends AbstractParameterGetter<T> {

	private final Class<T> modelClass;
	public ModelParameterGetter(Class<T> modelClass, String parameterName) {
		super(parameterName);
		this.modelClass = modelClass;
	}
	@Override
	public T get(Controller c) {
		return c.getModel(modelClass, this.getParameterName());
	}
}
