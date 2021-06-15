<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.Properties" %>
<%@ page import="com.cloudweb.oa.service.LoginService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>登录处理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="inc/common.js"></script>
</head>
<body>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
String op = ParamUtil.get(request, "op");

String userName = ParamUtil.get(request, "userName");
String pwdBase64 = ParamUtil.get(request, "pwd");

sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();  
byte[] bt = decoder.decodeBuffer( pwdBase64 );  
String pwd = new String(bt);

System.out.println(getClass() + " " + pwd);

String pwdMD5 = cn.js.fan.security.SecurityUtil.MD5(pwd);

boolean re = privilege.Authenticate(userName, pwdMD5);
if (!re) {
	out.print(SkinUtil.makeErrMsg(request, "验证非法！"));
	return;
}

privilege.doLoginSession(request, userName, pwdMD5);

// action = cn.js.fan.security.ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb", action);
UserDb user = new UserDb();
user = user.getUserDb(userName);

if (!user.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "用户不存在！"));
	return;
}

if (!user.isValid()) {
	out.print(SkinUtil.makeErrMsg(request, "用户非法！"));
	return;
}

com.redmoon.oa.Config conf = new com.redmoon.oa.Config();
if (!conf.getBooleanProperty("systemIsOpen")) {
	response.sendRedirect("../index.jsp?op=" + userName);
	return;
}

LoginService loginService = SpringUtil.getBean(LoginService.class);
String url = loginService.getUIModePage("");

if (op.equals("")) {
	op = "login";
}

if (op.equals("login")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	// response.sendRedirect("../oa.jsp?mainTitle=" + StrUtil.UrlEncode("查看流程") + "&mainPage=" + mainPage);	
	response.sendRedirect("../" + url);	
}
else if (op.equals("listFlowDoingOrReturn")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("待办流程") + "&mainPage=flow/flow_list.jsp?displayMode=1");
}
else if (op.equals("initFlow")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("发起流程") + "&mainPage=flow_initiate1.jsp");
}
else if (op.equals("plan")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("日程安排") + "&mainPage=plan/plan_month.jsp");
}
else if (op.equals("fileark")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("文件柜") + "&mainPage=fileark/fileark_frame.jsp");
}
else if (op.equals("message")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("内部消息") + "&mainPage=message_oa/message_ext/message.jsp");
}
%>
</body>
</head>