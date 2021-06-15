package com.redmoon.forum.plugin.activity;

import com.redmoon.forum.plugin.base.IPluginUI;
import com.redmoon.forum.plugin.base.IPluginViewListThread;
import com.redmoon.forum.plugin.base.IPluginViewAddMsg;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.plugin.base.IPluginViewAddReply;
import com.redmoon.forum.plugin.base.IPluginViewShowMsg;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.base.IPluginViewEditMsg;
import cn.js.fan.base.ISkin;
import com.redmoon.forum.plugin.base.IPluginViewCommon;

public class ActivityUI implements IPluginUI {
    HttpServletRequest request;

    public ActivityUI(HttpServletRequest request) {
        this.request = request;
    }

    public IPluginViewCommon getViewCommon() {
        return null;
    }

    public IPluginViewListThread getViewListThread(String boardCode) {
        return new ActivityViewListThread(request, boardCode);
    }

    public IPluginViewAddMsg getViewAddMsg(String boardCode) {
        return new ActivityViewAddMsg(request, boardCode);
    }

    public IPluginViewAddReply getViewAddReply(String boardCode, long msgRootId) {
        return new ActivityViewAddReply(request, boardCode, msgRootId);
    }

    public IPluginViewEditMsg getViewEditMsg(String boardCode, long msgId) {
        return new ActivityViewEditMsg(request, boardCode, msgId);
    }

    /**
     *
     * @param boardCode String
     * @param msgDb MsgDb 正被显示的贴子
     * @return IPluginViewShowMsg
     */
    public IPluginViewShowMsg getViewShowMsg(String boardCode, MsgDb msgDb) {
        return new ActivityViewShowMsg(request, boardCode, msgDb);
    }

    public ISkin getSkin() {
        return new ActivitySkin();
    }
}
