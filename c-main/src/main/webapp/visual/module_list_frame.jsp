<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = privilege.getUser(request);
String code = ParamUtil.get(request, "code");
String mainFormCode = code;
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(code);
int is_workLog = msd.getInt("is_workLog");
if (!msd.getString("code").equals(msd.getString("form_code"))) {
	ModuleSetupDb msdMain = msd.getModuleSetupDb(msd.getString("form_code"));
	is_workLog = msdMain.getInt("is_workLog");
	mainFormCode = msd.getString("form_code");
}

if (msd==null) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
}

if (msd.getInt("is_use") != 1) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块未启用！"));
	return;
}

String formCode = msd.getString("form_code");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

ModulePrivDb mpd = new ModulePrivDb(code);
if (!mpd.canUserSee(privilege.getUser(request))) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=fd.getName()%>列表</title>
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script src="../inc/common.js"></script>
</head>
<frameset id="frm" cols="250,*" framespacing="3" frameborder="1">
	<noframes><body></body></noframes>
	<frame src="module_list_left.jsp?code=<%=StrUtil.UrlEncode(code)%>" id="leftModuleFrame" name="leftModuleFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1" />
	<frame src="moduleListPage.do?isInFrame=true&code=<%=StrUtil.UrlEncode(code)%>" id="mainModuleFrame" name="mainModuleFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1" />
</frameset>
</html>
