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
<%@page import="com.redmoon.oa.netdisk.SideBarMgr"%>
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
String userName = "";

if (!op.equals("thirdpart")) {
	String authKey = ParamUtil.get(request, "authKey");
	
	//com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
	//authKey = ThreeDesUtil.decrypthexstr(cfg.get("key"), authKey);
	String deAuthKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(com.redmoon.clouddisk.socketServer.CloudDiskThread.OA_KEY, authKey);
	// System.out.println(getClass() + " op=" + op + " authKey=" + authKey);
	String[] ary = StrUtil.split(deAuthKey, "\\|");
	if (ary == null) {
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config.getInstance();
		if (cfg.getBooleanProperty("is_openSideHTML")) {
			SideBarMgr sbMgr = new SideBarMgr();
			sbMgr.syncAllFlag();
			out.print(SkinUtil.makeErrMsg(request, "正在同步您的侧边栏，请10秒后再试..."));
		} else {
			out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
		}
		return;
	}
	
	userName = ary[0];
} else {
	userName = ParamUtil.get(request, "username");
}

com.redmoon.oa.Config conf = new com.redmoon.oa.Config();
if (!conf.getBooleanProperty("systemIsOpen")) {
	response.sendRedirect("../index.jsp?op=" + userName);
	return;
}

// action = cn.js.fan.security.ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb", action);
UserDb user = new UserDb();
user = user.getUserDb(userName);

if (!op.equals("thirdpart")) {
	if (!user.isLoaded()) {
		out.print(SkinUtil.makeErrMsg(request, "用户不存在！"));
		return;
	}
	
	if (!user.isValid()) {
		out.print(SkinUtil.makeErrMsg(request, "用户非法！"));
		return;
	}
} else {
	String md5pwd = ParamUtil.get(request, "password");
	if (!user.isLoaded()) {
		user.create(userName, md5pwd);
		user = user.getUserDb(userName);
	}
	
	if (!user.isValid()) {
		out.print(SkinUtil.makeErrMsg(request, "用户非法！"));
		return;
	}
}
	
// System.out.println(getClass() + " op=" + op);

//UserSetupDb usd = new UserSetupDb();
// 注意不能用name作为参数，因为可能是用工号登录的
// usd = usd.getUserSetupDb(name);
//usd = usd.getUserSetupDb(userName);
//String url;
//if (usd.getUiMode()==UserSetupDb.UI_MODE_PROFESSION)
//	url = "oa.jsp";
//else if (usd.getUiMode()==UserSetupDb.UI_MODE_FLOWERINESS)
//	url = "mydesktop.jsp";
//else
//	url = "main.jsp";

// reverseRTXLogin=false拒绝腾讯通重复登陆
String url = "oa.jsp?isClouddisk=true";

if (op.equals("login")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	// response.sendRedirect("../oa.jsp?mainTitle=" + StrUtil.UrlEncode("查看流程") + "&mainPage=" + mainPage);	
	response.sendRedirect("../" + url);	
} else if (op.equals("doing")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("待办流程") + "&mainPage=flow/flow_list.jsp?displayMode=1");
} else if (op.equals("start")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("发起流程") + "&mainPage=flow_initiate1.jsp");
} else if (op.equals("notice")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("通知") + "&mainPage=notice/notice_list.jsp?isDeptNotice=0|op=|cond=title|what=");
} else if (op.equals("schedule")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("日程安排") + "&mainPage=plan/plan.jsp");
} else if (op.equals("fileark")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("文件柜") + "&mainPage=fileark/fileark_frame.jsp");
} else if (op.equals("plan")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("工作计划") + "&mainPage=workplan/workplan_list.jsp");
} else if (op.equals("worklog")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("工作记事") + "&mainPage=mywork/mywork.jsp?userName=" + StrUtil.UrlEncode(user.getName()));
} else if (op.equals("message")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode("短消息") + "&mainPage=message_oa/message_frame.jsp");
} else if (op.equals("myclouddisk")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../netdisk/clouddisk.jsp?page_no=1");
} else if (op.equals("myshare")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../netdisk/clouddisk.jsp?page_no=2");
} else if (op.equals("myrecycle")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../netdisk/clouddisk.jsp?page_no=4");
} else if (op.equals("sidebarhtml")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../netdisk/clouddisk_sidebar.jsp");
} else if (op.equals("sidebarMsg")) {
	int id = ParamUtil.getInt(request, "id", 0);
	MessageDb mDb = new MessageDb(id);
	if (mDb == null || !mDb.loaded) {
		out.print(SkinUtil.makeErrMsg(request, "该内部消息不存在！"));
	}
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode(mDb.getTitle()) + "&mainPage=message_oa/message_ext/sys_showmsg.jsp?id=" + id);
} else if (op.equals("sidebarFlow")){
	long id = ParamUtil.getLong(request, "id", 0);
	MyActionDb myDb = new MyActionDb(id);
	WorkflowDb wfDb = new WorkflowDb((int) myDb.getFlowId());
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode(wfDb.getTitle()) + "&mainPage=flow_dispose.jsp?myActionId=" + id);
} else if (op.equals("sidebarLink")){
	String link = ParamUtil.get(request, "link");
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	if (link.startsWith("netdisk/clouddisk.jsp") || link.startsWith("oa.jsp")) {
		response.sendRedirect("../" + link);
	} else {
		String title = ParamUtil.get(request,"title");
		response.sendRedirect("../" + url + "&mainTitle=" + StrUtil.UrlEncode(title) + "&mainPage="+link);
	}
} else if (op.equals("sidebarCustomer")) {
	String link = ParamUtil.get(request, "link");
	if (!link.startsWith("http://") && !link.startsWith("https://")) {
		link = "http://" + link;
	}
	response.sendRedirect(link);
} else if (op.equals("newsidebar")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../netdisk/clouddisk_sidebar_static.jsp");
} else if (op.equals("thirdpart")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../netdisk/clouddisk.jsp?page_no=1");
}
%>
</body>
</head>