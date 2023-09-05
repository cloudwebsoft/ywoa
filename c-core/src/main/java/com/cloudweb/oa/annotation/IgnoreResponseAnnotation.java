package com.cloudweb.oa.annotation;

import java.lang.annotation.*;

/**
 * @author
 * @version 1.0
 * @Description
 * @date
 * @updateby 自定义注解类，被注解后不封装返回参数
 * @updatedate
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreResponseAnnotation {
}
