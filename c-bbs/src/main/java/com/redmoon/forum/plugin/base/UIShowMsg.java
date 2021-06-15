package com.redmoon.forum.plugin.base;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: 对应于贴子显示界面showtopic.jsp的描述</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class UIShowMsg {
    public static final int POS_NOTE = 0;
    public static final int POS_BEFORE_MSG = 1;  // 贴子前
    public static final int POS_AFTER_MSG = 2;   // 贴子后
    public static final int POS_BEFORE_USER = 3; // 用户信息显示之前，暂无用处
    public static final int POS_AFTER_USER = 4;  // 用户信息显示之后
    public static final int POS_QUICK_REPLY_NOTE = 5;
    public static final int POS_QUICK_REPLY_ELEMENT = 6;

    public static final int POS_AFTER_NOTE = 7;
    public static final int POS_TOPIC_OPERATE_MENU = 8;

    public UIShowMsg() {
    }
}
