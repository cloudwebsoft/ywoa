package com.redmoon.forum.plugin.base;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.forum.MsgDb;
import com.redmoon.kit.util.FileUpload;
import cn.js.fan.util.ResKeyException;

public interface IPluginMsgAction {
    boolean AddNew(ServletContext application,
                   HttpServletRequest request, MsgDb md, FileUpload fu) throws ErrMsgException;
    
    /**
     * 回复
     * @param application
     * @param request
     * @param md 回贴
     * @param fu
     * @return
     * @throws ErrMsgException
     */
    boolean AddReply(ServletContext application,
                     HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException;

    boolean editTopic(ServletContext application,
                      HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException;

    boolean delTopic(ServletContext application,
                      HttpServletRequest request, MsgDb md) throws
            ErrMsgException;

    /**
     * 快速回复
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param replyMsgId long 回贴的ID
     * @return boolean
     * @throws ErrMsgException
     */
    boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException;

    public boolean delSingleMsg(long delId) throws
            ResKeyException;
}
