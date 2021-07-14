package com.redmoon.sns.app.base;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.MsgDb;
import com.redmoon.sns.ActionDb;
import com.redmoon.sns.ui.SkinMgr;

public interface IAction {
	public static final int ACTION_COMMON = 0;
	
	public String getTitle(ActionDb ad);
	
	/**
	 * 记录Action
	 * @param user_name
	 * @param appCode
	 * @param action
	 * @param actionId
	 * @param create_date
	 * @return
	 */
	public boolean log(String ownerId, String user_name, int action, long actionId);
	
	public String getIcon(HttpServletRequest request, ActionDb ad);
	
	public String getAbstract(HttpServletRequest request, ActionDb ad);
	
	public String getOperate(HttpServletRequest request, ActionDb ad);	
}
