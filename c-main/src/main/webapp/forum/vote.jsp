<%@ page contentType="text/html;charset=utf-8"
import = "cn.js.fan.util.*"
import = "java.io.File"
import = "cn.js.fan.util.ErrMsgException"
%>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.forum.Leaf" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege" />
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
if (!privilege.isUserLogin(request)) {
	out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
	return;
}

UserPrivDb upd = new UserPrivDb();
upd = upd.getUserPrivDb(privilege.getUser(request));
if (!upd.getBoolean("vote")) {
	response.sendRedirect("../info.jsp?info= " + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

long voteid = ParamUtil.getLong(request, "voteid");
MsgDb msgDb = Topic.getMsgDb(voteid);

String boardcode = msgDb.getboardcode();
// 取得皮肤路径
Leaf lf = new Leaf();
lf = lf.getLeaf(boardcode);
if (lf==null) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
	return;
}
String skinPath = SkinMgr.getSkinPath(request);
%>
<%
boolean isSuccess = false;
String privurl = request.getParameter("privurl");
try {
	isSuccess = Topic.vote(request);
}
catch (ErrMsgException e) {
	out.println(StrUtil.Alert_Back(e.getMessage()));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.forum.vote" key="vote"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil" />
<%if (isSuccess) {%>
<ol><lt:Label res="res.label.forum.treasure" key="vote_success"/></ol>
<%
out.println(StrUtil.waitJump("<a href='"+privurl+"'>" + SkinUtil.LoadString(request, "res.label.forum.vote", "go_back") + "</a>",3,privurl));
}else{%>
<ol><lt:Label res="res.label.forum.treasure" key="vote_fail"/></ol>
<%}%>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>


