package com.redmoon.forum.plugin.entrance;

import com.redmoon.forum.plugin.base.IPluginEntrance;
import com.redmoon.forum.Privilege;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.BoardManagerDb;
import com.redmoon.forum.BoardEntranceDb;
import cn.js.fan.web.SkinUtil;

public class ManagerEntrance implements IPluginEntrance {
    public static String CODE = "manager";

    public ManagerEntrance() {
    }

    public boolean canEnter(HttpServletRequest request, String boardCode) throws ErrMsgException{
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));//请先登陆！
        }
        if (pvg.isMasterLogin(request))
            return true;
        BoardManagerDb bm = new BoardManagerDb();
        boolean re = bm.isUserManager(pvg.getUser(request));
        if (!re) {
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.entrance","err_entrance"));//对不起，只有版主才能进入！
        }
        return true;
    }

    public boolean isPluginBoard(String boardCode) {
        BoardEntranceDb be = new BoardEntranceDb();
        be = be.getBoardEntranceDb(boardCode, CODE);
        if (be.isLoaded())
            return true;
        else
            return false;
    }

    public boolean canAddReply(HttpServletRequest request, String boardCode, long rootid) {
        return true;
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode) {
        return true;
    }
    
    public boolean canVote(HttpServletRequest request, String boardCode) throws ErrMsgException {
    	return true;
    }
}
