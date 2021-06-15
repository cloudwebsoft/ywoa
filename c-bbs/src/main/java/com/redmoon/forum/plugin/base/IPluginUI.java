package com.redmoon.forum.plugin.base;

import com.redmoon.forum.MsgDb;
import cn.js.fan.base.ISkin;

public interface IPluginUI {
    IPluginViewAddMsg getViewAddMsg(String boardCode);
    IPluginViewEditMsg getViewEditMsg(String boardCode, long msgId);
    IPluginViewListThread getViewListThread(String boardCode);
    IPluginViewAddReply getViewAddReply(String boardCode, long msgRootId);
    IPluginViewShowMsg getViewShowMsg(String boardCode, MsgDb msgDb);
    IPluginViewCommon getViewCommon();
    ISkin getSkin();
}
