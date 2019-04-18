<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.FormMgr"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import="com.redmoon.oa.worklog.WorkLogForModuleMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String myname = privilege.getUser( request );

String op = ParamUtil.get(request, "op");

String code = ParamUtil.get(request, "code");
if ("".equals(code)) {
	code = ParamUtil.get(request, "formCode");
}
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(code);
if (msd==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(code);
if (!mpd.canUserView(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = msd.getString("form_code");
if (formCode.equals("")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

int id = ParamUtil.getInt(request, "id");
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=fd.getName()%>查看</title>
<meta http-equiv="X-UA-Compatible" content="IE=Edge;chrome=IE8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script src="../inc/common.js"></script>
</head>
<frameset id="frm" cols="250,*" framespacing="3" frameborder="1">
	<noframes><body></body></noframes>
	<frame src="module_show_left.jsp?id=<%=id %>&code=<%=StrUtil.UrlEncode(code)%>" id="leftModuleFrame" name="leftModuleFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1" />
	<frame src="module_list.jsp?id=<%=id %>&isInFrame=true&code=<%=StrUtil.UrlEncode(code)%>" id="mainModuleFrame" name="mainModuleFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1" />
</frameset>
</html>
