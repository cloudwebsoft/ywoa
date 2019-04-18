package com.redmoon.forum.plugin.group;

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

public class GroupUI implements IPluginUI {
    HttpServletRequest request;

    public GroupUI(HttpServletRequest request) {
        this.request = request;
    }

    public IPluginViewCommon getViewCommon() {
        return new GroupViewCommon(request);
    }

    public IPluginViewListThread getViewListThread(String boardCode) {
        return new GroupViewListThread(request, boardCode);
    }

    public IPluginViewAddMsg getViewAddMsg(String boardCode) {
        return new GroupViewAddMsg(request, boardCode);
    }

    public IPluginViewAddReply getViewAddReply(String boardCode, long msgRootId) {
        return new GroupViewAddReply(request, boardCode, msgRootId);
    }

    public IPluginViewEditMsg getViewEditMsg(String boardCode, long msgId) {
        return new GroupViewEditMsg(request, boardCode, msgId);
    }

    /**
     *
     * @param boardCode String
     * @param msgDb MsgDb 正被显示的贴子
     * @return IPluginViewShowMsg
     */
    public IPluginViewShowMsg getViewShowMsg(String boardCode, MsgDb msgDb) {
        return new GroupViewShowMsg(request, boardCode, msgDb);
    }

    public ISkin getSkin() {
        return new GroupSkin();
    }
}
