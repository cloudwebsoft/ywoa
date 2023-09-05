package com.redmoon.oa.sys;

import java.util.Date;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;

import cn.js.fan.util.DateUtil;

public class DebugUtil {
    // 系统调试等级debugLevel，-1不输出，0通过System.out及日志输出debug、info、warn及error，
    // 1通过System.out及日志输出info、warn、error,
    // 2通过日志输出info、warn及error，3通过日志输出warn及error

    public static void log(Class cls, String func, String msg) {
        log(cls.getName(), func, msg);
    }

    public static void log(String clsName, String func, String msg) {
        Config cfg = Config.getInstance();
        int debugLevel = cfg.getInt("debugLevel");
        switch (debugLevel) {
            case 2:
                LogUtil.getLog(clsName).info(func + ":" + msg);
                break;
            case 1:
            case 0:
                LogUtil.getLog(clsName).info(func + ":" + msg);
                // System.out.println(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " info: " + clsName + " " + func + ": " + msg);
                break;
            case -1:
            default:
                break;
        }
    }

    public static void i(String clsName, String func, String msg) {
        log(clsName, func, msg);
    }

    public static void i(Class cls, String func, String msg) {
        log(cls.getName(), func, msg);
    }

    public static void e(Class cls, String func, String msg) {
        error(cls.getName(), func, msg);
    }

    public static void e(String clsName, String func, String msg) {
        error(clsName, func, msg);
    }

    public static void d(Class cls, String func, String msg) {
        debug(cls.getName(), func, msg);
    }

    public static void d(String clsName, String func, String msg) {
        debug(clsName, func, msg);
    }

    public static void error(String clsName, String func, String msg) {
        Config cfg = Config.getInstance();
        int debugLevel = cfg.getInt("debugLevel");
        switch (debugLevel) {
            case 3:
            case 2:
                LogUtil.getLog(clsName).error(func + ":" + msg);
                break;
            case 1:
            case 0:
                LogUtil.getLog(clsName).error(func + ":" + msg);
                System.out.println(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " error: " + clsName + " " + func + ": " + msg);
                break;
            case -1:
            default:
                break;
        }
    }

    public static void warn(String clsName, String func, String msg) {
        Config cfg = Config.getInstance();
        int debugLevel = cfg.getInt("debugLevel");
        switch (debugLevel) {
            case 3:
            case 2:
                LogUtil.getLog(clsName).warn("func:" + msg);
                break;
            case 1:
            case 0:
                LogUtil.getLog(clsName).warn("func:" + msg);
                System.out.println(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " warn: " + clsName + " " + func + ": " + msg);
                break;
            case -1:
            default:
                break;
        }
    }

    public static void debug(String clsName, String func, String msg) {
        Config cfg = Config.getInstance();
        int debugLevel = cfg.getInt("debugLevel");
        switch (debugLevel) {
            case 3:
            case 2:
                break;
            case 1:
                break;
            case 0:
                LogUtil.getLog(clsName).debug("func:" + msg);
                System.out.println(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " debug: " + clsName + " " + func + ": " + msg);
                break;
            case -1:
            default:
                break;
        }
    }

    public static void w(String clsName, String func, String msg) {
        warn(clsName, func, msg);
    }

    public static void w(Class cls, String func, String msg) {
        warn(cls.getName(), func, msg);
    }
}
