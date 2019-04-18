package com.redmoon.oa.sale;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.redmoon.oa.visual.IModuleChecker;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import com.redmoon.oa.visual.FormDAO;
import java.util.Vector;
import java.util.Iterator;

import com.redmoon.oa.crm.CRMConfig;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormDb;
import cn.js.fan.util.DateUtil;

import com.redmoon.oa.message.*;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.sale.SalePrivilege;
import com.redmoon.oa.sms.SMSFactory;

/**
 * <p>Title: 服务的有效性验证</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SalesServiceChecker implements IModuleChecker {
    public SalesServiceChecker() {
        super();
    }

    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {
        return true;
    }
    
    public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws ErrMsgException {
 	
        return true;
    }

    public boolean validateDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        CRMConfig crmcfg = CRMConfig.getInstance();
        
        int isMessage = StrUtil.toInt(fdao.getFieldValue("is_message"), 0);
        if (isMessage==0)
        	return true;
        
        long customerId = StrUtil.toLong(fdao.getCwsId());
        
        FormDb fd = new FormDb();
        fd = fd.getFormDb("sales_customer");
        FormDAO fdaoCustomer = new FormDAO();
        fdaoCustomer = fdaoCustomer.getFormDAO(customerId, fd);
        
        String t = StrUtil.format(crmcfg.getProperty(
                "serviceRemindTitle"),
                                  new
                                  Object[] {fdaoCustomer.getFieldValue(
                "customer")});
        String c = StrUtil.format(crmcfg.getProperty(
                "serviceRemindContent"),
                                  new
                                  Object[] {fdaoCustomer.getFieldValue(
                "customer"), fdao.getFieldValue("contact_date"), fdao.getFieldValue("contact_content")
        });
        
        
        IMessage imsg = null;
        ProxyFactory proxyFactory = new ProxyFactory(
                "com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();
        MessageDb md = new MessageDb();
        // 是否发送短信
        boolean isToMobile = SMSFactory.isUseSMS();
        
        if (!isToMobile)
            md.sendSysMsg(fdao.getFieldValue("person"), t, c);
        else {
            if (imsg != null)
                imsg.sendSysMsg(fdao.getFieldValue("person"), t, c);
        }
        
        return true;
    }

    public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
    	return false;
    }
    
    public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        CRMConfig crmcfg = CRMConfig.getInstance();
        
        int isMessage = StrUtil.toInt(fdao.getFieldValue("is_message"), 0);
        if (isMessage==0)
        	return true;
        
        long customerId = StrUtil.toLong(fdao.getCwsId());
        
        FormDb fd = new FormDb();
        fd = fd.getFormDb("sales_customer");
        FormDAO fdaoCustomer = new FormDAO();
        fdaoCustomer = fdaoCustomer.getFormDAO(customerId, fd);
        
        String t = StrUtil.format(crmcfg.getProperty(
                "serviceRemindTitle"),
                                  new
                                  Object[] {fdaoCustomer.getFieldValue(
                "customer")});
        String c = StrUtil.format(crmcfg.getProperty(
                "serviceRemindContent"),
                                  new
                                  Object[] {fdaoCustomer.getFieldValue(
                "customer"), fdao.getFieldValue("contact_date"), fdao.getFieldValue("contact_content")
        });
        
        
        IMessage imsg = null;
        ProxyFactory proxyFactory = new ProxyFactory(
                "com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();
        MessageDb md = new MessageDb();
        // 是否发送短信
        boolean isToMobile = SMSFactory.isUseSMS();
        
        if (!isToMobile)
            md.sendSysMsg(fdao.getFieldValue("person"), t, c);
        else {
            if (imsg != null)
                imsg.sendSysMsg(fdao.getFieldValue("person"), t, c);
        }
            	
        return true;
    }    
}
