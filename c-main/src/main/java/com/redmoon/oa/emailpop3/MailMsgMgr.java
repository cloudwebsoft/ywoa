package com.redmoon.oa.emailpop3;

/**
 * <p>Title: 社区</p>
 * <p>Description: 社区</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 红月亮工作室</p>
 * @author bluewind
 * @version 1.0
 */
import java.util.Date;

import cn.js.fan.util.*;

import javax.servlet.http.*;
import javax.servlet.ServletContext;
import cn.js.fan.web.SkinUtil;

public class MailMsgMgr {
    // public: connection parameters
    boolean debug = true;
    MailMsgDb mailMsgDb = new MailMsgDb();

    public MailMsgMgr() {
    }

    public boolean add(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (pvg.isUserLogin(request)) {
            try {
                mailMsgDb.create(application, request, pvg.getUser(request));
            } catch (ErrMsgException e) {
                throw e;
            }
        } else
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        return true;
    }

    public boolean delAttachment(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        int attachId = ParamUtil.getInt(request, "attachId");
        MailMsgDb mmd = getMailMsgDb(request, id);
        return mmd.delAttachment(attachId);
    }

    public boolean modify(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (pvg.isUserLogin(request)) {
            try {
                mailMsgDb.modify(application, request, pvg.getUser(request));
            } catch (ErrMsgException e) {
                throw e;
            }
        } else
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        return true;
    }

    public boolean delMulti(HttpServletRequest request) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        String[] ids = request.getParameterValues("ids");
        if (ids == null)
            throw new ErrMsgException("缺少标识!");

        return mailMsgDb.delMsg(ids);
    }

    public boolean del(HttpServletRequest request, boolean isDustbin) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        int id = ParamUtil.getInt(request, "id");
        return del(id, isDustbin);
    }

    public boolean del(int id, boolean isDustbin) throws ErrMsgException {
        MailMsgDb mmd = getMailMsgDb(id);
        if (isDustbin) {
            mmd.setType(MailMsgDb.TYPE_DUSTBIN);
            return mmd.save();
        }
        else
            return mmd.del();
    }

    public MailMsgDb getMailMsgDb(HttpServletRequest request, int id) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

        mailMsgDb = mailMsgDb.getMailMsgDb(id);
        String emailAddress = mailMsgDb.getEmailAddr();

        EmailPop3Db epd = new EmailPop3Db();
        epd = epd.getEmailPop3Db(pvg.getUser(request), emailAddress);
        if (epd != null) {
            return mailMsgDb;
        } else
            throw new ErrMsgException(SkinUtil.LoadString(request,
                        "pvg_invalid"));
    }

    public MailMsgDb getMailMsgDb(int id) throws ErrMsgException {
        return mailMsgDb.getMailMsgDb(id);
    }

    public MailMsgDb getMailMsgDb() {
        return this.mailMsgDb;
    }


}
