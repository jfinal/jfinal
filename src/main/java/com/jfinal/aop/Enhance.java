package com.jfinal.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enhance 用于配置被注入对象是否要增强
 * 
 * 由于下一版本的 jfinal 3.6 将根据目标类中是否存在 Before 注解
 * 来决定是否增强，所以该 Enhance 仅仅是一个过渡功能，不建议使用
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Deprecated
public @interface Enhance {
	boolean value();				// 是否增强
}
