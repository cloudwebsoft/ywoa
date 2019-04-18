package com.redmoon.forum.plugin.sweet;

import com.redmoon.forum.plugin.base.IPluginUnit;
import com.redmoon.forum.plugin.base.IPluginUI;
import com.redmoon.forum.plugin.base.IPluginPrivilege;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import com.redmoon.forum.plugin.BoardDb;

public class SweetUnit implements IPluginUnit {
    public static final String code = "sweet";

    public SweetUnit() {
    }

    public IPluginUI getUI(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
        return new SweetUI(request);
    }

    public IPluginPrivilege getPrivilege() {
        return new SweetPrivilege();
    }

    public boolean isPluginMsg(long msgId) {
        return false;
    }

    public IPluginMsgAction getMsgAction() {
        return new SweetMsgAction();
    }

    public boolean isPluginBoard(String boardCode) {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(code, boardCode);
    }
}
