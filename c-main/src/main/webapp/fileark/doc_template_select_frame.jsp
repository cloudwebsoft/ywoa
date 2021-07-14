<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>选择</title>
</head>
<%
String dirCode = ParamUtil.get(request, "dirCode");
%>
<frameset rows="*" cols="243,*" framespacing="0" border="1">
  <frame src="doc_template_select_left.jsp?dirCode=<%=StrUtil.UrlEncode(dirCode)%>" name="leftFrame" scrolling="yes" >
  <frame src="doc_template_select_main.jsp?dir_code=<%=StrUtil.UrlEncode(dirCode)%>" name="mainFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
