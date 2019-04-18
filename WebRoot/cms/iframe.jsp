<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.module.cms.*" %>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>iframe</title>
<link href="default.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body style="background-color:#FFFFFF">
<%
String url = ParamUtil.get(request, "url");
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();	

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "url", url, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

try {
	com.redmoon.oa.security.SecurityUtil.validateUrl(request, privilege, "url", url, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
%>
<iframe src="<%=url%>" frameborder="0" width="100%" height="100%" style="height:480px"></iframe>
</body>
</html>