package com.redmoon.forum.plugin.remark;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.sql.SQLException;
import com.cloudwebsoft.framework.util.LogUtil;

public class RemarkMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public RemarkMsgAction() {
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
        RemarkDb rd = new RemarkDb();
        rd = (RemarkDb)rd.getQObjectDb(new Long(md.getId()));
        boolean re = false;
        if (rd!=null) {
            try {
                re = rd.del();
            }
            catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
        }
        return re;
    }

    /**
     * 此函数置于MsgDb delSingleMsg中真正删除贴子之前，以便于递归删除贴子
     * @param delId int
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delSingleMsg(long delId) throws
            ResKeyException {
        return true;
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }

    public boolean remark(HttpServletRequest request) throws ErrMsgException,ResKeyException {
        long msgId = ParamUtil.getLong(request, "msgId");
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);
        Privilege pvg = new Privilege();
        if (!pvg.isManager(request, md.getboardcode()))
            throw new ErrMsgException("权限非法!");
        RemarkDb rd = new RemarkDb();
        rd = (RemarkDb)rd.getQObjectDb(new Long(msgId));
        String content = ParamUtil.get(request, "content");
        String sign = ParamUtil.get(request, "sign");
        String manager = "";
        if (pvg.isUserLogin(request))
            manager = pvg.getUser(request);
        boolean re = false;
        if (rd==null) {
            rd = new RemarkDb();
            re = rd.create(new JdbcTemplate(), new Object[] {
                new Long(msgId), content, sign, new java.util.Date(), manager
            });
        }
        else {
            try {
                re = rd.save(new JdbcTemplate(), new Object[] {
                    content, sign, new java.util.Date(), manager, new Long(msgId)
                });
            }
            catch (SQLException e) {
                LogUtil.getLog(getClass()).error("remark:" + e.getMessage());
            }
        }
        return re;
    }

}
