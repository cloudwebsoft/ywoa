package com.redmoon.forum.setup;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.Privilege;

public class UserLevelMgr {
    String connname;
    Logger logger = Logger.getLogger(UserLevelMgr.class.getName());

    public UserLevelMgr() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Priv:connname is empty.");
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isMasterLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));
        int level = ParamUtil.getInt(request, "level");
        String desc = ParamUtil.get(request, "desc");
        String levelPicPath = ParamUtil.get(request, "levelPicPath");
        if (desc.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.setup.UserLevelMgr", "err_need_desc"));
        String groupCode = ParamUtil.get(request, "groupCode");
        UserLevelDb uld = new UserLevelDb();
        uld.setLevel(level);
        uld.setDesc(desc);
        uld.setLevelPicPath(levelPicPath);
        uld.setGroupCode(groupCode);
        return uld.create();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isMasterLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));
        int level = ParamUtil.getInt(request, "level");
        int newLevel = ParamUtil.getInt(request, "newLevel");
        String desc = ParamUtil.get(request, "desc");
        String levelPicPath = ParamUtil.get(request, "levelPicPath");
        if (desc.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.setup.UserLevelMgr", "err_need_desc"));
        String groupCode = ParamUtil.get(request, "groupCode");

        UserLevelDb uld = new UserLevelDb();
        uld = uld.getUserLevelDb(level);
        uld.setNewLevel(newLevel);
        uld.setDesc(desc);
        uld.setLevelPicPath(levelPicPath);
        uld.setGroupCode(groupCode);
        return uld.save();
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isMasterLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));
        int level = ParamUtil.getInt(request, "level");
        UserLevelDb uld = new UserLevelDb();
        uld = uld.getUserLevelDb(level);
        return uld.del();
    }

}
