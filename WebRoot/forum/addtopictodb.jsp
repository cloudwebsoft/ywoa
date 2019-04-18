<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.ErrMsgException"
%>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.forum.person.UserSet"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="form" scope="page" class="cn.js.fan.security.Form" />
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
boolean isSuccess = false;
String privurl = "";

boolean cansubmit = false;
com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
int interval = cfg.getIntProperty("forum.addMsgInterval");
int maxtimespan = interval;
try {
	cansubmit = form.cansubmit(request, "addtopic", maxtimespan);// 防止重复刷新	
}
catch (ErrMsgException e) {
	out.println(StrUtil.Alert_Back(e.getMessage()));
	return;
}
%>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
String boardcode = "";
long id = -1;
if (cansubmit) {
	try {
		isSuccess = Topic.AddNew(application, request);
		privurl = Topic.getprivurl();
		boardcode = Topic.getCurBoardCode();
		id = Topic.getId();
	}
	catch (ErrMsgException e) {
		out.println(StrUtil.Alert_Back(e.getMessage()));
		// e.printStackTrace();
		return;
	}
}

String addFlag = ParamUtil.get(request, "addFlag");
if (Topic.isBlog()) {
	// 说明是从博客用户管理后台发表的
	if (addFlag.equals("blog")) {
		MsgDb md = Topic.getMsgDb(Topic.getId());
		if (md.getCheckStatus()==MsgDb.CHECK_STATUS_NOT) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "res.label.forum.addtopic", "need_check"), "../blog/user/listtopic.jsp?blogId=" + md.getBlogId()));			
		}
		else
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "../blog/user/listtopic.jsp?blogId=" + md.getBlogId()));	
		return;
	}
	// 是从论坛发贴时选择博客目录发表的
	if (boardcode.equals(Leaf.CODE_BLOG)) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "blog/showblog.jsp?rootid=" + id));	
		return;
	}
}

String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.forum.addtopic" key="addtopic"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<%if (addFlag.equals("")) {%>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%}%>
<%
if (isSuccess) {
%>
	<BR><ol><lt:Label key="info_op_success"/></ol>
<%
	if (addFlag.equals(""))
		out.println(SkinUtil.waitJump(request, "<a href='showtopic.jsp?rootid="+id+"'>" + SkinUtil.LoadString(request, "res.label.forum.addtopic", "jump_to_topic") + "</a>",3,"showtopic.jsp?rootid=" + id));
	else
		out.println(SkinUtil.waitJump(request, "<a href='showtopic.jsp?rootid="+id+"'>" + SkinUtil.LoadString(request, "res.label.forum.addtopic", "jump_to_topic") + "</a>",3,privurl));
	
	out.println("<ol><a href='"+privurl+"'>" + SkinUtil.LoadString(request, "info_back") + "</a></ol>");
	MsgDb md = Topic.getMsgDb(Topic.getId());
	if (md.getCheckStatus()==MsgDb.CHECK_STATUS_NOT) {
		out.println("<ol>" + SkinUtil.LoadString(request, "res.label.forum.addtopic", "need_check") + "</ol>");
	}
}%>
<%if (addFlag.equals("")) {%>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
<%}%>
</body>
</html>


