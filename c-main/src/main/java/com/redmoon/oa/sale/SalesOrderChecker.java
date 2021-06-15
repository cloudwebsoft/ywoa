package com.redmoon.oa.sale;


import com.redmoon.oa.visual.IModuleChecker;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import com.redmoon.oa.visual.FormDAO;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormDb;
import cn.js.fan.util.DateUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.sale.SalePrivilege;

/**
 * <p>Title: 订单的有效性验证</p>
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
public class SalesOrderChecker implements IModuleChecker {
    public SalesOrderChecker() {
        super();
    }

    @Override
    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {
        // System.out.println(getClass() + " validateUpdate");

        Privilege pvg = new Privilege();

        String creator = fdaoBeforeUpdate.getCreator();
        if (!creator.equals(pvg.getUser(request))) {
            long customerId = StrUtil.toLong(fdaoBeforeUpdate.getCwsId());
            if (!SalePrivilege.canUserManageCustomer(request, customerId)) {
                throw new ErrMsgException("权限非法：只有行动创建者、客户对应的销售员本人、部门管理员和销售总管理才能编辑！");
            }
        }
        return true;

    }
    @Override
    public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws ErrMsgException {
    	long customerId = StrUtil.toLong(fu.getFieldValue("cws_id"));
        if (!SalePrivilege.canUserSeeCustomer(request, customerId)) {
            throw new ErrMsgException("权限非法：只有管理员、发现者、共享用户、部门管理员及所属的业务员有操作权限！");
        }    	
        return true;
    }

    @Override
    public boolean validateDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        long customerId = StrUtil.toLong(fdao.getCwsId());
        if (!SalePrivilege.canUserManageCustomer(request, customerId)) {
            throw new ErrMsgException("权限非法：只有客户对应的销售员本人、部门管理者和销售总管理才能删除！");
        }

        return true;
    }

    @Override
    public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    @Override
    public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
    	/*
    	long customerId = StrUtil.toInt(fdao.getFieldValue("customer"));
    	
    	FormDb fd = new FormDb();
    	fd = fd.getFormDb("sales_customer");
    	FormDAO fdaoCust = new FormDAO();
    	fdaoCust = fdaoCust.getFormDAO(customerId, fd);
    	
    	fdao.setCwsId("" + fdaoCust.getId());
    	*/
    	
    	fdao.setCwsId(fdao.getFieldValue("customer"));

    	return fdao.save();
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
