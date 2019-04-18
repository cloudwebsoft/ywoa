<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.util.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>网络硬盘主窗口</title>
</head>
<style>
body { background:#fcfcfc}
</style>
<body>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<span style="color:#666666">请点击左侧的共享文件夹</span>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("showDirShare"))
	return;
String mode = ParamUtil.get(request, "mode");
String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals(""))
	dir_code = privilege.getUser(request);
response.sendRedirect("dir_list.jsp?op=editarticle&dir_code=" +
                      StrUtil.UrlEncode(privilege.getUser(request)) + "&mode=" + mode);
%>

</body>
</html>
