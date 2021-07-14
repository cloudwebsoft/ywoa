package com.redmoon.sns.app.t;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.SequenceMgr;
import com.redmoon.forum.person.UserPropDb;
import com.redmoon.sns.ActionDb;
import com.redmoon.sns.app.base.AppAction;
import com.redmoon.sns.app.base.IAction;
import com.redmoon.sns.app.blog.BlogUnit;
import com.redmoon.sns.ui.SkinMgr;
import com.redmoon.t.TDb;
import com.redmoon.t.TMsgDb;
import com.redmoon.t.TMsgMgr;

public class TAction extends AppAction {

	public String getTitle(ActionDb ad) {
		return "发布了微博";
	}
	
	public boolean log(String ownerId, String userName, int action, long actionId) {
		ActionDb ad = new ActionDb();
		long id = SequenceMgr.nextID(SequenceMgr.SNS_APP_ACTION);
		boolean re = false;
		try {
			re = ad.create(new JdbcTemplate(), new Object[]{new Long(id), ownerId, userName,TUnit.code,new Integer(action),new Long(actionId),new java.util.Date()});
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		// 更新sq_user_prop中的用户最后发布的action，以便于在用户中心中对action进行排序
		UserPropDb upd = new UserPropDb();
		upd = upd.getUserPropDb(userName);
		upd.set("app_action_id", new Long(id));
		try {
			upd.save();
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		
		return re;
	}
	
	public String getIcon(HttpServletRequest request, ActionDb ad) {
		return request.getContextPath() + "/user/" + SkinMgr.getSkinPath(request) + "/images/app_t.gif";
	}	
	
	public String getAbstract(HttpServletRequest request, ActionDb ad) {
		long id = ad.getLong("action_id");
		TMsgDb tmd = new TMsgDb();
		tmd = tmd.getTMsgDb(id);
		if (tmd!=null)
			return TMsgMgr.render(request, tmd);
		else
			return "Msg id=" + id + " is not exist.";
	}

	public String getOperate(HttpServletRequest request, ActionDb ad) {
		long id = ad.getLong("action_id");
		TMsgDb tmd = new TMsgDb();
		tmd = tmd.getTMsgDb(id);
		if (tmd==null)
			return "Msg id=" + id + " is not exist.";
		StringBuffer buf = new StringBuffer();
		String rootPath = Global.getRootPath();
		String isJsWrited = (String) request.getAttribute("isJsWrited_" + TUnit.code);
		if (isJsWrited == null) {
			buf.append("<script src='" + rootPath + "/user/t_action.js'></script>");
			request.setAttribute("isJsWrited_" + TUnit.code, "true");
		}
		// buf.append("<a href='javascript:;' onclick=\"tReply('" + ad.getLong("id") + "','" + ad.getLong("action_id") + "')\">回复</a>");
		// buf.append("<a href='" + request.getContextPath() + "/user/t.jsp?tid=" + tmd.getLong("t_id") + "#t" + id + "' target='_blank'>回复</a>");		
		buf.append("<a href='" + request.getContextPath() + "/user/t_msg.jsp?id=" + tmd.getLong("id") + "' target='_blank'>回复</a>");		
		return buf.toString();
	}

}
