package com.redmoon.oa.sale;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.module.sales.CustomerShareDb;

public class SalePrivilege {
	/**
	 * 用户能否看到客户及其相关资料，只有管理员、发现者、共享用户、部门管理员及所属的业务员能看到，其它人都不能看到
	 * 回落客户大家都能看到
	 * @param request
	 * @param customerId
	 * @return
	 */
	public static boolean canUserSeeCustomer(HttpServletRequest request, long customerId) {
		Privilege privilege = new Privilege();
		FormDb fd = new FormDb();
		fd = fd.getFormDb("sales_customer");
		FormDAO fdao = new FormDAO();
		fdao = fdao.getFormDAO(customerId, fd);
		// 回落客户大家都能看到
		if ("2".equals(fdao.getFieldValue("kind")))
			return true;
		
		String salesPerson = StrUtil.getNullStr(fdao.getFieldValue("sales_person"));
		String founder = StrUtil.getNullStr(fdao.getFieldValue("founder"));
		String myname = privilege.getUser(request);
		if (privilege.isUserPrivValid(request, "sales") || myname.equals(salesPerson) || myname.equals(founder))
			return true;
		else {
			if (privilege.canAdminUser(request, salesPerson)) {
				return true;
			}
			// 判断是否为共享客户
			else if (CustomerShareDb.isExist(myname, customerId))
				return true;
			else {
				return false;
			}
		}
		
	}

	/**
	 * 用户能否管理客户及其相关资料，仅允许部门管理员、销售经理及对应的销售员管理
	 * @param request
	 * @param customerId
	 * @return
	 */
	public static boolean canUserManageCustomer(HttpServletRequest request, long customerId) {
		Privilege privilege = new Privilege();
		FormDb fd = new FormDb();
		fd = fd.getFormDb("sales_customer");
		FormDAO fdao = new FormDAO();
		fdao = fdao.getFormDAO(customerId, fd);
		String salesPerson = StrUtil.getNullStr(fdao.getFieldValue("sales_person"));
		String myname = privilege.getUser(request);
		// System.out.println(SalePrivilege.class + " sales_person=" + salesPerson);
		if (privilege.isUserPrivValid(request, "sales") || myname.equals(salesPerson))
			return true;
		else {
			if (privilege.canAdminUser(request, salesPerson)) {
				return true;
			}			
			else
				return false;
		}
	}
	
	/**
	 * 销售总管理和部门管理员能够删除
	 * @Description: 
	 * @param request
	 * @param customerId
	 * @return
	 */
	public static boolean canUserDel(HttpServletRequest request, long customerId) {
		Privilege privilege = new Privilege();
		FormDb fd = new FormDb();
		fd = fd.getFormDb("sales_customer");
		FormDAO fdao = new FormDAO();
		fdao = fdao.getFormDAO(customerId, fd);
		String salesPerson = StrUtil.getNullStr(fdao.getFieldValue("sales_person"));
		// System.out.println(SalePrivilege.class + " sales_person=" + salesPerson);
		if (privilege.isUserPrivValid(request, "sales"))
			return true;
		else {
			if (privilege.canAdminUser(request, salesPerson)) {
				return true;
			}			
			else
				return false;
		}		
	}
}
