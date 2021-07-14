<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"><head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>滑动菜单</title>
</head>
<%
int id = ParamUtil.getInt(request, "id");
%>
<frameset rows="*" cols="220,*" framespacing="2" border="0" frameborder="1">
  <frame src="slide_menu_left.jsp?groupId=<%=id%>" name="leftFileFrame" >
  <frame src="slide_menu_main.jsp?groupId=<%=id%>" name="mainFileFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
