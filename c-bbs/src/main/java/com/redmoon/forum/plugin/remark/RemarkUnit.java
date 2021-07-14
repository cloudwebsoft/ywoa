package com.redmoon.forum.plugin.remark;

import com.redmoon.forum.plugin.base.IPluginUnit;
import com.redmoon.forum.plugin.base.IPluginUI;
import com.redmoon.forum.plugin.base.IPluginPrivilege;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.BoardDb;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import com.redmoon.forum.plugin.*;

public class RemarkUnit implements IPluginUnit {
    public static final String code = "remark";

    public RemarkUnit() {
    }

    public IPluginUI getUI(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
        return new RemarkUI(request);
    }

    public IPluginPrivilege getPrivilege() {
        return new RemarkPrivilege();
    }

    public boolean isPluginMsg(long msgId) {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);
        String pluginCode = md.getRootMsgPluginCode();
        return pluginCode.equals(code);
    }

    public IPluginMsgAction getMsgAction() {
        return new RemarkMsgAction();
    }

    public boolean isPluginBoard(String boardCode) {
        PluginMgr pm = new PluginMgr();
        PluginUnit pu = pm.getPluginUnit(RemarkUnit.code);
        if (pu.getType().equals(PluginUnit.TYPE_FORUM))
            return true;

        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(code, boardCode);
    }
}
