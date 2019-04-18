<%@ page contentType="text/html;charset=utf-8"
import = "cn.js.fan.web.*"
import = "cn.js.fan.util.ErrMsgException"
import = "cn.js.fan.util.*"
import = "com.redmoon.forum.ucenter.*"
%>
<%@ page import="org.jdom.Element"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<html>
<head>
<title><lt:Label res="res.label.myinfo" key="myinfo"/> - <%=Global.AppName%></title>
<%@ include file="inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
<style type="text/css">
<!--
body {
	margin-top: 0px;
}
-->
</style></head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice" />
<%
boolean re = false;
try {
	re = userservice.editmyinfo(request,response);
}
catch (ErrMsgException e) {
	out.println(StrUtil.Alert_Back(com.cloudwebsoft.framework.security.AntiXSS.clean(e.getMessage())));
	return;
}
if (re)
	out.println(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "myinfo.jsp?userName=" + StrUtil.UrlEncode(ParamUtil.get(request, "RegName"))));
else
	out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
%>
</body>
</html>


