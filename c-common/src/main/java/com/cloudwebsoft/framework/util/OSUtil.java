package com.cloudwebsoft.framework.util;

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
public class OSUtil {
    public OSUtil() {
    }

    /**
     * 判断是否为windwos操作系统
     * @return boolean
     */
    public static boolean isWindows() {
        boolean flag = false;
        if (System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            flag = true;
        }
        return flag;
    }
}
