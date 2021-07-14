<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Frameset//EN">
<HTML><HEAD><TITLE><%=cn.js.fan.web.Global.AppName%> - CMS</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<META content="MSHTML 6.00.3790.259" name=GENERATOR></HEAD>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if (!privilege.isUserLogin(request))
{
	response.sendRedirect("index.jsp");
	return;
}

%>
<FRAMESET border=0 
frameSpacing=0 rows=49,* frameBorder=NO cols=*><FRAME name=topFrame 
src="top.jsp" noResize scrolling=no><FRAMESET 
border=0 frameSpacing=0 rows=* frameBorder=NO cols=198,*><FRAME name=leftFrame 
src="menu.jsp" 
noResize scrolling=yes><FRAME name=mainFrame 
src="main.jsp" 
scrolling=yes></FRAMESET></FRAMESET><noframes></noframes></HTML>
