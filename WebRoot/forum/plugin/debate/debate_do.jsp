<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.debate.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
Privilege privilege = new Privilege();
if (!privilege.isUserLogin(request)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_not_login")));
	return;
}
String userName = privilege.getUser(request);
long msgId = ParamUtil.getLong(request, "msgId");
String op = ParamUtil.get(request, "op");
DebateDb dd = new DebateDb();
dd = dd.getDebateDb(msgId);
if (!dd.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该贴不是辩论贴！"));
}

if (DateUtil.compare(dd.getBeginDate(), new java.util.Date())==1) {
	out.print(SkinUtil.makeErrMsg(request, "该辩论的开始时间为" + ForumSkin.formatDate(request, dd.getBeginDate()) + "，目前不能再进行相关操作！"));
	return;
}

// 到期检查
if (DateUtil.compare(dd.getEndDate(), new java.util.Date())==2) {
	if (!DateUtil.isSameDay(dd.getEndDate(), new java.util.Date())) {
		out.print(SkinUtil.makeErrMsg(request, "该辩论已过期，不能再进行相关操作！"));
		return;
	}
}

if (op.equals("vote_support")) {
	// 检查用户是否已投过票
	String voteUser1 = dd.getVoteUser1();
	String voteUser2 = dd.getVoteUser2();
	if (voteUser1.indexOf("|" + userName + "|")!=-1 || voteUser2.indexOf("|" + userName + "|")!=-1) {
		out.print(SkinUtil.makeErrMsg(request, "您已投过票了，不能再投！"));
		return;
	}
	dd.setVoteCount1(dd.getVoteCount1() + 1);
	if (voteUser1.equals(""))
		voteUser1 = "|" + userName + "|";
	else {
		voteUser1 += userName + "|";
	}
	dd.setVoteUser1(voteUser1);
	if (dd.save()) {
		out.print(SkinUtil.makeInfo(request, "操作成功！"));
	}
	else {
		out.print(SkinUtil.makeInfo(request, "操作失败！"));
	}
}

if (op.equals("vote_oppose")) {
	String voteUser1 = dd.getVoteUser1();
	String voteUser2 = dd.getVoteUser2();
	if (voteUser1.indexOf("|" + userName + "|")!=-1 || voteUser2.indexOf("|" + userName + "|")!=-1) {
		out.print(SkinUtil.makeErrMsg(request, "您已投过票了，不能再投！"));
		return;
	}
	dd.setVoteCount2(dd.getVoteCount2() + 1);
	if (voteUser2.equals(""))
		voteUser2 = "|" + userName + "|";
	else {
		voteUser2 += userName + "|";
	}
	dd.setVoteUser2(voteUser2);
	if (dd.save()) {
		out.print(SkinUtil.makeInfo(request, "操作成功！"));
	}
	else {
		out.print(SkinUtil.makeInfo(request, "操作失败！"));
	}
}
%>