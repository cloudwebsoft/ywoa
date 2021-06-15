<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.ErrMsgException"
import = "cn.js.fan.util.ParamUtil"
import="com.redmoon.forum.*"
%>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.forum.person.UserSet"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="form" scope="page" class="cn.js.fan.security.Form" />
<%
boolean cansubmit = false;
com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
int interval = cfg.getIntProperty("forum.addMsgInterval");
int maxtimespan = interval;
try {
	cansubmit = form.cansubmit(request,"addtopic", maxtimespan);// 防止重复刷新	
}
catch (ErrMsgException e) {
	out.println(StrUtil.Alert_Back(e.getMessage()));
	return;
}

boolean isSuccess = false;
String privurl = "", boardcode="";
MsgDb md = new MsgDb();
MsgDb replyMsgDb = null;
try {
	MsgMgr msgMgr = new MsgMgr();
	privurl = request.getParameter("privurl");
	boardcode = request.getParameter("boardcode");
	long replyid = ParamUtil.getLong(request, "replyid");
	md = md.getMsgDb(replyid);
	isSuccess = msgMgr.AddQuickReply(application, request);
	replyMsgDb = msgMgr.getMsgDb(msgMgr.getId());
}
catch (ErrMsgException e) {
	out.println(StrUtil.Alert_Back(e.getMessage()));
	return;
}
%>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.forum.addreply" key="quick_reply"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<%if (!md.isBlog()) {%>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<%}

if (isSuccess) {
	if (!md.isBlog()) {
		%>
		<ol><lt:Label key="info_op_success"/></ol>
		<%
		out.println(StrUtil.waitJump("<a href='"+privurl+"'>" + SkinUtil.LoadString(request, "res.label.forum.addreply", "back_to_priv") + "</a>",3,privurl));
		%>
		<ol><a href="listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(boardcode)%>"><lt:Label res="res.label.forum.addreply" key="back_to_cur_board"/></a></ol>
		<%
		if (replyMsgDb.getCheckStatus()==MsgDb.CHECK_STATUS_NOT) {
			out.println("<ol>" + SkinUtil.LoadString(request, "res.label.forum.addtopic", "need_check") + "</ol>");
		}
		%>
	<%}else{
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), privurl));
	}
}
if (!md.isBlog()) {%>
<br /><br />
<br />
<br />
<br /><br />
<br /><br />
</div><%@ include file="inc/footer.jsp"%></div>
<%}%>
</body>
</html>


