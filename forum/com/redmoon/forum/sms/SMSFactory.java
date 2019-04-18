package com.redmoon.forum.sms;

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

    static {
        getMsgUtil();
    }

    public static IMsgUtil getMsgUtil() {
        if (!isUseSms)
            return null;
        if (imsg!=null) {
            return imsg;
        }
        Config cfg = new Config();
        imsg = cfg.getIsUsedIMsg();
        if (imsg == null)
            isUseSms = false;
        return imsg;
    }

    public static boolean isUseSMS() {
        return isUseSms;
    }
}
