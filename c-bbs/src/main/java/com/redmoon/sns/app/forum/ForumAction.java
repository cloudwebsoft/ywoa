package com.redmoon.sns.app.forum;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.BoardRenderDb;
import com.redmoon.forum.Leaf;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.SequenceMgr;
import com.redmoon.forum.person.UserPropDb;
import com.redmoon.forum.plugin.base.IPluginRender;
import com.redmoon.sns.ActionDb;
import com.redmoon.sns.app.base.AppAction;
import com.redmoon.sns.app.blog.BlogUnit;

public class ForumAction extends AppAction {
	public static final int ACTION_TOPIC = 1;
	public static final int ACTION_REPLY = 2;

	public String getAbstract(HttpServletRequest request, ActionDb ad) {
		// TODO Auto-generated method stub
		int action = ad.getInt("app_action");
		if (action==ACTION_TOPIC || action==ACTION_REPLY) {
			long id = ad.getLong("action_id");
			MsgDb md = new MsgDb();
			md = md.getMsgDb(id);
	
			BoardRenderDb boardRender = new BoardRenderDb();
			boardRender = boardRender.getBoardRenderDb(md.getboardcode());
			IPluginRender render = boardRender.getRender();
			return StrUtil.getAbstract(request, render.RenderContent(request, md), 200);
		}
		return "";
	}

	public String getIcon(HttpServletRequest request, ActionDb ad) {
		// TODO Auto-generated method stub
		return "";
	}

	public String getOperate(HttpServletRequest request, ActionDb ad) {
		String str = "";
		Leaf lf = new Leaf();
		lf = lf.getLeaf(ad.getString("owner_id"));
		str = "<a href='" + request.getContextPath() + "/forum/listtopic.jsp?boardcode=" + StrUtil.UrlEncode(lf.getCode()) + "' target='_blank'>进入版块&nbsp;>>&nbsp;" + lf.getName() + "</a>";
		int action = ad.getInt("app_action");
		if (action==ACTION_TOPIC) {
			str += "&nbsp;&nbsp;&nbsp;&nbsp;<a href='" + request.getContextPath() + "/forum/showtopic.jsp?rootid=" + ad.getLong("action_id") + "' target='_blank'>查看</a>";
		}
		else {
			MsgDb md = new MsgDb();
			md = md.getMsgDb(ad.getLong("action_id"));
			str += "&nbsp;&nbsp;&nbsp;&nbsp;<a href='" + request.getContextPath() + "/forum/showtopic_tree.jsp?rootid=" + md.getRootid()	+ "&showid=" + ad.getLong("action_id") + "' target='_blank'>查看</a>";		
		}
		return str;
	}

	public String getTitle(ActionDb ad) {
		// TODO Auto-generated method stub
		MsgDb md = new MsgDb();
		md = md.getMsgDb(ad.getLong("action_id"));
		String pre = "发布了新贴：";
		if (!md.isRootMsg())
			pre = "发布了回贴：";
		return pre + md.getTitle();
	}

	public boolean log(String ownerId, String userName, int action, long actionId) {
		ActionDb ad = new ActionDb();
		long id = SequenceMgr.nextID(SequenceMgr.SNS_APP_ACTION);
		boolean re = false;
		try {
			re = ad.create(new JdbcTemplate(), new Object[]{new Long(id), ownerId, userName,ForumUnit.code,new Integer(action),new Long(actionId),new java.util.Date()});
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

}
