package com.redmoon.forum.plugin.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

public interface IPluginUnit {
        /**
         * 取得插件的UI接口
         * @param request HttpServletRequest
         * @return IPluginUI
         */
        IPluginUI getUI(HttpServletRequest request, HttpServletResponse response, JspWriter out);

        /**
         * 取得插件的权限接口
         * @return IPluginPrivilege
         */
        IPluginPrivilege getPrivilege();

        /**
         * 取得插件的贴子操作接口
         * @return IPluginMsgAction
         */
        IPluginMsgAction getMsgAction();

        /**
         * 插件是否插于版块上
         * @param boardCode String
         * @return boolean
         */
        boolean isPluginBoard(String boardCode);

        /**
         * 插件是否插于贴子上
         * @param msgId long
         * @return boolean
         */
        boolean isPluginMsg(long msgId);
}
