package com.jfinal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 控制器注解
 * 说明：标注Controller和访问路径
 * @author 董华健
 * 
 * 描述：如果属性名称为value，在使用注解时可以不用指定属性名称
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Ctrl {
	
	/**
	 * 控制器路径，可以配置多个路径数组
	 * @return
	 */
    String[] value();

    /**
     * 视图模板基础路径
     * @return
     */
    String viewPath() default "";
    
}
