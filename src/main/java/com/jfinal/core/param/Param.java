package com.jfinal.core.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: Param  
 * @Description:  自动绑定入口函数的参数到http请求中的一个参数
 * @author 李飞  
 * @date 2014年10月19日 下午5:19:41
 * @since V1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface Param {
    /**
     * 对应到 HTTP 参数里的参数名称
     */
    String value();
    
    /**
     * 默认值
     */
    String defaultValue() default "";
}
