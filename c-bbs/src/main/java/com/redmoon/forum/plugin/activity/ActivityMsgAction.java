package com.redmoon.forum.plugin.activity;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import org.apache.log4j.Logger;
import com.redmoon.forum.MsgDb;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ResKeyException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.forum.MsgMgr;
import cn.js.fan.util.DateUtil;

public class ActivityMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public ActivityMsgAction() {
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
        String organizer = fu.getFieldValue("organizer");
        String tel = fu.getFieldValue("tel");
        String moneyCode = fu.getFieldValue("moneyCode");
        String strExpireDate = fu.getFieldValue("activityExpireDate");
        String strAttendMoneyCount = fu.getFieldValue("attendMoneyCount");
        String strExitMoneyCount = fu.getFieldValue("exitMoneyCount");
        int attendMoneyCount = 0;
        int exitMoneyCount = 0;
        if (StrUtil.isNumeric(strAttendMoneyCount))
            attendMoneyCount = Integer.parseInt(strAttendMoneyCount);
        if (StrUtil.isNumeric(strExitMoneyCount))
            exitMoneyCount = Integer.parseInt(strExitMoneyCount);
        java.util.Date expireDate = DateUtil.parse(strExpireDate, "yyyy-MM-dd");
        String strUserLevel = fu.getFieldValue("userLevel");
        int userLevel = 0;
        if (StrUtil.isNumeric(strUserLevel))
            userLevel = Integer.parseInt(strUserLevel);

        int userCount = -1;
        String strUserCount = fu.getFieldValue("userCount");
        try {
            userCount = Integer.parseInt(strUserCount);
        }
        catch (Exception e) {
        }

        ActivityDb atd = new ActivityDb();
        atd = atd.getActivityDb(md.getId());
        atd.setOrganizer(organizer);
        atd.setTel(tel);
        atd.setExpireDate(expireDate);
        atd.setMoneyCode(moneyCode);
        atd.setAttendMoneyCount(attendMoneyCount);
        atd.setExitMoneyCount(exitMoneyCount);
        atd.setUserLevel(userLevel);
        atd.setUserCount(userCount);
        boolean re = false;
        try {
            re = atd.save();
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        String organizer = fu.getFieldValue("organizer");
        String tel = fu.getFieldValue("tel");
        String moneyCode = fu.getFieldValue("moneyCode");
        String strExpireDate = fu.getFieldValue("activityExpireDate");
        String strAttendMoneyCount = fu.getFieldValue("attendMoneyCount");
        String strExitMoneyCount = fu.getFieldValue("exitMoneyCount");
        int attendMoneyCount = 0;
        int exitMoneyCount = 0;
        if (StrUtil.isNumeric(strAttendMoneyCount))
            attendMoneyCount = Integer.parseInt(strAttendMoneyCount);
        if (StrUtil.isNumeric(strExitMoneyCount))
            exitMoneyCount = Integer.parseInt(strExitMoneyCount);
        java.util.Date expireDate = DateUtil.parse(strExpireDate, "yyyy-MM-dd");
        String strUserLevel = fu.getFieldValue("userLevel");
        int userLevel = 0;
        if (StrUtil.isNumeric(strUserLevel))
            userLevel = Integer.parseInt(strUserLevel);

        int userCount = -1;
        String strUserCount = fu.getFieldValue("userCount");
        try {
            userCount = Integer.parseInt(strUserCount);
        }
        catch (Exception e) {
        }

        ActivityDb atd = new ActivityDb();
        atd.setOrganizer(organizer);
        atd.setTel(tel);
        atd.setExpireDate(expireDate);
        atd.setMoneyCode(moneyCode);
        atd.setAttendMoneyCount(attendMoneyCount);
        atd.setExitMoneyCount(exitMoneyCount);
        atd.setUserLevel(userLevel);
        atd.setMsgId(md.getId());
        atd.setUserCount(userCount);
        boolean re = false;
        try {
            re = atd.create();
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
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
        ActivityDb rd = new ActivityDb();
        rd = rd.getActivityDb(delId);
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

}
