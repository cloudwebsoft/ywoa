<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.db.*"
import = "cn.js.fan.util.*"
import = "cn.js.fan.web.Global"
import = "cn.js.fan.web.SkinUtil"
import = "com.redmoon.forum.person.*"
import = "com.redmoon.forum.*"
%>
<%@ page import = "java.net.URLEncoder"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<html>
<head>
<title>Modify my icon</title>
<%@ include file="inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%
try {
	UserMgr um = new UserMgr();
	boolean r = um.DIYMyface(application, request);
	if (r) {
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
%>
	<script>
	window.parent.location.reload();
	</script>
<%		
	}
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
}
%>
</body>
</html>


