package com.redmoon.forum.plugin.base;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.MsgDb;

public interface IPluginViewListThread  extends IPluginView {
    String getBoardRule();
    String getBoardNote();
    boolean IsPluginBoard();
    public String getListtopicSql(HttpServletRequest request, String boardcode, String op, String timelimit, int threadType);
    public String render(int position, MsgDb md);
}
