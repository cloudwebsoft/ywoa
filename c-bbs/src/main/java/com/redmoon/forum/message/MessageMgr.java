package com.redmoon.forum.message;

/**
 * <p>Title: 社区</p>
 * <p>Description: 社区</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 红月亮工作室</p>
 * @author bluewind
 * @version 1.0
 */
import cn.js.fan.util.*;
import javax.servlet.http.*;
import cn.js.fan.web.SkinUtil;

public class MessageMgr {
    // public: connection parameters
    boolean debug = true;
    Privilege privilege;
    MessageDb MsgDB = new MessageDb();

    public MessageMgr() {
        privilege = new Privilege();
    }

    public int getNewMsgCount(HttpServletRequest request) {
        String name;
        com.redmoon.forum.Privilege pvg = new com.redmoon.forum.Privilege();
        name = pvg.getUser(request);
        MessageDb md = new MessageDb();
        return md.getNewMsgCount(name);
    }

    public boolean AddGroupMsg(HttpServletRequest request) throws
            ErrMsgException {
        com.redmoon.forum.Privilege pvg = new com.redmoon.forum.Privilege();
        if (pvg.isMasterLogin(request)) {
            try {
                MsgDB.AddMsg(request);
            } catch (ErrMsgException e) {
                throw e;
            }
        } else
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        return true;
    }

    public boolean AddMsg(HttpServletRequest request) throws
            ErrMsgException {
        com.redmoon.forum.Privilege pvg = new com.redmoon.forum.Privilege();
        if (pvg.isUserLogin(request)) {
            try {
                String sender = ParamUtil.get(request, "sender");
                MsgDB.AddMsg(request, sender);
            } catch (ErrMsgException e) {
                throw e;
            }
        } else
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        return true;
    }

    public boolean delMsg(HttpServletRequest request) throws ErrMsgException {
        com.redmoon.forum.Privilege pvg = new com.redmoon.forum.Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        String[] ids = request.getParameterValues("ids");
        if (ids == null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_id")); // "缺少标识!");
        if (!privilege.canManage(request, ids))
            return false;

        boolean re = false;
        try {
            re = MsgDB.delMsg(ids);
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public MessageDb getMessageDb(int id) throws ErrMsgException {
        return MsgDB.getMessageDb(id);
    }

}
