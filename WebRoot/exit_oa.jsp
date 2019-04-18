<%@ page contentType="text/html;charset=utf-8"
import = "cn.js.fan.util.ErrMsgException"
import = "com.redmoon.forum.security.*"
%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.util.*"%>
<html>
<head>
<title>退出</title>
<LINK href="common.css" type=text/css rel=stylesheet>
<%@ include file="inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<META HTTP-EQUIV="pragma" CONTENT="no-cache"> 
<META HTTP-EQUIV="Cache-Control" CONTENT= "no-cache, must-revalidate"> 
<META HTTP-EQUIV="expires" CONTENT= "Wed, 26 Feb 1997 08:21:57 GMT">
<link rel="stylesheet" href="../common.css" type="text/css">
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<jsp:useBean id="privilegeForum" scope="page" class="com.redmoon.forum.Privilege" />
<%
String skincode = ParamUtil.get(request, "skincode");
privilege.logout(request, response);
privilegeForum.logout(request, response);
String iskicked = request.getParameter("iskicked");
if (iskicked!=null)
	out.print(fchar.p_center("您已被踢出讨论室！"));
else
	out.print(fchar.Alert("您已安全退出"));

com.redmoon.oa.integration.cwbbs.CWBBSConfig ccfg = com.redmoon.oa.integration.cwbbs.CWBBSConfig.getInstance();
if (ccfg.getBooleanProperty("isUse")) {
	PassportRemoteUtil pru = new PassportRemoteUtil();
	pru.remoteSuperLogout(request, response, ccfg.getProperty("url"), ccfg.getProperty("key"), cn.js.fan.web.Global.getFullRootPath(request) + "/index.jsp");
}
else {
	String redirectUrl = ParamUtil.get(request, "redirectUrl");
	if ("".equals(redirectUrl)) {
		redirectUrl = "index.jsp?skincode="+skincode;
	}
	response.sendRedirect(redirectUrl);
}
%>
</body>
</body>
</html>


