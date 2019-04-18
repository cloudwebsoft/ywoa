<%@ page contentType="text/html;charset=utf-8"
import = "cn.js.fan.util.*"
import = "cn.js.fan.security.*"
import = "java.io.File"
import = "org.json.*"
%>
<%@page import="com.redmoon.oa.person.UserSetupDb"%>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("checkPwd")) {
	com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
	int minLen = scfg.getIntProperty("password.minLen");
	int maxLen = scfg.getIntProperty("password.maxLen");
	int strenth = scfg.getIntProperty("password.strenth");
		
	String pwd = ParamUtil.get(request, "pwd");
	PasswordUtil pu = new PasswordUtil();
	JSONObject json = new JSONObject();

	int ret = pu.check(pwd, minLen, maxLen, strenth);

	json.put("ret", ret);
	json.put("msg", pu.getResultDesc(request));
	
	out.print(json);
	return;
}
%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="userMgr" scope="page" class="com.redmoon.oa.person.UserMgr" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>更改用户信息</title>
<%@ include file="../inc/nocache.jsp"%>
<link rel="stylesheet" href="../common.css" type="text/css">
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

boolean isSuccess = false;
String name = ParamUtil.get(request, "name");
try {
	isSuccess = userMgr.modify(application, request);
}
catch (ErrMsgException e) {
	out.println(fchar.jAlert_Back(e.getMessage(),"提示"));
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}
if (isSuccess) {
	out.println(fchar.jAlert_Redirect("修改成功！","提示", "user_edit.jsp?name=" + StrUtil.UrlEncode(name)));
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}
%>
</body>
</html>


