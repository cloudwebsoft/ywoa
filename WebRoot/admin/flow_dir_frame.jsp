<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>管理目录</title>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
String root_code = ParamUtil.get(request, "root_code");
%>
<frameset rows="*,150" cols="*">
  <frame src="flow_dir_top.jsp?root_code=<%=StrUtil.UrlEncode(root_code)%>" name="dirmainFrame">
  <frame src="flow_dir_bottom.jsp" name="dirbottomFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
