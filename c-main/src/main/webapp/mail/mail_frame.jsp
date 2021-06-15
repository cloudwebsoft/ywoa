<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@page import="com.redmoon.oa.emailpop3.EmailPop3Db"%>
<%@page import="java.util.Iterator"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String myname = privilege.getUser(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>邮件页面框架</title>
</head>
<frameset id="mainFrame" cols="220,*" frameborder="0" framespacing="0">
  <frame id="leftFrame" name="leftFrame" src="left_menu.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize />
  
  <%
  	EmailPop3Db epd = new EmailPop3Db();
	Iterator ir = epd.getEmailPop3DbOfUser(myname).iterator();
	int i=0;
	String server = "";
	while (ir.hasNext()) {
		i++;
		epd = (EmailPop3Db)ir.next();
		server = epd.getServer();
		
	}
  %>
  <%
  	if(i == 1 && server.equals("")){
  %>
   <frame id="rightFrame" name="rightFrame" src="set_email_pop.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize />
  <%}else{ %>
  <frame id="rightFrame" name="rightFrame" src="in_box.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize />
  <%} %>
</frameset>
<noframes>
<body>
很抱歉，您使用的浏览器不支持框架功能，请转用新的浏览器。
</body>
</noframes>
</html>
