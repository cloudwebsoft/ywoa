package com.redmoon.oa.visual.module;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.crm.CRMConfig;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sale.SalePrivilege;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.IModuleChecker;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class LinkmanChecker implements IModuleChecker {
    public LinkmanChecker() {
    }

    /**
     * onDel
     *
     * @param fdao FormDAO
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.oa.visual.IModuleChecker method
     */
    @Override
    public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    public FormField getFormField(Vector fields, String fieldName) {
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getName().equals(fieldName))
                return ff;
        }
        return null;
    }

    /**
     * validateCreate
     *
     * @param request HttpServletRequest
     * @param fields Vector
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.oa.visual.IModuleChecker method
     */
    @Override
    public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws
            ErrMsgException {
        FormField ff = getFormField(fields, "linkmanName");
        if (ff==null)
            throw new ErrMsgException("姓名不能为空！");
        if (ff.getValue().equals(""))
            throw new ErrMsgException("联系人姓名不能为空！");
        ff = getFormField(fields, "customer");
        if (ff==null)
            throw new ErrMsgException("客户不能为空！");
        if (ff.getValue().equals(""))
            throw new ErrMsgException("客户名称不能为空！");
        return true;
    }

    /**
     * validateDel
     *
     * @param request HttpServletRequest
     * @param fdao FormDAO
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.oa.visual.IModuleChecker method
     */
    @Override
    public boolean validateDel(HttpServletRequest request, FormDAO fdao) throws
            ErrMsgException {
    	long customerId = StrUtil.toLong(fdao.getFieldValue("customer"));
    	if (!SalePrivilege.canUserManageCustomer(request, customerId))
    		throw new ErrMsgException("您对客户无管理权限，不能删除！");
        
		FormDb fd = new FormDb();
		fd = fd.getFormDb("sales_customer");
		FormDAO fdaoCust = new FormDAO();
		fdaoCust = fdaoCust.getFormDAO(customerId, fd);
        String dt = fdaoCust.getFieldValue("find_date");

        CRMConfig myconfig = CRMConfig.getInstance();
        int canDelDays = myconfig.getIntProperty("canDelDays");
        
        Privilege pvg = new Privilege();
        java.util.Date t = DateUtil.parse(dt, "yyyy-MM-dd");
        if (DateUtil.datediff(new java.util.Date(), t)>canDelDays) {
        	if (!pvg.isUserPrivValid(request, "sales")) {
        		throw new ErrMsgException("客户记录已超过" + canDelDays + "天，仅销售总管理才能删除！");
        	}
        }    	
    	
        return true;
    }

    /**
     * validateUpdate
     *
     * @param request HttpServletRequest
     * @param fields Vector
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.oa.visual.IModuleChecker method
     */
    @Override
    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {
        FormField ff = getFormField(fields, "linkmanName");
        if (ff==null)
            throw new ErrMsgException("姓名不能为空！");
        if (ff.getValue().equals(""))
            throw new ErrMsgException("联系人姓名不能为空！");
        ff = getFormField(fields, "customer");
        if (ff==null)
            throw new ErrMsgException("客户不能为空！");
        if (ff.getValue().equals(""))
            throw new ErrMsgException("客户名称不能为空！");
        return true;
    }

    @Override
    public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
    	return true;
    }

    
    @Override
    public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
    	return false;
    } 

    @Override
    public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }
}
