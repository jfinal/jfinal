package com.jfinal.core.param;

import com.jfinal.core.Controller;
/**
 * 参数获取器
 * @author dafei
 */
public interface ParameterGetter<T> {
	public T get(Controller c);
}
