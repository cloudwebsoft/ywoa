package com.redmoon.forum.plugin.witkey;

import com.redmoon.forum.plugin.base.IPluginUI;
import com.redmoon.forum.plugin.base.IPluginViewListThread;
import com.redmoon.forum.plugin.base.IPluginViewAddMsg;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.plugin.base.IPluginViewAddReply;
import com.redmoon.forum.plugin.base.IPluginViewShowMsg;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.base.IPluginViewEditMsg;
import cn.js.fan.base.ISkin;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletResponse;
import com.redmoon.forum.plugin.base.IPluginViewCommon;

public class WitkeyUI implements IPluginUI {
    HttpServletRequest request;
    HttpServletResponse response;
    JspWriter out;

    public WitkeyUI(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
        this.request = request;
        this.response = response;
        this.out = out;
    }

    public IPluginViewCommon getViewCommon() {
        return null;
    }

    public IPluginViewListThread getViewListThread(String boardCode) {
        return new WitkeyViewListThread(request, response, out, boardCode);
    }

    public IPluginViewAddMsg getViewAddMsg(String boardCode) {
        return new WitkeyViewAddMsg(request, boardCode);
    }

    public IPluginViewAddReply getViewAddReply(String boardCode, long msgRootId) {
        return new WitkeyViewAddReply(request, boardCode, msgRootId);
    }

    public IPluginViewEditMsg getViewEditMsg(String boardCode, long msgId) {
        return new WitkeyViewEditMsg(request, boardCode, msgId);
    }

    /**
     *
     * @param boardCode String
     * @param msgDb MsgDb 正被显示的贴子
     * @return IPluginViewShowMsg
     */
    public IPluginViewShowMsg getViewShowMsg(String boardCode, MsgDb msgDb) {
        return new WitkeyViewShowMsg(request, boardCode, msgDb);
    }

    public ISkin getSkin() {
        return new WitkeySkin();
    }
}
