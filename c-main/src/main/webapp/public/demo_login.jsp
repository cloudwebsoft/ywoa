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
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>登录处理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="inc/common.js"></script>
</head>
<body>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
String module = ParamUtil.get(request, "module");                //具体哪个模块
String op = ParamUtil.get(request, "op");             	//操作
String userName = ParamUtil.get(request, "userName");

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
else
	url = "main.jsp";


if (op.equals("login")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	// response.sendRedirect("../oa.jsp?mainTitle=" + StrUtil.UrlEncode("查看流程") + "&mainPage=" + mainPage);	
	response.sendRedirect("../" + url);	
}
else if (op.equals("initFlow")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("发起流程") + "&mainPage=flow_initiate1_do.jsp?typeCode="+module+"&projectId=-1&title=&level=0&curTime=["+DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss")+"]");
}
else if (op.equals("workplan")) {        
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("工作计划") + "&mainPage=workplan/workplan_list.jsp");   
}
else if (op.equals("netdisk")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	//response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("网络硬盘") + "&mainPage=netdisk/dir_list.jsp?op=editarticle&dir_code="+userName+"&mode=");
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("网络硬盘") + "&mainPage=netdisk/netdisk_frame.jsp");
}
else if (op.equals("workreport")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("工作报告") + "&mainPage=mywork/mywork.jsp");
}
else if (op.equals("deptwork")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("部门工作") + "&mainPage=admin/admin_dept_frame.jsp");
}
else if (op.equals("notice")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("通知公告") + "&mainPage=notice/notice_list.jsp");
}
else if (op.equals("fileark")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("文件柜") + "&mainPage=fileark/fileark_frame.jsp");
}
else if (op.equals("sales")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("销售管理") + "&mainPage=sales/sales_desktop.jsp");
}
else if (op.equals("location")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("签到管理") + "&mainPage=map/location_list.jsp");
}
else if (op.equals("formQuery")) {
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("报表统计") + "&mainPage=visual/module_list.jsp?code="+module+"&formCode="+module);
}
else if (op.equals("caiwu_report")){
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("财务汇总表") + "&mainPage=reportJsp/showReport.jsp?raq=财务报表");
}
else if (op.equals("jb_report")){
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("加班申请表") + "&mainPage=reportJsp/showReport.jsp?raq=加班申请表");
}
else if (op.equals("flow")){
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("@流程") + "&mainPage=flow_dispose_light.jsp?myActionId=122");
}
%>
</body>
</head>