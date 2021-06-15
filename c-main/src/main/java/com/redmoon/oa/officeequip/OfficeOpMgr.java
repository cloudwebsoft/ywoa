package com.redmoon.oa.officeequip;

import cn.js.fan.util.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class OfficeOpMgr {
	Logger logger = Logger.getLogger(OfficeOpMgr.class.getName());

	public OfficeOpMgr() {
	}

	public boolean create(HttpServletRequest request) throws ErrMsgException {
		boolean re = true;
		int type;
		String person = "", opDate = "", remark = "", /* officeId = "", */count = "";
		// officeId = ParamUtil.get(request, "equipId");
		String officeName = "";
		officeName = ParamUtil.get(request, "officeName");
		if ("".equals(officeName)) {
			throw new ErrMsgException("用品名称不能为空！");
		}
		
		Privilege pvg = new Privilege();
		String operator = pvg.getUser(request);		

		person = ParamUtil.get(request, "person");
		UserDb ud = new UserDb();
		ud = ud.getUserDb(person);
		if (ud == null || !ud.isLoaded()) {
			throw new ErrMsgException("用户" + person + "不存在！");
		}

		count = ParamUtil.get(request, "storageCount");
		if (!StrUtil.isNumeric(count)) {
			throw new ErrMsgException("数量不能为空！");
		}
		opDate = ParamUtil.get(request, "opDate");
		remark = ParamUtil.get(request, "abstracts");

		OfficeOpDb office = new OfficeOpDb();
		type = ParamUtil.getInt(request, "type");
		office.setOfficeCode(officeName);
		office.setCount(Integer.parseInt(count));
		office.setType(type);
		java.util.Date d = null;
		try {
			d = DateUtil.parse(opDate, "yyyy-MM-dd");
		} catch (Exception e) {
			logger.error("create:" + e.getMessage());
		}
		office.setOpDate(d);
		office.setPerson(person);
		office.setRemark(remark);
		office.setOperator(operator);

		re = office.create();
		return re;
	}

	public OfficeOpDb getOfficeOpDb(int id) {
		OfficeOpDb addr = new OfficeOpDb();
		return addr.getOfficeOpDb(id);
	}

	public boolean del(HttpServletRequest request) throws ErrMsgException {
		boolean re = true;
		String strids = ParamUtil.get(request, "ids");
		String[] ids = strids.split(",");
		int id;
		for (int i = 0; i < ids.length; i++) {
			id = Integer.parseInt(ids[i]);
			OfficeOpDb od = getOfficeOpDb(id);
			re = od.del();
		}
		return re;
	}

	// 归还时修改库存
	public boolean returnOfficeEquip(HttpServletRequest request)
			throws ErrMsgException {
		boolean re = true;
		String returnDate = "";
		int officeId = ParamUtil.getInt(request, "id");
		returnDate = ParamUtil.get(request, "endDate");
		String remark = ParamUtil.get(request, "abstracts");
		Privilege pvg = new Privilege();
		String operator = pvg.getUser(request);		
		
		OfficeOpDb ood = new OfficeOpDb();
		ood = ood.getOfficeOpDb(officeId);
		ood.setType(OfficeOpDb.TYPE_RETURN);
		java.util.Date d = null;
		try {
			d = DateUtil.parse(returnDate, "yyyy-MM-dd");
		} catch (Exception e) {
			logger.error("returnOfficeEquip:" + e.getMessage());
		}
		ood.setReturnDate(d);
		ood.setRemark(remark);
		ood.setOperator(operator);
		re = ood.save();
		return re;
	}

}
