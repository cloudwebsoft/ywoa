package com.redmoon.forum.plugin.base;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.MsgDb;

public interface IPluginViewShowMsg  extends IPluginView {
    boolean IsPluginBoard();
    public String getShowtopicSql(HttpServletRequest request, MsgDb rootMsgDb, String userId);
}
