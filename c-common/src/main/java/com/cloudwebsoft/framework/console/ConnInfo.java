package com.cloudwebsoft.framework.console;

import com.cloudwebsoft.framework.util.*;

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
public class ConnInfo {

    public ConnInfo() {
        this.stackTraceString = ThreadUtil.getStackTraceString();
        this.time = System.currentTimeMillis();
        threadName = Thread.currentThread().getName();
    }

    public void setStackTraceString(String stackTraceString) {
        this.stackTraceString = stackTraceString;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setPreparedStatement(boolean preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public void setQueryTimeElapse(long queryTimeElapse) {
        this.queryTimeElapse = queryTimeElapse;
    }

    public String getStackTraceString() {
        return stackTraceString;
    }

    public long getTime() {
        return time;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getSql() {
        return sql;
    }

    public boolean isPreparedStatement() {
        return preparedStatement;
    }

    public long getQueryTimeElapse() {
        return queryTimeElapse;
    }

    private String stackTraceString;
    /**
     * 创建的时间
     */
    private long time;
    private String threadName;
    private String sql;
    private boolean preparedStatement = false;
    private long queryTimeElapse = 0;
}
