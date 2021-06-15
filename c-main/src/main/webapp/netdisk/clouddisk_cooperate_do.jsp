<%@ page contentType="text/html; charset=utf-8" %><%@ page import="java.util.*"%><%@ page import="com.redmoon.oa.netdisk.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import = "org.json.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.netdisk.DocumentMgr"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print("对不起，请先登录！");
	 return;
}
String op = ParamUtil.get(request, "op");
NetDiskCooperate cooperate = new NetDiskCooperate();
String dir_code = ParamUtil.get(request,"dir_code");
// 新建文件夹
if (op.equals("cooperateLog")) {
	out.print(cooperate.queryMyAttengCooperateLogByAjax(dir_code));
}

%>