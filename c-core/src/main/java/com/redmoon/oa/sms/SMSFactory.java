package com.redmoon.oa.sms;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SMSFactory {
    public static boolean isUseSms = true;
    static IMsgUtil imsg;
    public static int boundary = 0;

    static {
        getMsgUtil();
    }

    public static void init() {
        isUseSms = true;
        imsg = null;
        boundary = 0;
    }

    public static IMsgUtil getMsgUtil() {
        if (!isUseSms) {
            return null;
        }
        if (imsg!=null) {
            return imsg;
        }
        Config cfg = new Config();
        boundary = Integer.parseInt(cfg.getProperty("sms.boundary"));
        imsg = cfg.getIsUsedIMsg();
        if (imsg == null) {
            isUseSms = false;
        }

        return imsg;
    }

    public static boolean isUseSMS() {
        return isUseSms;
    }

    public static int getBoundary(){
        return boundary;
    }
}
