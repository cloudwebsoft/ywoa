package com.redmoon.forum.plugin.group;

import java.util.Vector;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;

public class GroupPrivilege implements IPluginPrivilege {
    static Logger logger = Logger.getLogger(GroupPrivilege.class.getName());

    public GroupPrivilege() {
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode, FileUpload fileUpload) throws ErrMsgException {
        // 检测本版是否含有此功能
        GroupUnit cu = new GroupUnit();
        if (!cu.isPluginBoard(boardCode))
            throw new ErrMsgException(GroupSkin.LoadString(request,
                    "addNewErrorBoardInvalid"));
        return true;
    }

    public boolean isOwner(HttpServletRequest request, long msgRootId) {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgRootId);
        String user = Privilege.getUser(request);
        if (user.equals(md.getName()))
            return true;
        else
            return false;
    }

    public boolean canAddReply(HttpServletRequest request, String boardCode, long msgRootId) throws ErrMsgException {
        // 检测本版是否含有此功能
        GroupUnit cu = new GroupUnit();
        if (!cu.isPluginBoard(boardCode))
            throw new ErrMsgException(GroupSkin.LoadString(request, "addNewErrorBoardInvalid"));
        return true;
    }

    public boolean canEdit(HttpServletRequest request, MsgDb md) throws ErrMsgException {
        GroupUnit cu = new GroupUnit();
        if (!cu.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(GroupSkin.LoadString(request, "addNewErrorBoardInvalid"));

        return true;
    }

    /**
     *
     * @param request HttpServletRequest
     * @param md MsgDb replyid所对应的贴子，也即根贴
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean canAddQuickReply(HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        return true;
    }

    public boolean canManage(HttpServletRequest request, long msgId) throws ErrMsgException {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);

        // 检测本版是否含有此功能
        GroupUnit au = new GroupUnit();
        if (!au.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(GroupSkin.LoadString(request,
                    "addNewErrorBoardInvalid"));

        Privilege privilege = new Privilege();
        if (privilege.canManage(request, msgId))
            return true;

        // 如果是楼主可以管理
        if (md.isBlog()) {
            long rootId = md.getRootid();
            md = md.getMsgDb(rootId);
            if (Privilege.getUser(request).equalsIgnoreCase(md.getName()))
                return true;
        }
        return false;
    }

    public static boolean isManager(HttpServletRequest request, long groupId) {
        return canUserDo(request, groupId, GroupUserDb.PRIV_ALL);
    }

    public static boolean isMember(HttpServletRequest request, long groupId) {
        return canUserDo(request, groupId, "enter");
    }

    public static boolean canUserDo(HttpServletRequest request, long groupId, String action) {
        if (Privilege.isMasterLogin(request))
            return true;
        String userName = Privilege.getUser(request);
        GroupDb ucd = new GroupDb();
        ucd = (GroupDb)ucd.getQObjectDb(new Long(groupId));
        // 团队博客创建者
        if (ucd.getString("creator").equals(userName))
            return true;
        GroupUserDb bu = new GroupUserDb();
        bu = bu.getGroupUserDb(groupId, userName);
        if (bu==null)
            return false;
        // 具有管理员权限
        if (bu.getString(GroupUserDb.PRIV_ALL).equals("1")) {
            return true;
        }
        if (action.equals("enter")) {
            if (bu==null || !bu.isLoaded()) {
                return false;
            }
            else {
                if (bu.getInt("check_status")==GroupUserDb.CHECK_STATUS_NOT)
                    return false;
                else
                    return true;
            }
        }
        return bu.getString(action).equals("1");
    }
    
    /**
     * 判断用户是否能访问圈内的贴子
     * @param request
     * @param md
     * @return
     */
    public static boolean canUserSee(HttpServletRequest request, MsgDb md) {
    	com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
    	if (cfg.getBooleanProperty("forum.isUseGroupThreadOnlyMemberSee")) {
	    	if (md.getboardcode().equals(GroupUnit.code)) {
	    		GroupThreadDb gtd = new GroupThreadDb();
	    		Vector v = gtd.getGroupThreadsOfThread(md.getRootid());
	    		if (v.size()>0) {
	    			gtd = (GroupThreadDb)v.elementAt(0);
	    			long groupId = gtd.getLong("group_id");
	    			GroupDb gd = new GroupDb();
	    			gd = (GroupDb) gd.getQObjectDb(new Long(groupId));
	    			if (gd != null) {
	    				if (gd.getInt("is_public")==0) {
	    					if (!com.redmoon.forum.plugin.group.GroupPrivilege.isMember(request, groupId)) {
	    						return false;			
	    					}
	    				}
	    				else
	    					return true;
	    			}
	    		}
	    	}
    	}
    	return true;
    }
}
