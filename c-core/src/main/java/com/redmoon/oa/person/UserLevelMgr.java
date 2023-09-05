package com.redmoon.oa.person;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.redmoon.oa.pvg.Privilege;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.web.SkinUtil;

public class UserLevelMgr {
    String connname;

    public UserLevelMgr() {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).info("Priv:connname is empty.");
        }
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserPrivValid(request, "admin"))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));
        int level = ParamUtil.getInt(request, "level", -1);
        if (level==-1)
        	throw new ErrMsgException("在线时长需为整数!");
        String desc = ParamUtil.get(request, "desc");
        String levelPicPath = ParamUtil.get(request, "levelPicPath");
        if (desc.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.setup.UserLevelMgr", "err_need_desc"));

        UserLevelDb uld = new UserLevelDb();
        uld.setLevel(level);
        uld.setDesc(desc);
        uld.setLevelPicPath(levelPicPath);
        return uld.create();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserPrivValid(request, "admin"))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));
        int level = ParamUtil.getInt(request, "level", -1);
        if (level==-1)
        	throw new ErrMsgException("在线时长需为整数!");
        int newLevel = ParamUtil.getInt(request, "newLevel", -1);
        if (newLevel==-1)
        	throw new ErrMsgException("在线时长需为整数!");        
        String desc = ParamUtil.get(request, "desc");
        String levelPicPath = ParamUtil.get(request, "levelPicPath");
        if (desc.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.setup.UserLevelMgr", "err_need_desc"));

        UserLevelDb uld = new UserLevelDb();
        uld = uld.getUserLevelDb(level);
        uld.setNewLevel(newLevel);
        uld.setDesc(desc);
        uld.setLevelPicPath(levelPicPath);
        return uld.save();
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserPrivValid(request, "admin"))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));
        int level = ParamUtil.getInt(request, "level");
        UserLevelDb uld = new UserLevelDb();
        uld = uld.getUserLevelDb(level);
        return uld.del();
    }

}
