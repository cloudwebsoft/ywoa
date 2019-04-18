package com.redmoon.oa.officeequip;

import java.util.Iterator;
import java.util.List;

import cn.js.fan.util.*;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;

import com.redmoon.oa.basic.TreeSelectDb;
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
public class OfficeMgr {

	public OfficeMgr() {
	}

	public boolean create(HttpServletRequest request) throws ErrMsgException,
			ResKeyException {
		boolean re = true;
		String measureUnit = "", officeName = "", buyPerson = "", buyDate = "", abstracts = "";
		double price;
		Privilege pvg = new Privilege();
		String operator = pvg.getUser(request);
		officeName = ParamUtil.get(request, "officeName");
		if (officeName.equals(""))
			throw new ErrMsgException("请选择办公用品！");
		TreeSelectDb tsd = new TreeSelectDb(officeName);
		if (tsd == null || !tsd.isLoaded()) {
			throw new ErrMsgException("请先录入办公用品！");
		}
		if (tsd.getChildCount() > 0) {
			throw new ErrMsgException("请选择办公用品！");
		}
		measureUnit = ParamUtil.get(request, "measureUnit");
		if (measureUnit.equals("")) {
			throw new ErrMsgException("请输入计量单位！");
		}
		buyPerson = ParamUtil.get(request, "buyPerson");
		int storageCount = 0;
		try {
			storageCount = ParamUtil.getInt(request, "storageCount");
		} catch (ErrMsgException e) {
			throw new ErrMsgException("请输入正确数量！");
		}
		if (storageCount <= 0) {
			throw new ErrMsgException("数量必须至少为1！");
		}
		buyDate = ParamUtil.get(request, "buyDate");
		price = ParamUtil.getDouble(request, "price", 0);
		if (price == 0) {
			throw new ErrMsgException("请输入正确价格！");
		}
		if (storageCount < 0.0) {
			throw new ErrMsgException("价格不能为负数！");
		}
		abstracts = ParamUtil.get(request, "abstracts");

		String unitCode = ParamUtil.get(request, "unitCode");

		OfficeDb office = new OfficeDb();
		office.setBuyPerson(buyPerson);
		office.setMeasureUnit(measureUnit);
		office.setOfficeName(officeName);
		office.setStorageCount(storageCount);
		// office.setTypeId(Integer.parseInt(typeId));
		office.setPrice(price);
		office.setUnitCode(unitCode);
		office.setOperator(operator);
		java.util.Date d = null;
		if (buyDate.equals("")) {
			d = new java.util.Date();
		} else {
			d = DateUtil.parse(buyDate, "yyyy-MM-dd");
		}
		office.setBuyDate(d);
		office.setAbstracts(abstracts);
		re = office.create();

		// add by tbl 增加盘点表记录
		OfficeStocktakingDb officest = new OfficeStocktakingDb();
		officest.setEquipmentCode(officeName);
		officest.setStockNum(storageCount);
		officest.setOperator(operator);
		boolean re1 = officest.save();

		if (re && re1) {
			return true;
		}
		return false;
		// add by tbl
	}

	public OfficeDb getOfficeDb(int id) {
		OfficeDb addr = new OfficeDb();
		return addr.getOfficeDb(id);
	}

	public boolean chageStorageCount(HttpServletRequest request)
			throws ErrMsgException {
		boolean re = true;
		String officeName = ParamUtil.get(request, "officeName");

		if (officeName.equals(""))
			throw new ErrMsgException("请选择办公用品！");
		TreeSelectDb tsd = new TreeSelectDb(officeName);
		if (tsd.getChildCount() > 0) {
			throw new ErrMsgException("请选择办公用品！");
		}

		int storageCount = 0;
		try {
			storageCount = ParamUtil.getInt(request, "storageCount");
		} catch (ErrMsgException e) {
			throw new ErrMsgException("请输入正确数量！");
		}
		if (storageCount <= 0) {
			throw new ErrMsgException("数量必须至少为1！");
		}

		int type = ParamUtil.getInt(request, "type");
		String us = "";
		switch (type) {
		case OfficeOpDb.TYPE_RECEIVE:
			us = "领用";
			break;
		case OfficeOpDb.TYPE_BORROW:
			us = "借用";
			break;
		case OfficeOpDb.TYPE_RETURN:
			us = "归还";
			break;
		default:
			us = "领用";
			break;
		}

		String userName = ParamUtil.get(request, "person");
		if (userName.equals("")) {
			throw new ErrMsgException("请选择" + us + "人！");
		}
		UserDb ud = new UserDb(userName);
		if (ud == null || !ud.loaded) {
			throw new ErrMsgException(us + "人不存在！");
		}

		// add by tbl 改变盘点表中库存数
		OfficeStocktakingDb osd = new OfficeStocktakingDb();
		re = osd.changeStocknum(storageCount, officeName);
		return re;
		// add by tbl
	}

