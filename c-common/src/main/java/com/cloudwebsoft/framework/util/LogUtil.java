package com.cloudwebsoft.framework.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import cn.js.fan.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

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

    // The wrapper class' fully qualified class name.
    // 封装类的全类名，注意通过这个才能由LocationAwareLogger获取到调用LogUtil的类的行号
    private final String FQCN = LogUtil.class.getName();

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

    public void info(Exception ex) {
        LoggerFactory.getLogger(name).info("info:", ex);
    }

    public void warn(Exception ex) {
        LoggerFactory.getLogger(name).warn("warn:", ex);
    }

    public void error(Exception ex) {
        LoggerFactory.getLogger(name).error("error:", ex);
    }

    public void error(Throwable t) {
        LoggerFactory.getLogger(name).error("error:", t);
    }

    public void info(String msg) {
        ((LocationAwareLogger)LoggerFactory.getLogger(name)).log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, null);
    }

    // info("姓名{}，年龄{}", name, age)
    public void info(String msg, Object... objects) {
        ((LocationAwareLogger)LoggerFactory.getLogger(name)).log(null, FQCN, LocationAwareLogger.INFO_INT, msg, objects, null);
    }

    public void error(String msg) {
        ((LocationAwareLogger)LoggerFactory.getLogger(name)).log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, null);
    }

    public void trace(Exception e) {
        LoggerFactory.getLogger(name).error(StrUtil.trace(e));
    }

    public void warn(String msg) {
        ((LocationAwareLogger)LoggerFactory.getLogger(name)).log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, null);
    }

    public void warn(String msg, Object... objects) {
        ((LocationAwareLogger)LoggerFactory.getLogger(name)).log(null, FQCN, LocationAwareLogger.WARN_INT, msg, objects, null);
    }

    public void debug(String msg) {
        ((LocationAwareLogger)LoggerFactory.getLogger(name)).log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
    }

    public void debug(String msg, Object... objects) {
        ((LocationAwareLogger)LoggerFactory.getLogger(name)).log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, objects, null);
    }

    public boolean isDebugEnable() {
        return LoggerFactory.getLogger(name).isDebugEnabled();
    }
}
