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
public class SaleMgr {
    Logger logger = Logger.getLogger(SaleMgr.class.getName());
    public SaleMgr() {
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //throw new ErrMsgException("请先登录！");
        SaleDb sdb = new SaleDb();
        boolean re = true;
        String customer="",  customerName="", ifShare="", customerCode="" , customerShort="" ,phone="",faxNo="" ,customerWWW="" ,email="" ,customerArea="" ,postalcode="" ,constomerAdd="" ,source="" ,kind="" ,sellMode="" ,attribute="" ,enterType="" ,enterMemo="" ,demo="";
        customer = ParamUtil.get(request, "customer");
        customerName = ParamUtil.get(request, "customerName");
        customerCode = ParamUtil.get(request, "customerCode");
        customerShort = ParamUtil.get(request, "customerShort");
        phone = ParamUtil.get(request, "phone");
        faxNo = ParamUtil.get(request, "faxNo");
        customerWWW = ParamUtil.get(request, "customerWWW");
        email = ParamUtil.get(request, "email");
        customerArea = ParamUtil.get(request, "customerArea");
        postalcode = ParamUtil.get(request, "postalcode");
        constomerAdd = ParamUtil.get(request, "constomerAdd");
        source = ParamUtil.get(request, "source");
        kind = ParamUtil.get(request, "kind");
        sellMode = ParamUtil.get(request, "sellMode");
        enterType = ParamUtil.get(request, "enterType");
        source = ParamUtil.get(request, "source");
        enterMemo = ParamUtil.get(request, "enterMemo");
        demo = ParamUtil.get(request, "demo");
        attribute= ParamUtil.get(request,"attribute");
        ifShare = ParamUtil.get(request, "ifShare");
        sdb.setCustomer(customer);
        if(ifShare=="1")
          sdb.setIfShare("1");
        else
          sdb.setIfShare("0");
        sdb.setCustomerName(customerName);
        sdb.setCustomerCode(customerCode);
        sdb.setCustomerShort(customerShort);
        sdb.setPhone(phone);
        sdb.setFaxNo(faxNo);
        sdb.setCustomerWWW(customerWWW);
        sdb.setEmail(email);
        sdb.setCustomerArea(customerArea);
        sdb.setPostalcode(postalcode);
        sdb.setConstomerAdd(constomerAdd);
        sdb.setSource(source);
        sdb.setKind(kind);
        sdb.setSellMode(sellMode);
        sdb.setAttribute(attribute);
        sdb.setEnterType(enterType);
        sdb.setEnterMemo(enterMemo);
        sdb.setDemo(demo);
        re = sdb.create();
        return re;
    }
    public SaleDb getSaleDb(int id) {
     SaleDb atd = new SaleDb();
     return atd.getSaleDb(id);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        SaleDb atd = getSaleDb(id);
        if (atd == null || !atd.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }
        return atd.del();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //  throw new ErrMsgException("请先登录！");
        boolean re = true;
        String customer="",  customerName="", ifShare="", customerCode="" , customerShort="" ,phone="",faxNo="" ,customerWWW="" ,email="" ,customerArea="" ,postalcode="" ,constomerAdd="" ,source="" ,kind="" ,sellMode="" ,attribute="" ,enterType="" ,enterMemo="" ,demo="";
        int id = ParamUtil.getInt(request,"id");
        customer = ParamUtil.get(request, "customer");
        customerName = ParamUtil.get(request, "customerName");
        customerCode = ParamUtil.get(request, "customerCode");
        customerShort = ParamUtil.get(request, "customerShort");
        phone = ParamUtil.get(request, "phone");
        faxNo = ParamUtil.get(request, "faxNo");
        customerWWW = ParamUtil.get(request, "customerWWW");
        email = ParamUtil.get(request, "email");
        if(!email.equals("") && !StrUtil.IsValidEmail(email))
            throw new ErrMsgException("请输入正确E-mail！\\n");
        customerArea = ParamUtil.get(request, "customerArea");
        postalcode = ParamUtil.get(request, "postalcode");
        constomerAdd = ParamUtil.get(request, "constomerAdd");
        source = ParamUtil.get(request, "source");
        kind = ParamUtil.get(request, "kind");
        sellMode = ParamUtil.get(request, "sellMode");
        enterType = ParamUtil.get(request, "enterType");
        source = ParamUtil.get(request, "source");
        enterMemo = ParamUtil.get(request, "enterMemo");
        demo = ParamUtil.get(request, "demo");
        attribute= ParamUtil.get(request,"attribute");
        ifShare = ParamUtil.get(request, "ifShare");
        SaleDb sdb = getSaleDb(id);
        sdb.setCustomer(customer);
        if(ifShare=="1")
          sdb.setIfShare("1");
        else
          sdb.setIfShare("0");
        sdb.setCustomerName(customerName);
        sdb.setCustomerCode(customerCode);
        sdb.setCustomerShort(customerShort);
        sdb.setPhone(phone);
        sdb.setFaxNo(faxNo);
        sdb.setCustomerWWW(customerWWW);
        sdb.setEmail(email);
        sdb.setCustomerArea(customerArea);
        sdb.setPostalcode(postalcode);
        sdb.setConstomerAdd(constomerAdd);
        sdb.setSource(source);
        sdb.setKind(kind);
        sdb.setSellMode(sellMode);
        sdb.setAttribute(attribute);
        sdb.setEnterType(enterType);
        sdb.setEnterMemo(enterMemo);
        sdb.setDemo(demo);
        re = sdb.save();
        return re;
    }
}
