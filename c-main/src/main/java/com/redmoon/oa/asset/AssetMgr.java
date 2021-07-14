package com.redmoon.oa.asset;

import cn.js.fan.db.Conn;
import cn.js.fan.util.*;
import java.sql.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import cn.js.fan.db.*;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class AssetMgr {
    Logger logger = Logger.getLogger(AssetMgr.class.getName());

    public AssetMgr() {

    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //throw new ErrMsgException("请先登录！");
//name, type,  number, typeId , addId, department, buyMan,
//keeper, inputMan,startDate, buyDate, regDate,abstracts
        AssetDb atd = new AssetDb();
        boolean re = true;
        String name = "", type = "", number = "", department = "", buyMan = "";
        String keeper = "", inputMan = "", startDate = "", buyDate = "",
                regDate = "", abstracts = "" , addId = "";
        String errmsg = "";
        name = ParamUtil.get(request, "name");
        type = ParamUtil.get(request, "type");
        number = ParamUtil.get(request, "number");
        department = ParamUtil.get(request, "department");
        buyMan = ParamUtil.get(request, "buyMan");
        keeper = ParamUtil.get(request, "keeper");
        inputMan = ParamUtil.get(request, "inputMan");
        startDate = ParamUtil.get(request, "startDate");
        buyDate = ParamUtil.get(request, "buyDate");
        regDate = ParamUtil.get(request, "regDate");
        int typeId = ParamUtil.getInt(request, "typeId");
        addId = ParamUtil.get(request, "addId");
        abstracts = ParamUtil.get(request, "abstracts");
        double price = ParamUtil.getDouble(request, "price");
        if (name.equals("")) {
            errmsg += "名称不能为空！\\n";
        }
        if (number.equals("")){
            errmsg +="编号不能为空！\\n";
        }
        if (atd.isExist(number)){
            errmsg +="编号不能相同，请重新输入编号！\\n";
        }
        if (typeId==0)
                 throw new ErrMsgException("请您选择资产类别！\\n");
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
        
        long amount = ParamUtil.getLong(request, "amount", 0);
        
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(department);
		String unitCode = dd.getUnitOfDept(dd).getCode();

		atd.setName(name);
		atd.setAddId(addId);
		atd.setNumber(number);
		atd.setTypeId(typeId);
		atd.setType(type);
		atd.setDepartment(department);
		atd.setBuyMan(buyMan);
		atd.setKeeper(keeper);
		atd.setInputMan(inputMan);
		
		atd.setAmount(amount);
		atd.setUnitCode(unitCode);

		java.util.Date d = null;
		try {
			d = DateUtil.parse(startDate, "yyyy-MM-dd");
		} catch (Exception e) {
			logger.error("create:" + e.getMessage());
		}
		atd.setStartDate(d);
		try {
			d = DateUtil.parse(buyDate, "yyyy-MM-dd");
		} catch (Exception e) {
			logger.error("create:" + e.getMessage());
		}
		atd.setBuyDate(d);
		try {
			d = DateUtil.parse(regDate, "yyyy-MM-dd");
		} catch (Exception e) {
			logger.error("create:" + e.getMessage());
		}
		atd.setRegDate(d);
		atd.setAbstracts(abstracts);
		atd.setPrice(price);
		re = atd.create();
		return re;
    }

    public AssetDb getAssetDb(int id) {
     AssetDb atd = new AssetDb();
     return atd.getAssetDb(id);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        AssetDb atd = getAssetDb(id);
        if (atd == null || !atd.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }

        //BookDb bd = new BookDb();
        //if (bd.hasBookOfType(id)) {
        //     String info = SkinUtil.LoadString(request, "res.module.book", "warn_type_del_hasbook");
        //    throw new ErrMsgException(info);
        // }

        return atd.del();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //  throw new ErrMsgException("请先登录！");
        boolean re = true;
        String name = "", type = "", number = "", department = "", buyMan = "";
        String keeper = "", inputMan = "", startDate = "", buyDate = "",
                regDate = "", abstracts = "";
        String errmsg = "";
        int id = ParamUtil.getInt(request,"id");
        name = ParamUtil.get(request, "name");
        type = ParamUtil.get(request, "type");
        number = ParamUtil.get(request, "number");
        department = ParamUtil.get(request, "department");
        buyMan = ParamUtil.get(request, "buyMan");
        keeper = ParamUtil.get(request, "keeper");
        inputMan = ParamUtil.get(request, "inputMan");
        startDate = ParamUtil.get(request, "startDate");
        buyDate = ParamUtil.get(request, "buyDate");
        regDate = ParamUtil.get(request, "regDate");
        int typeId = ParamUtil.getInt(request, "typeId");
        String addId = ParamUtil.get(request, "addId");
        abstracts = ParamUtil.get(request, "abstracts");
        double price = ParamUtil.getDouble(request, "price");
        if (name.equals("")) {
            errmsg += "名称不能为空！\\n";
        }
        if (number.equals("")){ 
            errmsg +="编号不能为空！\\n";
        }
        if (typeId==0)
                 throw new ErrMsgException("请您选择资产类别！\\n");
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
        
        long amount = ParamUtil.getLong(request, "amount", 0);
        LogUtil.getLog(getClass()).info("amount=" + amount); 
        
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(department);
		String unitCode = dd.getUnitOfDept(dd).getCode();    

		AssetDb atd = getAssetDb(id);
		// name, type, number, typeId , addId, department, buyMan,
		// keeper, inputMan,startDate, buyDate, regDate,abstracts
		atd.setName(name);
		atd.setAddId(addId);
		atd.setNumber(number);
		atd.setTypeId(typeId); 
		atd.setType(type);
		atd.setDepartment(department);
		atd.setBuyMan(buyMan);
		atd.setKeeper(keeper);
		atd.setInputMan(inputMan);
		java.util.Date d = null;
		try {
			d = DateUtil.parse(startDate, "yyyy-MM-dd");
		} catch (Exception e) {
			logger.error("create:" + e.getMessage());
		}
		atd.setStartDate(d);
		try {
			d = DateUtil.parse(buyDate, "yyyy-MM-dd");
		} catch (Exception e) {
			logger.error("create:" + e.getMessage());
		}
		atd.setBuyDate(d);
		try {
			d = DateUtil.parse(regDate, "yyyy-MM-dd");
		} catch (Exception e) {
			logger.error("create:" + e.getMessage());
		}
		atd.setAbstracts(abstracts);
		atd.setPrice(price);

		atd.setAmount(amount);
		atd.setUnitCode(unitCode);

		re = atd.save();
		return re;

    }


}
