package com.redmoon.forum.plugin.present;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;

public class PresentMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public PresentMsgAction() {
    }

    /**
     *
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb 所存储的是ReceiveData后得来的信息
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean editTopic(ServletContext application,
                                          HttpServletRequest request,
                                          MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }

    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException {
        return true;
    }

    /**
     * 本方法置于MsgMgr中delTopic真正删除贴子之前，使在删除插件相应内容后，再删除贴子本身
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delTopic(ServletContext application,
                            HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        return true;
    }

    /**
     * 此函数置于MsgDb delSingleMsg中真正删除贴子之前，以便于递归删除贴子
     * @param delId int
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delSingleMsg(long delId) throws
            ResKeyException {
        PresentDb rd = new PresentDb();
        rd = rd.getPresentDb(delId);
        if (rd.isLoaded())
            return rd.del();
        else
            return true;
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }

    public boolean give(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        long msgId = ParamUtil.getLong(request, "msgId");
        MsgMgr mm = new MsgMgr();
        MsgDb md = mm.getMsgDb(msgId);
        if (!md.isLoaded()) {
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.present","err_isNotExsist"));//该贴已不存在！
        }

        if (md.getName().equals(privilege.getUser(request))) {
            throw new ErrMsgException("您不能给自己送分！");
        }

        int score = ParamUtil.getInt(request, "score");
        if (score<=0)
            throw new ErrMsgException("分值需大于0!");

        String moneyCode = ParamUtil.get(request, "moneyCode");
        if (moneyCode.equals(""))
            throw new ErrMsgException("请选择币种!");

        String reason = ParamUtil.get(request, "reason");
        if (reason.length()>255)
            throw new ErrMsgException("原因的长度不能大于255");

        boolean re;
        try {
            PresentDb pd = new PresentDb();
            re = pd.doGive(md, privilege.getUser(request), moneyCode, score, reason);
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }
}
