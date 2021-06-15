package com.redmoon.forum.plugin.entrance;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.person.MasterDb;
import com.redmoon.forum.plugin.base.IPluginEntrance;
import com.redmoon.forum.Privilege;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.BoardEntranceDb;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class VIPUserGroupEntrance implements IPluginEntrance {
    public static String CODE = "vipUserGroup";

    public VIPUserGroupEntrance() {
    }

    public boolean canEnter(HttpServletRequest request, String boardCode) throws ErrMsgException{
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));//请先登陆！
        }
        if (pvg.isMasterLogin(request))
            return true;
        // 查询用户是否为VIPUserGroup用户
        VIPUserGroupDb vud = new VIPUserGroupDb();
        UserDb ud = new UserDb();
        ud = ud.getUser(pvg.getUser(request));

        vud = vud.getVIPUserGroupDb(ud.getGroupCode());
        boolean isValid = false;
        // System.out.println("VIPUserEntrance.java " + vud.getBoards() + " " + boardCode);
        if (vud!=null && vud.isLoaded()) {
            if ((vud.getBoards()+",").indexOf(boardCode + ",")!=-1)
                isValid = true;
        }
        if (!isValid)
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));//您没有权限
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
