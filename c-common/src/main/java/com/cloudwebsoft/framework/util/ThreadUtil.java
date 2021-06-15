package com.cloudwebsoft.framework.util;

import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ThreadUtil {
    public ThreadUtil() {
    }

    public static String getStackTraceString() {
        // Thread.getCurrentThread().getStackTrace(); // jdk1.5以上支持

        // StackTraceElement stack[] = (new Throwable()).getStackTrace();
        return StrUtil.trace(new Throwable());
    }
}
