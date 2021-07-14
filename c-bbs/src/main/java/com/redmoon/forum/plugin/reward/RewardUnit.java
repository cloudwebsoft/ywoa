package com.redmoon.forum.plugin.reward;

import com.redmoon.forum.plugin.base.IPluginUnit;
import com.redmoon.forum.plugin.base.IPluginUI;
import com.redmoon.forum.plugin.base.IPluginPrivilege;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.MsgDb;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import com.redmoon.forum.plugin.BoardDb;

public class RewardUnit implements IPluginUnit {
    public static final String code = "reward";

    public RewardUnit() {
    }

    public IPluginUI getUI(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
        return new RewardUI(request);
    }

    public IPluginPrivilege getPrivilege() {
        return new RewardPrivilege();
    }

    public boolean isPluginMsg(long msgId) {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);
        String pluginCode = md.getRootMsgPluginCode();
        return pluginCode.equals(code);
    }

    public IPluginMsgAction getMsgAction() {
        return new RewardMsgAction();
    }

    public boolean isPluginBoard(String boardCode) {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(code, boardCode);
    }
}
