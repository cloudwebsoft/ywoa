<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="java.util.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE>菜单</TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="css.css">
<%@ include file="inc/nocache.jsp"%>
<link href="skin/default/css.css" rel="stylesheet" type="text/css">
<script src="forum/inc/main.js"></script>
<META content="Microsoft FrontPage 4.0" name=GENERATOR>
</HEAD>
<BODY>
<table width="100%" border="0" cellspacing="0" cellpadding="3">
  <tr>
    <td>在线用户</td>
  </tr>
<%
OnlineUserDb oud = new OnlineUserDb();
Iterator ir = oud.list().iterator();
while (ir.hasNext()) {
	oud = (OnlineUserDb)ir.next();
	
	String mstr = "<a target='mainFrame' href='message_oa/send.jsp?receiver=" + StrUtil.UrlEncode(oud.getName()) + "'>" + "发送短消息" + "</a>";
	%>
  <tr>
    <td>
	<a href="#" onMouseOver="showmenu(event, '<%=mstr%>', 0)"><%=oud.getName()%></a>
	</td>
  </tr>
<%}%>
</table>
<div class=menuskin id=popmenu 
      onmouseover="clearhidemenu();highlightmenu(event,'on')" 
      onmouseout="highlightmenu(event,'off');dynamichide(event)" style="Z-index:100"></div>
</BODY>
</HTML>
