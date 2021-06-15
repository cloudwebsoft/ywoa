package com.redmoon.forum.plugin.debate;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.plugin.base.IPluginPrivilege;
import com.redmoon.forum.Privilege;
import org.apache.log4j.Logger;
import com.redmoon.forum.MsgDb;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;

public class DebatePrivilege implements IPluginPrivilege {
    static Logger logger = Logger.getLogger(DebatePrivilege.class.getName());

    public DebatePrivilege() {
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode, FileUpload fu) throws ErrMsgException {
        // 检测本版是否含有此功能
        DebateUnit au = new DebateUnit();
        if (!au.isPluginBoard(boardCode))
            throw new ErrMsgException(DebateSkin.LoadString(request, "addNewErrorBoardInvalid"));
        return true;
    }

    public boolean isOwner(HttpServletRequest request, long msgRootId) {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgRootId);
        Privilege privilege = new Privilege();
        String user = privilege.getUser(request);
        if (user.equals(md.getName()))
            return true;
        else
            return false;
    }

    public boolean canAddReply(HttpServletRequest request, String boardCode, long msgRootId) throws ErrMsgException {
        // 检测本版是否含有此功能
        DebateUnit au = new DebateUnit();
        if (!au.isPluginBoard(boardCode))
            throw new ErrMsgException(DebateSkin.LoadString(request, "addNewErrorBoardInvalid"));
        // 检查辩论是否已过期
        DebateDb dd = new DebateDb();
        dd = dd.getDebateDb(msgRootId);
        // LogUtil.getLog(getClass()).info("msgRootId=" + msgRootId + " isLoaded=" + dd.isLoaded() + " beginDate=" + DateUtil.format(dd.getBeginDate(), "yyyy-MM-dd"));
        if (DateUtil.compare(dd.getBeginDate(), new java.util.Date())==1) {
            throw new ErrMsgException("辩论尚未开始，您不能回复！");
        }
        if (DateUtil.compare(new java.util.Date(), dd.getEndDate())==1) {
            throw new ErrMsgException("辩论已结束，您不能回复！");
        }
        return true;
    }

    public boolean canEdit(HttpServletRequest request, MsgDb md) throws ErrMsgException {
        // 检测本版是否含有此功能
        DebateUnit au = new DebateUnit();
        if (!au.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(DebateSkin.LoadString(request, "addNewErrorBoardInvalid"));

        return true;
    }

    /**
     *
     * @param request HttpServletRequest
     * @param md MsgDb replyid所对应的贴子，也即根贴
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean canAddQuickReply(HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        // 检查辩论是否已过期
        DebateDb dd = new DebateDb();
        dd = dd.getDebateDb(md.getId());
        // LogUtil.getLog(getClass()).info("msgRootId=" + md.getId() + " isLoaded=" + dd.isLoaded() + " beginDate=" + DateUtil.format(dd.getBeginDate(), "yyyy-MM-dd"));
        if (DateUtil.compare(dd.getBeginDate(), new java.util.Date()) == 1) {
            throw new ErrMsgException("辩论尚未开始，您不能回复！");
        }
        if (DateUtil.compare(new java.util.Date(), dd.getEndDate()) == 1) {
            throw new ErrMsgException("辩论已结束，您不能回复！");
        }
        return true;
    }

    public boolean canManage(HttpServletRequest request, long msgId) throws ErrMsgException {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);

        // 检测本版是否含有此功能
        DebateUnit au = new DebateUnit();
        if (!au.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(DebateSkin.LoadString(request, "addNewErrorBoardInvalid"));

        Privilege privilege = new Privilege();
        // String user = privilege.getUser(request);

        boolean re = false;
        /*
        // 如果是楼主可以管理
        long rootId = md.getRootid();
        md = md.getMsgDb(rootId);
        if (user.equals(md.getName()))
            re = true;
        */

        // 如果是版主也可以管理
        if (privilege.canManage(request, msgId)) {
            re = true;
        }

        return re;
    }
}
