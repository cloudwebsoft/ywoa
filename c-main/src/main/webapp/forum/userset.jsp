<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.web.SkinUtil"
import = "cn.js.fan.util.ParamUtil"
import = "cn.js.fan.util.ErrMsgException"
%>
<%@ page import="java.util.Calendar" %>
<html>
<head>
<title>Change Skin</title>
<%@ include file="../inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="../common.css" type="text/css">
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="us" scope="page" class="com.redmoon.forum.person.UserSet" />
<%
String op = ParamUtil.get(request, "op");
if (op.equals("setSkin")) {
	String skinCode = ParamUtil.get(request, "skinCode");
	us.setSkin(request, response, skinCode);
	// out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
}
response.sendRedirect("index.jsp");
%>
</body>
</html>


