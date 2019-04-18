package com.redmoon.oa.project.forum;

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

public class ProjectUI implements IPluginUI {
    HttpServletRequest request;

    public ProjectUI(HttpServletRequest request) {
        this.request = request;
    }

    public IPluginViewCommon getViewCommon() {
        return null;
    }

    public IPluginViewListThread getViewListThread(String boardCode) {
        return new ProjectViewListThread(request, boardCode);
    }

    public IPluginViewAddMsg getViewAddMsg(String boardCode) {
        return new ProjectViewAddMsg(request, boardCode);
    }

    public IPluginViewAddReply getViewAddReply(String boardCode, long msgRootId) {
        return new ProjectViewAddReply(request, boardCode, msgRootId);
    }

    public IPluginViewEditMsg getViewEditMsg(String boardCode, long msgId) {
        return new ProjectViewEditMsg(request, boardCode, msgId);
    }

    /**
     *
     * @param boardCode String
     * @param msgDb MsgDb 正被显示的贴子
     * @return IPluginViewShowMsg
     */
    public IPluginViewShowMsg getViewShowMsg(String boardCode, MsgDb msgDb) {
        return new ProjectViewShowMsg(request, boardCode, msgDb);
    }

    public ISkin getSkin() {
        return new ProjectSkin();
    }
}
