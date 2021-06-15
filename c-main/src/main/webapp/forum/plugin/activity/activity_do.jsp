<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.activity.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%
Privilege privilege = new Privilege();
if (!privilege.isUserLogin(request)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_not_login")));
	return;
}
long msgId = ParamUtil.getLong(request, "msgId");
String op = ParamUtil.get(request, "op");
String userName = privilege.getUser(request);
ActivityDb atd = new ActivityDb();
atd = atd.getActivityDb(msgId);
if (!atd.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该贴不是召集活动贴！"));
}

// 到期检查
if (DateUtil.compare(atd.getExpireDate(), new java.util.Date())==2) {
	out.print(SkinUtil.makeErrMsg(request, "该活动已过期，不能再进行相关操作！"));
	return;
}

UserLevelDb uld = new UserLevelDb();
uld = uld.getUserLevelDb(atd.getUserLevel());

UserMgr um = new UserMgr();
UserDb ud = um.getUser(userName);
if (ud.getUserLevelDb().getLevel()<uld.getLevel()) {
	out.print(SkinUtil.makeErrMsg(request, "您的级别不够，不能参加该活动！"));
	return;
}

String users = atd.getUsers().trim();
String[] ary = StrUtil.split(users, ",");

if (op.equals("attend")) {
	if (ary!=null) {
		// 检查用户是否已经加入了
		int len = ary.length;
		int userCount = atd.getUserCount();
		if (userCount!=-1) {
			if (len>=userCount) {
				out.print(SkinUtil.makeErrMsg(request, "加入人数已达到上限 " + userCount + "！加入失败！"));
				return;				
			}
		}
		for (int i=0; i<len; i++) {
			if (ary[i].equals(userName)) {
				out.print(SkinUtil.makeErrMsg(request, "您已经加入了该活动，无需再申请加入！"));
				return;
			}
		}
	}
	if (users.equals(""))
		users = userName;
	else {
		users += "," + userName;
	}
	atd.setUsers(users);
	if (atd.save()) {
		if (!atd.getMoneyCode().equals("")) {
			ScoreMgr sm = new ScoreMgr();
			ScoreUnit su = sm.getScoreUnit(atd.getMoneyCode());
			su.getScore().pay(su.getScore().SELLER_SYSTEM, userName, atd.getAttendMoneyCount());
			out.print(SkinUtil.makeInfo(request, "您已成功加入该活动！得到赠送的" + su.getName(request) + " " + atd.getAttendMoneyCount()));
		}
		else {
			out.print(SkinUtil.makeInfo(request, "您已成功加入该活动！"));
		}
		return;
	}
}
else if (op.equals("exit")) {
	boolean isFound = false;
	String u = "";
	if (ary!=null) {
		int len = ary.length;
		for (int i=0; i<len; i++) {
			if (!ary[i].equals(userName)) {
				if (u.equals(""))
					u = ary[i];
				else
					u += "," + ary[i];
			}
			else
				isFound = true;
		}
	}
	if (isFound) {
		atd.setUsers(u);
		if (atd.save()) {
			if (!atd.getMoneyCode().equals("")) {
				ScoreMgr sm = new ScoreMgr();
				ScoreUnit su = sm.getScoreUnit(atd.getMoneyCode());
				su.getScore().pay(su.getScore().SELLER_SYSTEM, userName, -atd.getExitMoneyCount());
				out.print(SkinUtil.makeInfo(request, "您已退出该活动！被扣" + su.getName(request) + " " + atd.getExitMoneyCount()));
			}
			else
				out.print(SkinUtil.makeInfo(request, "您已退出该活动！"));
			return;
		}
	}
	else {
		out.print(SkinUtil.makeInfo(request, "您未加入该活动！"));
	}
}
%>