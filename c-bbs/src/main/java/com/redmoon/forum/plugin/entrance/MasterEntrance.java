package com.redmoon.forum.plugin.entrance;

import com.redmoon.forum.plugin.base.IPluginEntrance;
import com.redmoon.forum.Privilege;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.BoardEntranceDb;
import cn.js.fan.web.SkinUtil;

public class MasterEntrance implements IPluginEntrance {
    public static String CODE = "master";

    public MasterEntrance() {
    }

    public boolean canEnter(HttpServletRequest request, String boardCode) throws ErrMsgException{
        Privilege pvg = new Privilege();
        if (pvg.isMasterLogin(request)) // 这样从后台登录的就可以进入
            return true;

        // 验证用户
        if (!pvg.isUserLogin(request)) {
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.entrance","err_entrance"));//对不起，只有版主才能进入！
        }

        if (pvg.isMasterLogin(request))
            return true;
        else {
            // 查询用户是否为管理员
            // MasterDb md = new MasterDb();
            // md = md.getMasterDb(pvg.getUser(request));
            // if (!md.isLoaded()) {
            // }
            throw new ErrMsgException("对不起，只有论坛管理员才能进入");
        }
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
