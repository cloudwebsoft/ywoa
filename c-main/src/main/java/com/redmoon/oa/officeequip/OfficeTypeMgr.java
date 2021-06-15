package com.redmoon.oa.officeequip;
import cn.js.fan.util.*;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
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
public class OfficeTypeMgr {
    Logger logger = Logger.getLogger(OfficeTypeMgr.class.getName());

    public OfficeTypeMgr() {

    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        boolean re = true;
        String errmsg = "";

        int id = ParamUtil.getInt(request, "id");
        String name = ParamUtil.get(request, "name");
        if (name.equals(""))
            errmsg += "名称不能为空！\\n";
        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);

        OfficeTypeDb otd = getOfficeTypeDb(id);
        otd.setName(name);
        re = otd.save();
        return re;
    }

    public OfficeTypeDb getOfficeTypeDb(int id) {
        OfficeTypeDb addr = new OfficeTypeDb();
        return addr.getOfficeTypeDb(id);
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        boolean re = true;
        String errmsg = "" , unit = "", abstracts ="";
        String name = ParamUtil.get(request, "name");
        // 防注入
        if (!cn.js.fan.db.SQLFilter.isValidSqlParam(name)) {
        	Privilege privilege = new Privilege();
        	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ OfficeTypeMgr name=" + name);
        	throw new ErrMsgException(SkinUtil.LoadString(request, "param_invalid"));
        }	
        
        unit = ParamUtil.get(request, "unit");
        abstracts = ParamUtil.get(request,"abstracts");
        if (name.equals(""))
            errmsg += "名称不能为空！\\n";
        if (unit.equals(""))
            errmsg += "参考单位不能为空！\\n";
        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);
        OfficeTypeDb otd = new OfficeTypeDb();
        if (otd.isExist(name))
            throw new ErrMsgException("该类别已存在!");
        else {
            otd.setName(name);
            otd.setUnit(unit);
            otd.setAbstracts(abstracts);
            re = otd.create();
        }
        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        OfficeTypeDb OfficeTypeDb = getOfficeTypeDb(id);
        if (OfficeTypeDb == null || !OfficeTypeDb.isLoaded())
            throw new ErrMsgException("该项已不存在！");
        OfficeDb odb = new OfficeDb();
        if (odb.hasOfficeOfType(id)) {
           // String info = SkinUtil.LoadString(request, "res.module.book", "warn_type_del_hasbook");
             throw new ErrMsgException("此类别下已存货品，请删除货品后才能删除此分类！");
        }
        return OfficeTypeDb.del();
        //}
    }
}
