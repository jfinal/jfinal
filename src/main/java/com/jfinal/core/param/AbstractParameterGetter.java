package com.jfinal.core.param;

public abstract class AbstractParameterGetter<T> implements ParameterGetter<T> {
	private final String parameterName;
	private final T defaultValue;
	
	protected final String getParameterName() {
		return parameterName;
	}
	
	protected final T getDefaultValue() {
		return defaultValue;
	}
	
	public AbstractParameterGetter(String parameterName){
		this.parameterName = parameterName;
		this.defaultValue = null;
	}
	
	public AbstractParameterGetter(String parameterName, T defaultValue){
		this.parameterName = parameterName;
		this.defaultValue = defaultValue;
	}
}
