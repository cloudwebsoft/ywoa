package com.cloudwebsoft.framework.console;

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
public class ConsoleConfig {
    private static boolean debug = false;

    /**
     * 超时阀值1000毫秒
     */
    public static long connElapseTimeMax = 1000;

    public ConsoleConfig() {
    }

    public static boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        ConsoleConfig.debug = debug;
    }
}
