package com.cloudwebsoft.framework.util;

import org.apache.log4j.Logger;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: Log4j工具</p>
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
public class LogUtil {
    String name;

    public LogUtil(String name) {
        this.name = name;
    }

    public LogUtil(Class clazz) {
        this.name = clazz.getName();
    }

    public static LogUtil getLog(String name) {
        return new LogUtil(name);
    }

    public static LogUtil getLog(Class clazz) {
        return new LogUtil(clazz);
    }

    public void info(Object obj) {
        Logger.getLogger(name).info(obj);
    }

    public void error(Object obj) {
        Logger.getLogger(name).error(obj);
    }

    public void trace(Exception e) {
        error(StrUtil.trace(e));
    }

    public void warn(Object obj) {
        Logger.getLogger(name).warn(obj);
    }

    public void debug(Object obj) {
        Logger.getLogger(name).debug(obj);
    }
}
