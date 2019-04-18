<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.Properties" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>登录处理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="inc/common.js"></script>
</head>
<body>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
String op = ParamUtil.get(request, "op");

String authKey = ParamUtil.get(request, "authKey");

com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
authKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(cfg.get("key"), authKey);
// System.out.println(getClass() + " op=" + op + " authKey=" + authKey);
String[] ary = StrUtil.split(authKey, "\\|");
if (ary==null) {
	out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
	return;
}

String loginTime = "";
if (ary.length>1) {
	loginTime = ary[1];
	Date d = DateUtil.parse(loginTime, "yyyy-MM-dd HH:mm:ss");
	// 3分钟后超时
	if (DateUtil.datediffMinute(new Date(), d)>3) {
		out.print(SkinUtil.makeErrMsg(request, "操作超时！"));
		return;
	}
}

String userName = ary[0];
// action = cn.js.fan.security.ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb", action);
UserDb user = new UserDb();
user = user.getUserDb(userName);

if (!user.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "用户 " + userName + " 不存在！"));
	return;
}

if (!user.isValid()) {
	out.print(SkinUtil.makeErrMsg(request, "用户" + userName + " 非法！"));
	return;
}

// System.out.println(getClass() + " op=" + op);

UserSetupDb usd = new UserSetupDb();
// 注意不能用name作为参数，因为可能是用工号登录的
// usd = usd.getUserSetupDb(name);
usd = usd.getUserSetupDb(userName);
String url;
if (usd.getUiMode()==UserSetupDb.UI_MODE_PROFESSION)
	url = "oa.jsp";
else if (usd.getUiMode()==UserSetupDb.UI_MODE_FLOWERINESS)
	url = "mydesktop.jsp";
else if (usd.getUiMode() == UserSetupDb.UI_MODE_LTE) {
	url = "lte/index.jsp";
}	
else
	url = "oa_main.jsp";


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