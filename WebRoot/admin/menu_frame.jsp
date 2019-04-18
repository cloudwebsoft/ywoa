<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>菜单管理</title>
</head>
<%
String root_code = ParamUtil.get(request, "root_code");
%>
<frameset rows="*" cols="256,*">
  <frame src="menu_tree.jsp?root_code=<%=StrUtil.UrlEncode(root_code)%>" name="leftFrame">
  <frame src="menu_right.jsp" name="rightFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
