<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>门户菜单</title>
</head>
<%
    int portalId = ParamUtil.getInt(request, "portalId", -1);
    String tabId = ParamUtil.get(request, "tabId");
%>
<frameset rows="*" cols="220,*" framespacing="2" border="0" frameborder="1">
    <frame src="portal_menu_left.jsp?portalId=<%=portalId%>&tabId=<%=tabId%>" name="leftFileFrame">
    <frame src="portal_menu_main.jsp?portalId=<%=portalId%>&tabId=<%=tabId%>" name="mainFileFrame">
</frameset>
<noframes>
    <body>
    </body>
</noframes>
</html>
