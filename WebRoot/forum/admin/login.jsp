<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<html>
<head>
<title>Log in</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ include file="../inc/nocache.jsp"%>
<link rel="stylesheet" href="../../common.css" type="text/css">
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="login" scope="page" class="cn.js.fan.security.Login"/>
<jsp:useBean id="privilege" scope="page" class="cn.js.fan.module.pvg.Privilege"/>
<%
try {
	login.canlogin(request,"redmoon");
}
catch (ErrMsgException e)
{
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

boolean re = false;
String username = ParamUtil.get(request, "username");
String pwd = ParamUtil.get(request, "pwd");
try {
	re = privilege.login(request, username, pwd);
	login.afterlogin(request,re,"redmoon",true);
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (re) {
	response.sendRedirect("frame.jsp");
}
else {
	out.print(strutil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.admin.index", "login_fail")));
}
%>
</body>
</html>