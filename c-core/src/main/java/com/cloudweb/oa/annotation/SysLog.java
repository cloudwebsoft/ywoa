package com.cloudweb.oa.annotation;


import com.cloudweb.oa.enums.LogLevel;
import com.cloudweb.oa.enums.LogType;

import java.lang.annotation.*;

/**
 * @author fgf
 */
// @SysLog(type = LogType.login, desc = "用户${userName}登录", level=LogLevel.INFO)
@Documented
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SysLog {
    /**
     * 动作描述,可使用占位符获取参数:${userName}
     */
    String action() default "";

    String remark() default "";

    String args() default "";

    boolean debug() default false;

    /**
     * 日志等级enum
     */
    LogLevel level() default LogLevel.NORMAL;

    String user() default "";

    /**
     * 操作类型enum
     */
    LogType type() default LogType.AUTHORIZE;

}