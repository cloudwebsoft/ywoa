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
public class QueryInfo {

    public QueryInfo() {
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

    public String getStackTraceString() {
        return stackTraceString;
    }

    public long getTime() {
        return time;
    }

    public String getThreadName() {
        return threadName;
    }

    private String stackTraceString;
    private long time;
    private String threadName;
}
