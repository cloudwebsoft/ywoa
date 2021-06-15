<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.Properties" %>
<%@page import="com.redmoon.oa.message.MessageDb"%>
<%@page import="com.redmoon.oa.android.CloudConfig"%>
<%@page import="com.redmoon.oa.pvg.Privilege"%>
<%@page import="cn.js.fan.security.ThreeDesUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="textml; charset=utf-8">
<title>登录处理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="inc/common.js"></script>
</head>
<body>
<%
String skey = ParamUtil.get(request,"skey");
String userName = "";
String op = "";
String id = "";
UserDb user = null;

if (skey.equals("")) {
	userName = ParamUtil.get(request, "userName");
	String pwd = ParamUtil.get(request, "pwd");
	user = new UserDb(userName);
	if (user == null || !user.isLoaded() || !user.isValid()) {
		out.print(SkinUtil.makeErrMsg(request, "用户非法！"));
		return;
	}
	if (!user.getPwdMD5().toLowerCase().equals(pwd)) {
		out.print(SkinUtil.makeErrMsg(request, "密码错误！"));
		return;
	}
	op = ParamUtil.get(request, "op");
	if (op.equals("")) {
		op = "login";
	}
}

if (op.equals("")) {
CloudConfig cfg = CloudConfig.getInstance();
skey = ThreeDesUtil.decrypthexstr(cfg.getProperty("key"), skey);
String[] ary = skey.split("&");
if (ary.length != 3) {
	out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
	return;
}

String[] ary1 = ary[0].split("=");
if (ary1.length != 2) {
	out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
	return;
}
if (!ary1[0].equals("op")) {
	out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
	return;
}
	op = ary1[1];
	
	String[] ary2 = ary[1].split("=");
	if (ary2.length != 2) {
		out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
		return;
	}
	if (!ary2[0].equals("skey")) {
		out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
		return;
	}
	
	skey = ary2[1];
	String[] ary23 = skey.split("\\|");
	if (ary23.length != 3) {
		out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
		return;
	}
	userName = ary23[0];
	
	if (userName.equals("")) {
		out.print(SkinUtil.makeErrMsg(request, "用户非法！"));
		return;
	}
	
	String[] ary3 = ary[2].split("=");
	if (ary3.length != 2) {
		out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
		return;
	}
	if (!ary3[0].equals("id")) {
		out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
		return;
	}
	
	user = new UserDb(userName);
	if (user == null || !user.isLoaded() || !user.isValid()) {
		out.print(SkinUtil.makeErrMsg(request, "用户非法！"));
		return;
	}
	id = ary3[1];
}

String url = "oa.jsp?isTigase=true";
Privilege privilege = new Privilege();

if (op.equals("login")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	// response.sendRedirect("../oa.jsp?mainTitle=" + StrUtil.UrlEncode("查看流程") + "&mainPage=" + mainPage);	
	response.sendRedirect("../" + url);	
} else if (op.equals("sysmsg")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("消息") + "&mainPage=message_oa/message_ext/sys_showmsg.jsp?id=" + id);
} else if (op.equals("doing")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("待办流程") + "&mainPage=flow/flow_list.jsp?displayMode=1");
} else if (op.equals("notice")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("通知") + "&mainPage=notice/notice_list.jsp?isDeptNotice=0|op=|cond=title|what=");
}
%>
</body>
</head>