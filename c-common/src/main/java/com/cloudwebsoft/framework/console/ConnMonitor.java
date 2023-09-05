package com.cloudwebsoft.framework.console;

import java.util.*;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.db.IConnection;

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
public class ConnMonitor {
    /**
     * 时间超长的查询
     */
    private static Map tooLongQueries = Collections.synchronizedMap(new HashMap());

    /**
     * 超过0.2秒的执行时间将会被记录
     */
    public static long MAX_ELAPSE_TIME = 500; // 0.5秒

    /**
     * 活动的连接
     */
    private static Map activeConnections = Collections.synchronizedMap(new
            HashMap());

    public ConnMonitor() {

    }

    public static void onGetConnection(IConnection connObj) {
        activeConnections.put(connObj.getId(), new ConnInfo());
        // LogUtil.getLog(ConnMonitor.class).info("Get conn " + connObj);
    }

    public static void onCloseConnection(IConnection connObj) {
        if (!ConsoleConfig.isDebug())
            return;
        activeConnections.remove(connObj.getId());
        // LogUtil.getLog(ConnMonitor.class).info("Close conn " + connObj);
    }

    public static Map getActiveConnections() {
        return activeConnections;
    }

    public static void clearActiveConnections() {
        activeConnections.clear();
    }

    public static Map getTooLongQueries() {
        return tooLongQueries;
    }

    public static void removeFromActiveConnections(String connObjId) {
        activeConnections.remove(connObjId);
    }

    public static ConnInfo getConnInfo(IConnection connObj) {
        return (ConnInfo) activeConnections.get(connObj.getId());
    }

    /**
     * 记录preparedStatement用到的sql语句
     * @param connObj Object
     * @param sql String
     */
    public static void onPrepareStatement(IConnection connObj, String sql) {
        if (!ConsoleConfig.isDebug())
            return;
        ConnInfo ci = (ConnInfo) activeConnections.get(connObj.getId());
        if (ci!=null) {
            ci.setSql(sql);
            ci.setPreparedStatement(true);
            activeConnections.put(connObj.getId(), ci);
        }
        else {
            new Throwable().printStackTrace();
        }
    }

    /**
     * 当执行预编译的SQL时
     * @param connObj Object
     * @param timeElapse long
     */
    public static void onExecutePreQuery(IConnection connObj, long timeElapse) {
        if (!ConsoleConfig.isDebug())
            return;
        if (timeElapse >= MAX_ELAPSE_TIME) {
            ConnInfo ci = (ConnInfo) activeConnections.get(connObj.getId());
            if (ci!=null) {
                ci.setQueryTimeElapse(timeElapse);
                tooLongQueries.put(connObj.getId(), ci);
            }
            else {
                new Throwable().printStackTrace();
            }
        }
    }

    /**
     * 当执行SQL时
     * @param connObj Object
     * @param sql String
     * @param timeElapse long
     */
    public static void onExecuteQuery(IConnection connObj, String sql, long timeElapse) {
        if (!ConsoleConfig.isDebug())
            return;
        if (timeElapse >= MAX_ELAPSE_TIME) {
            ConnInfo ci = (ConnInfo) activeConnections.get(connObj.getId());
            if (ci!=null) {
                ci.setSql(sql);
                ci.setQueryTimeElapse(timeElapse);
                tooLongQueries.put(connObj.getId(), ci);
            }
            else {
                new Throwable().printStackTrace();
            }
        }
    }

    /**
     * 从超长执行的SQL记录中删除相关项
     * @param connObjHashCode String
     */
    public static void removeFromTooLongQueries(String connObjId) {
        tooLongQueries.remove(connObjId);
    }

    /**
     * 清除超长执行的SQL记录
     */
    public static void clearTooLongQueries() {
        tooLongQueries.clear();
    }

    /**
     * 替换占位符
     * @task:暂因JdbcTemplate中的fillPreparedStatement为static型，而无法调用
     * @param connObj Object
     * @param i int
     * @param objParam Object
     */
    public static void onFillPreparedStatement(IConnection connObj, int i, Object objParam) {
        if (!ConsoleConfig.isDebug())
            return;

        ConnInfo ci = (ConnInfo) activeConnections.get(connObj.getId());
        if (ci!=null) {
            String sql = ci.getSql();
            if (objParam instanceof java.util.Date) {
                sql = sql.replaceFirst("\\?", DateUtil.format((java.util.Date)objParam, "yyyy-MM-dd HH:mm:ss"));
            } else {
                sql = sql.replaceFirst("\\?", objParam.toString());
            }
        }
        else {
            new Throwable().printStackTrace();
        }
    }
}
