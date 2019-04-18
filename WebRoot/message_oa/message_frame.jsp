<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>内部邮箱页面框架</title>
</head>
<%
String op = ParamUtil.get(request, "op");
String receiver = ParamUtil.get(request, "receiver");
String title = ParamUtil.get(request, "title");
String content = ParamUtil.get(request, "content");
long id = ParamUtil.getLong(request, "id", -1);
String src = op.equals("send") ? "send.jsp?receiver=" + StrUtil.UrlEncode(receiver) + "&title=" + StrUtil.UrlEncode(title) + "&content=" + StrUtil.UrlEncode(content): "message.jsp";
if (id!=-1) {
	src = "showmsg.jsp?id=" + id;
}
%>
<frameset id="mainFrame" cols="167,*" frameborder="0" framespacing="0">
  <frame id="leftFrame" name="leftFrame" src="left_menu.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize />
  <frame id="rightFrame" name="rightFrame" src="<%=src %>" marginwidth="0" marginheight="0" scrolling="yes" frameborder="0" noresize />
</frameset>
<noframes>
<body>
很抱歉，您使用的浏览器不支持框架功能，请转用新的浏览器。
</body>
</noframes>
</html>