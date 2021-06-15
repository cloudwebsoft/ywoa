package com.redmoon.oa.sale;
import cn.js.fan.util.*;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class ContactMgr {
    Logger logger = Logger.getLogger(ContactMgr.class.getName());
    public ContactMgr() {
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //throw new ErrMsgException("请先登录！");
        ContactDb cdb = new ContactDb();
        boolean re = true;
        String contactType="",contactDate="",contactContent="",satisfy="",feedback="",memo="";
        double moneyCost, timeCost;
        contactType = ParamUtil.get(request, "contactType");
        contactDate = ParamUtil.get(request, "contactDate");
        int customerNameId = ParamUtil.getInt(request, "customerNameId");
        int linkId = ParamUtil.getInt(request, "linkId");
        contactContent = ParamUtil.get(request, "contactContent");
        moneyCost = ParamUtil.getDouble(request, "moneyCost");
        timeCost = ParamUtil.getDouble(request, "timeCost");
        satisfy = ParamUtil.get(request, "satisfy");
        feedback = ParamUtil.get(request, "feedback");
        memo = ParamUtil.get(request, "memo");
        cdb.setContactType(contactType);
        java.util.Date d = null;
        try {
            d = DateUtil.parse(contactDate, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("create:" + e.getMessage());
        }
        cdb.setContactDate(d);
        cdb.setCustomerNameId(customerNameId);
        cdb.setLinkId(linkId);
        cdb.setContactContent(contactContent);
        cdb.setMoneyCost(moneyCost);
        cdb.setTimeCost(timeCost);
        cdb.setSatisfy(satisfy);
        cdb.setFeedback(feedback);
        cdb.setMemo(memo);
        re = cdb.create();
        return re;
    }
    public ContactDb getContactDb(int id) {
     ContactDb atd = new ContactDb();
     return atd.getContactDb(id);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        ContactDb atd = getContactDb(id);
        if (atd == null || !atd.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }
        return atd.del();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //  throw new ErrMsgException("请先登录！");
        boolean re = true;
        String contactType="",contactDate="",contactContent="",satisfy="",feedback="",memo="";
        int customerNameId,linkId;
        double moneyCost, timeCost;
        int id = ParamUtil.getInt(request, "id");
        contactType = ParamUtil.get(request, "contactType");
        contactDate = ParamUtil.get(request, "contactDate");
        customerNameId = ParamUtil.getInt(request, "customerNameId");
        linkId = ParamUtil.getInt(request, "linkId");
        contactContent = ParamUtil.get(request, "contactContent");
        moneyCost = ParamUtil.getDouble(request, "moneyCost");
        timeCost = ParamUtil.getDouble(request, "timeCost");
        satisfy = ParamUtil.get(request, "satisfy");
        feedback = ParamUtil.get(request, "feedback");
        memo = ParamUtil.get(request, "memo");
        ContactDb cdb = getContactDb(id);
        cdb.setContactType(contactType);
        java.util.Date d = null;
        try {
            d = DateUtil.parse(contactDate, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("modify:" + e.getMessage());
        }
        cdb.setContactDate(d);
        cdb.setCustomerNameId(customerNameId);
        cdb.setLinkId(linkId);
        cdb.setContactContent(contactContent);
        cdb.setMoneyCost(moneyCost);
        cdb.setTimeCost(timeCost);
        cdb.setSatisfy(satisfy);
        cdb.setFeedback(feedback);
        cdb.setMemo(memo);
        cdb = getContactDb(id);
        re = cdb.save();
        return re;
    }
}
