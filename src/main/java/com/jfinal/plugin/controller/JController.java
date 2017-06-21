package com.jfinal.plugin.controller;

/**
 * Created by GongRui on 5/12/2017.
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JController {
    String reqPath() default "/";
    String viewPath() default "";
}