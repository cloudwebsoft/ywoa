package com.redmoon.forum.plugin.entrance;

import com.redmoon.forum.plugin.base.IPluginEntrance;
import com.redmoon.forum.Privilege;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.BoardManagerDb;
import com.redmoon.forum.BoardEntranceDb;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.MsgDb;

public class ManagerReplyEntrance implements IPluginEntrance {
    public static String CODE = "managerReply";

    public ManagerReplyEntrance() {
    }

    public boolean canEnter(HttpServletRequest request, String boardCode) throws ErrMsgException{
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

    public boolean canAddReply(HttpServletRequest request, String boardCode, long rootid) throws ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));// 请先登陆！
        }
        if (pvg.isMasterLogin(request))
            return true;
        MsgDb md = new MsgDb();
        md = md.getMsgDb(rootid);
        boolean re = md.getName().equals(pvg.getUser(request));
        if (!re) {
            if (!pvg.isManager(request, boardCode))
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "res.forum.plugin.entrance", "err_reply_manager"));
        }
        return re;
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode) {
        return true;
    }
    
    public boolean canVote(HttpServletRequest request, String boardCode) throws ErrMsgException {
    	return true;
    }    
}
