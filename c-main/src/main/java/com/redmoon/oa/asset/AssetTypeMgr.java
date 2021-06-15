package com.redmoon.oa.asset;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.*;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.SkinUtil;

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
public class AssetTypeMgr {
    Logger logger = Logger.getLogger(AssetTypeMgr.class.getName());

    public AssetTypeMgr() {
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //throw new ErrMsgException("请先登录！");
        boolean re = true;
        String errmsg = "";
        String name = ParamUtil.get(request, "name");
        String depreciationRate = ParamUtil.get(request, "depreciationRate");
        String depreciationYears = ParamUtil.get(request, "depreciationYears");
        String abstracts = ParamUtil.get(request, "abstracts");
        if (name.equals(""))
            errmsg += "名称不能为空！\\n";
        
    	if (!SQLFilter.isValidSqlParam(name)) {
    		Privilege privilege = new Privilege();
    		com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ AssetTypeMgr create, param:name=" + name);
    		throw new ErrMsgException(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "param_invalid")));
    	}        
        
        if (StrUtil.toDouble(depreciationRate) > 1)
            errmsg += "折旧率必须是小于1的小数！\\n";

        if (!StrUtil.isNumeric(depreciationYears)) {
                errmsg += "折旧年限输入错误！\\n";
        }
        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);
        AssetTypeDb atd = new AssetTypeDb();
        if (atd.isExist(name))
            throw new ErrMsgException("该类别已存在!");
        else {
            atd.setName(name);
            atd.setAbstracts(abstracts);
            atd.setDepreciationRate(depreciationRate);
            atd.setDepreciationYears(Integer.parseInt(depreciationYears));
            re = atd.create();
        }
        return re;
    }

    public AssetTypeDb getAssetTypeDb(int id) {
        AssetTypeDb atd = new AssetTypeDb();
        return atd.getAssetTypeDb(id);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        AssetTypeDb atd = getAssetTypeDb(id);
        if (atd == null || !atd.isLoaded())
            throw new ErrMsgException("该项已不存在！");
        AssetDb adb = new AssetDb();
        if (adb.hasAssetOfType(id)) {
            // String info = SkinUtil.LoadString(request, "res.module.book", "warn_type_del_hasbook");
            throw new ErrMsgException("该类别下面已有资产，必需删除后才能执行此操作！");
        }
        return atd.del();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        //if (!privilege.isUserLogin(request))
        //  throw new ErrMsgException("请先登录！");
        boolean re = true;
        String errmsg = "";
        int id = ParamUtil.getInt(request, "id");
        String name = ParamUtil.get(request, "name");
        String depreciationRate = ParamUtil.get(request, "depreciationRate");
        int depreciationYears = ParamUtil.getInt(request, "depreciationYears");
        String abstracts = ParamUtil.get(request, "abstracts");
        if (name.equals(""))
            errmsg += "名称不能为空！\\n";
        if (StrUtil.toDouble(depreciationRate) > 1)
            errmsg += "折旧率必须是小于1的小数！\\n";

        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);
        AssetTypeDb atd = getAssetTypeDb(id);
        atd.setName(name);
        atd.setAbstracts(abstracts);
        atd.setDepreciationRate(depreciationRate);
        atd.setDepreciationYears(depreciationYears);
        re = atd.save();
        return re;

    }


}
