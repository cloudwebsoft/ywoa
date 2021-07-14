package com.redmoon.forum.plugin.refer;

import com.redmoon.forum.plugin.base.IPluginUnit;
import com.redmoon.forum.plugin.base.IPluginUI;
import com.redmoon.forum.plugin.base.IPluginPrivilege;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.BoardDb;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

public class ReferUnit implements IPluginUnit {
    public static final String code = "refer";

    public ReferUnit() {
    }

    public IPluginUI getUI(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
        return new ReferUI(request);
    }

    public IPluginPrivilege getPrivilege() {
        return new ReferPrivilege();
    }

    public boolean isPluginMsg(long msgId) {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);
        String pluginCode = md.getRootMsgPluginCode();
        return pluginCode.equals(code);
    }

    public IPluginMsgAction getMsgAction() {
        return new ReferMsgAction();
    }

    public boolean isPluginBoard(String boardCode) {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(code, boardCode);
    }
}
