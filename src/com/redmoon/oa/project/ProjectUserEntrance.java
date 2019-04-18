package com.redmoon.oa.project;


import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;

import com.redmoon.forum.BoardEntranceDb;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.plugin.base.IPluginEntrance;

/**
 * 该类已弃用
 * @author Administrator
 *
 */

public class ProjectUserEntrance implements IPluginEntrance {
    public static String CODE = "project";
    
	public boolean canAddNew(HttpServletRequest request, String boardCode)
			throws ErrMsgException {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean canAddReply(HttpServletRequest request, String boardCode, long rootid)
			throws ErrMsgException {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean canEnter(HttpServletRequest request, String boardCode)
			throws ErrMsgException {
        if (!Privilege.isUserLogin(request)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));//请先登陆！
        }
        if (Privilege.isMasterLogin(request))
            return true;
        
        // 判断是否为项目成员
        long projectId = StrUtil.toLong(boardCode.substring(ProjectChecker.CODE_PREFIX.length()));

        return ProjectMemberChecker.isUserExist(projectId, Privilege.getUser(request));
	}

	public boolean isPluginBoard(String boardCode) {
        BoardEntranceDb be = new BoardEntranceDb();
        be = be.getBoardEntranceDb(boardCode, CODE);
        if (be.isLoaded())
            return true;
        else
            return false;
	}

	public boolean canVote(HttpServletRequest arg0, String arg1)
			throws ErrMsgException {
		// TODO Auto-generated method stub
		return true;
	}

}
