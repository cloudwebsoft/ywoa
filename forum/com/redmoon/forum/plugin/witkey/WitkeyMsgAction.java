package com.redmoon.forum.plugin.witkey;

import javax.servlet.*;
import javax.servlet.http.*;
import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;
import java.sql.Timestamp;

public class WitkeyMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public WitkeyMsgAction() {
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
        String sEndDate = fu.getFieldValue("endDate");
        if (sEndDate.equals("")) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addEndDateError"));
        }
        java.util.Date dEndDate =  DateUtil.parse(sEndDate, "yyyy-MM-dd");
        long endDate = dEndDate.getTime();

        WitkeyDb cd = new WitkeyDb();
        cd = cd.getWitkeyDb(md.getId());
        if (Long.parseLong(cd.getEndDate()) >= endDate) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "errEndDateError"));
        }

        cd.setEndDate(Long.toString(endDate));

        try {
            return cd.save();
        } catch (ResKeyException ex) {
            return false;
        }
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        String catalogCode = fu.getFieldValue("catalogCode");
        if (catalogCode.equals("") || catalogCode.length() > 50 || catalogCode.equals("not")) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addCatalogCodeError"));
        }

        String moneyCode = fu.getFieldValue("moneyCode");
        if (moneyCode == null) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addMoneyCodeError"));
        }

        if (moneyCode.equals("")) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addMoneyCodeError"));
        }

        String score = fu.getFieldValue("score");
        if (score.equals("") || !StrUtil.isNumeric(score)) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addScoreError"));
        }

        String city = fu.getFieldValue("city");
        if (city.equals("") || city.length()>20) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addCityError"));
        }

        String contact = fu.getFieldValue("contact");
        if (contact.equals("") || contact.length()>250) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addContactError"));
        }

        String sEndDate = fu.getFieldValue("endDate");
        if (sEndDate.equals("")) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addEndDateError"));
        }
        java.util.Date dEndDate =  DateUtil.parse(sEndDate, "yyyy-MM-dd");
        long endDate = dEndDate.getTime();

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        java.util.Date date = DateUtil.parse(ts.toString(), "yyyy-MM-dd");

        if (date.getTime() > endDate) {
            throw new ErrMsgException(WitkeySkin.LoadString(request,"errAddEndDateError"));
        }



        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        WitkeyDb cd = new WitkeyDb();
        cd.setMsgRootId(md.getId());
        cd.setCatalogCode(catalogCode);
        cd.setMoneyCode(moneyCode);
        cd.setScore(Integer.parseInt(score));
        cd.setCity(city);
        cd.setContact(contact);
        cd.setEndDate(Long.toString(endDate));
        cd.setUserName(userName);

        return cd.create();
    }

    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException {
        WitkeyDb wd = new WitkeyDb();

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        java.util.Date date = DateUtil.parse(ts.toString(), "yyyy-MM-dd");

        MsgMgr mm = new MsgMgr();
        MsgDb md = mm.getMsgDb(replyMsgId);
        long msgId = md.getRootid();

        wd = wd.getWitkeyDb(msgId);
        if (date.getTime() > Long.parseLong(wd.getEndDate())) {
            throw new ErrMsgException(WitkeySkin.LoadString(request,
                    "errWitkeyEndDate"));
        }


        String replyType = ParamUtil.get(request, "replyType");

        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        String witkeyViewType = ParamUtil.get(request, "witkeyViewType");
        WitkeyReplyDb wrd = new WitkeyReplyDb();
        wrd.setMsgId(replyMsgId);
        wrd.setUserName(userName);
        wrd.setViewType(Integer.parseInt(witkeyViewType));
        wrd.setReplyType(Integer.parseInt(replyType));
        if (wrd.create()) {
            WitkeyUserDb wud = new WitkeyUserDb();
            wud = wud.getWitkeyUserDb(msgId, userName);
            wd = wd.getWitkeyDb(md.getRootid());
            if (Integer.parseInt(replyType) == WitkeyReplyDb.REPLY_TYPE_CONTRIBUTION) {
                wud.setContributionCount(wud.getContributionCount() + 1);
                wd.setContributionCount(wd.getContributionCount() + 1);
            } else {
                wud.setCommunicationCount(wud.getCommunicationCount() + 1);
            }

            try {
                if (Integer.parseInt(replyType) == WitkeyReplyDb.REPLY_TYPE_CONTRIBUTION) {
                    wud.save();
                    wd.save();
                }else{
                    wud.save();
                }
                return true;
            } catch (ResKeyException ex) {
                logger.error("save:" + ex.getMessage());
                return false;
            }
        } else {
            return false;
        }

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
        WitkeyDb gdb = new WitkeyDb();
        gdb = gdb.getWitkeyDb(md.getId());
        if (gdb.isLoaded()) {
            return gdb.del();
        } else
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
        WitkeyDb gdb = new WitkeyDb();
        gdb = gdb.getWitkeyDb(delId);
        if (gdb.isLoaded()) {
            return gdb.del();
        } else
            return true;
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        WitkeyDb wd = new WitkeyDb();

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        java.util.Date date = DateUtil.parse(ts.toString(), "yyyy-MM-dd");

        wd = wd.getWitkeyDb(md.getRootid());
        if (date.getTime() > Long.parseLong(wd.getEndDate())) {
            throw new ErrMsgException(WitkeySkin.LoadString(request,
                    "errWitkeyEndDate"));
        }


        String replyType = fu.getFieldValue("replyType");

        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        String witkeyViewType = fu.getFieldValue("witkeyViewType");
        WitkeyReplyDb wrd = new WitkeyReplyDb();
        wrd.setMsgId(md.getId());
        wrd.setUserName(userName);
        wrd.setViewType(Integer.parseInt(witkeyViewType));
        wrd.setReplyType(Integer.parseInt(replyType));
        if(wrd.create()){
            WitkeyUserDb wud = new WitkeyUserDb();
            wud = wud.getWitkeyUserDb(md.getRootid(), userName);
            wd = wd.getWitkeyDb(md.getRootid());
            if(Integer.parseInt(replyType) == WitkeyReplyDb.REPLY_TYPE_CONTRIBUTION){
                wud.setContributionCount(wud.getContributionCount() + 1);
                wd.setContributionCount(wd.getContributionCount() + 1);
            }else{
                wud.setCommunicationCount(wud.getCommunicationCount() + 1);
            }
            try {
                if (Integer.parseInt(replyType) == WitkeyReplyDb.REPLY_TYPE_CONTRIBUTION) {
                    wud.save();
                    wd.save();
                } else {
                    wud.save();
                }
                return true;
            } catch (ResKeyException ex) {
                logger.error("save:" + ex.getMessage());
                return false;
            }
        }else{
            return false;
        }
    }
}