	public boolean chStorageCount(HttpServletRequest request)
			throws ErrMsgException {
		boolean re = true;
		int id = ParamUtil.getInt(request, "officeId");
		int storageCount = ParamUtil.getInt(request, "count");
		OfficeDb odb = new OfficeDb();
		odb = odb.getOfficeDb(id);
		storageCount = odb.getStorageCount() + storageCount;
		odb.setStorageCount(storageCount);
		re = odb.save();
		return re;
	}

	/**
	 * 归还后改变数量
	 */
	public boolean returnChageStorageCount(HttpServletRequest request)
			throws ErrMsgException {
		boolean re = true;
		String officeName = ParamUtil.get(request, "officeName");
		int storageCount = ParamUtil.getInt(request, "count");
		// add by tbl 改变库存表数量
		OfficeStocktakingDb osd = new OfficeStocktakingDb();
		re = osd.returnChangeStocknum(storageCount, officeName);
		// add by tbl
		return re;
	}

	public boolean del(HttpServletRequest request) throws ErrMsgException {
		int id = ParamUtil.getInt(request, "id");
		OfficeDb OfficeDb = getOfficeDb(id);
		
		if (OfficeDb == null || !OfficeDb.isLoaded())
			throw new ErrMsgException("该项已不存在！");
		// edit by tbl
		OfficeOpDb ood = new OfficeOpDb();
//		if (ood.hasOfficeOfType(id)) {
//			throw new ErrMsgException("此已货品借出，归还货品后才能删除货物！");
//		}
		if (ood.isOfficeCodeExist(OfficeDb.getOfficeName())>0) {
			throw new ErrMsgException("此用品已有领用、借出、归还记录，不能删除该用品！");
		}
//		OfficeStocktakingDb osd = new OfficeStocktakingDb();
//		if (osd.isOfficeCodeExist(OfficeDb.getOfficeName())>0) {
//			throw new ErrMsgException("此用品已有盘点记录，不能删除该用品！");
//		}
		//edit by tbl

		return OfficeDb.del();
	}
	
	public boolean delBatch(HttpServletRequest request) throws ErrMsgException {
		String ids = ParamUtil.get(request, "ids");
		String[] idAry = ids.split(",");
		for (String id : idAry) {
			OfficeDb OfficeDb = getOfficeDb(StrUtil.toInt(id));

			if (OfficeDb == null || !OfficeDb.isLoaded())
				throw new ErrMsgException("该项已不存在！");
			OfficeOpDb ood = new OfficeOpDb();
			if (ood.isOfficeCodeExist(OfficeDb.getOfficeName()) > 0) {
				throw new ErrMsgException("此用品已有领用、借出、归还记录，不能删除该用品！");
			}

			OfficeDb.del();
		}
		return true;
	}
	
	public boolean delTree(HttpServletRequest request) throws ErrMsgException {
		String officeCode = ParamUtil.get(request, "delcode");
		OfficeDb od = new OfficeDb();
		boolean bool = true;
		List<OfficeDb> officeDbs = od.queryDataByOfficeCode(officeCode);
		
		Iterator<OfficeDb> iterator = officeDbs.iterator();

		while (iterator.hasNext()) {
			OfficeDb officeDb = iterator.next();
			if (officeDb == null )
				throw new ErrMsgException("该用品已不存在！");
			OfficeOpDb ood = new OfficeOpDb();
			if (officeDb.getStorageCount() > 0) {
				throw new ErrMsgException("此用品已有入库记录，不能删除该用品！");
			}
			if (ood.isOfficeCodeExist(officeDb.getOfficeName())>0) {
				throw new ErrMsgException("此用品已有领用、借出、归还记录，不能删除该用品！");
			}
//			OfficeStocktakingDb osd = new OfficeStocktakingDb();
//			if (osd.isOfficeCodeExist(officeDb.getOfficeName())>0) {
//				throw new ErrMsgException("此用品已有盘点记录，不能删除该用品！");
//			}
			
			if (!officeDb.del()) bool = false;
		}
		return bool;
	}

	// add by tbl
	// 盘点操作
	public boolean tookstock(HttpServletRequest request) throws ErrMsgException {
		String officeName = ParamUtil.get(request, "officeName");

		if (officeName.equals(""))
			throw new ErrMsgException("请选择办公用品！");
		TreeSelectDb tsd = new TreeSelectDb(officeName);
		if (tsd.getChildCount() > 0) {
			throw new ErrMsgException("请选择办公用品！");
		}
		int realNum = 0;
		try {
			realNum = ParamUtil.getInt(request, "realNum");
		} catch (ErrMsgException e) {
			throw new ErrMsgException("请输入正确库存数量！");
		}

		OfficeStocktakingDb officeStocktakingDb = new OfficeStocktakingDb();
		officeStocktakingDb.setRealNum(realNum);
		officeStocktakingDb.setEquipmentCode(officeName);

		if (officeStocktakingDb.hasEquipCode(officeName) == -1) {
			throw new ErrMsgException("该办公用品不存在，请先入库！");
		}
		
		Privilege pvg = new Privilege();
		String operator = pvg.getUser(request);		
		officeStocktakingDb.setOperator(operator);

		return (officeStocktakingDb.updateStock() && officeStocktakingDb
				.insertStock());
	}
	// add by tbl
}
