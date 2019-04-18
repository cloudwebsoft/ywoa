package com.redmoon.oa.account;

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

public class AccountMgr {
    Logger logger = Logger.getLogger(AccountMgr.class.getName());

    public AccountMgr() {

    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

        boolean re = true;
        String errmsg = "";
        String name = ParamUtil.get(request, "name");
        if (name.equals(""))
            errmsg += "请输入正确的工号！\\n";
        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);

        AccountDb adb = getAccountDb(name);
        String userName = ParamUtil.get(request, "userName");
        if (userName.equals(adb.getUserName()))
            return true;

        adb.clearAccountUserName(userName);
        adb.setUserName(userName);
        re = adb.save();
        return re;
    }

    public AccountDb getAccountDb(String name) {
        AccountDb ad = new AccountDb();
        return ad.getAccountDb(name);
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

        boolean re = true;
        AccountDb adb = new AccountDb();
        String errmsg = "";
        String name = ParamUtil.get(request, "name");
        String userName = ParamUtil.get(request, "userName");

        if (name.equals(""))
            errmsg += "请输入正确的工号！\\n";
        if (adb.isExist(name)) {
            errmsg += "工号已存在，请重新输入！\\n";
        }
        if (userName.equals(""))
            errmsg += "请选择用户！";
        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);

        String unitCode = privilege.getUserUnitCode(request);

        adb.clearAccountUserName(userName);

        adb.setName(name);
        adb.setUserName(userName);
        adb.setUnitCode(unitCode);
        re = adb.create();

        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

        String name = ParamUtil.get(request, "name");
        AccountDb atd = getAccountDb(name);
        if (atd == null || !atd.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }
        return atd.del();
    }
}
