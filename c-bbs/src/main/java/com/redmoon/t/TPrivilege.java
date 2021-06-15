package com.redmoon.t;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.Privilege;

public class TPrivilege extends Privilege {
	
	public static boolean canManage(HttpServletRequest request, TDb tdb) {
    	// 检查用户是否为管理员
    	// if (Privilege.isMasterLogin(request))
    	//	return true;
    	// 检查用户是否为本人
    	// System.out.println(TPrivilege.class.getName() + " Privilege.getUser=" + Privilege.getUser(request) + " " + tdb.getString("owner"));
    	if (Privilege.getUser(request).equals(tdb.getString("owner")))
    		return true;
    	
    	// 如果用户为班级型，则检查用户是否具有班级网站的管理权限
    	
    	return false;
	}
	
	public static boolean canDel(HttpServletRequest request, TMsgDb tmd) {
    	// 检查用户是否为管理员
    	if (Privilege.isMasterLogin(request))
    		return true;
    	// 检查用户是否为发贴者本人
    	// System.out.println(TPrivilege.class.getName() + " Privilege.getUser=" + Privilege.getUser(request) + " " + tdb.getString("owner"));
    	if (Privilege.getUser(request).equals(tmd.getString("user_name")))
    		return true;
    	
    	// 如果是回复贴，且删除者为被回复贴的博主
    	if (tmd.getLong("reply_id")!=0) {
    		TMsgDb reTmd = tmd.getTMsgDb(tmd.getLong("reply_id"));
    		if (reTmd!=null) {
	    		TDb tdb = new TDb();
	    		tdb = tdb.getTDb(reTmd.getLong("t_id"));
	    		if (Privilege.getUser(request).equals(tdb.getString("owner")))
	    			return true;
    		}
    	}
    	
    	return false;
	}	
}
