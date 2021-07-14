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
public class LinkmanMgr {
    Logger logger = Logger.getLogger(LinkmanMgr.class.getName());
    public LinkmanMgr() {
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //throw new ErrMsgException("请先登录！");
        LinkmanDb ldb = new LinkmanDb();
        boolean re = true;
        //customerNameId,postPriv,linkmanName,linkmanSex,birthday,hobby
       //postcodeHome,addHome,telNoWork,telNoHome,mobile,email,oicq,msn,demo
        String postPriv="",linkmanName="", linkmanSex="" ,birthday="", hobby="", postcodeHome="" ,addHome="" ,telNoWork="", telNoHome="", mobile="" ,email="",oicq="", msn="", demo="" ;
        int customerNameId = ParamUtil.getInt(request, "customerNameId");
        postPriv = ParamUtil.get(request, "postPriv");
        linkmanName = ParamUtil.get(request, "linkmanName");
        linkmanSex = ParamUtil.get(request, "linkmanSex");
        birthday = ParamUtil.get(request, "birthday");
        hobby = ParamUtil.get(request, "hobby");
        postcodeHome = ParamUtil.get(request, "postcodeHome");
        addHome = ParamUtil.get(request, "addHome");
        telNoWork = ParamUtil.get(request, "telNoWork");
        telNoHome = ParamUtil.get(request, "telNoHome");
        mobile = ParamUtil.get(request, "mobile");
        email = ParamUtil.get(request, "email");
        oicq = ParamUtil.get(request, "oicq");
        msn = ParamUtil.get(request, "msn");
        demo = ParamUtil.get(request, "demo");
        ldb.setCustomerNameId(customerNameId);
        ldb.setPostPriv(postPriv);
        ldb.setLinkmanName(linkmanName);
        ldb.setLinkmanSex(linkmanSex);
        java.util.Date d = null;
        try {
            d = DateUtil.parse(birthday, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("create:" + e.getMessage());
        }
        ldb.setBirthday(d);
        ldb.setHobby(hobby);
        ldb.setPostcodeHome(postcodeHome);
        ldb.setAddHome(addHome);
        ldb.setTelNoWork(telNoWork);
        ldb.setTelNoHome(telNoHome);
        ldb.setMobile(mobile);
        ldb.setEmail(email);
        ldb.setOicq(oicq);
        ldb.setMsn(msn);
        ldb.setDemo(demo);
        re = ldb.create();
        return re;
    }
    public LinkmanDb getLinkmanDb(int id) {
     LinkmanDb atd = new LinkmanDb();
     return atd.getLinkmanDb(id);
    }
    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        LinkmanDb atd = getLinkmanDb(id);
        if (atd == null || !atd.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }
        return atd.del();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //  throw new ErrMsgException("请先登录！");
        boolean re = true;
        String  postPriv="",linkmanName="", linkmanSex="" ,birthday="", hobby="", postcodeHome="" ,addHome="" ,telNoWork="", telNoHome="", mobile="" ,email="",oicq="", msn="", demo="" ;
        int id = ParamUtil.getInt(request, "id");
        int customerNameId = ParamUtil.getInt(request, "customerNameId");
        postPriv = ParamUtil.get(request, "postPriv");
        linkmanName = ParamUtil.get(request, "linkmanName");
        linkmanSex = ParamUtil.get(request, "linkmanSex");
        birthday = ParamUtil.get(request, "birthday");
        hobby = ParamUtil.get(request, "hobby");
        postcodeHome = ParamUtil.get(request, "postcodeHome");
        addHome = ParamUtil.get(request, "addHome");
        telNoWork = ParamUtil.get(request, "telNoWork");
        telNoHome = ParamUtil.get(request, "telNoHome");
        mobile = ParamUtil.get(request, "mobile");
        email = ParamUtil.get(request, "email");
        oicq = ParamUtil.get(request, "oicq");
        msn = ParamUtil.get(request, "msn");
        demo = ParamUtil.get(request, "demo");
        LinkmanDb ldb = getLinkmanDb(id);
        ldb.setCustomerNameId(customerNameId);
        ldb.setPostPriv(postPriv);
        ldb.setLinkmanName(linkmanName);
        ldb.setLinkmanSex(linkmanSex);
        java.util.Date d = null;
        try {
            d = DateUtil.parse(birthday, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("modify:" + e.getMessage());
        }
        ldb.setBirthday(d);
        ldb.setHobby(hobby);
        ldb.setPostcodeHome(postcodeHome);
        ldb.setAddHome(addHome);
        ldb.setTelNoWork(telNoWork);
        ldb.setTelNoHome(telNoHome);
        ldb.setMobile(mobile);
        ldb.setEmail(email);
        ldb.setOicq(oicq);
        ldb.setMsn(msn);
        ldb.setDemo(demo);
        ldb = getLinkmanDb(id);
        re = ldb.save();
        return re;
    }
}
