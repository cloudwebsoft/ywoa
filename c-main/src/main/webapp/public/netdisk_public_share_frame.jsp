<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.cloudwebsoft.framework.util.*" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>共享目录</title>
</head>
<%
/*
String ip1 = "10.1.180.1";
String ip2 = "10.1.180.255";
long ip = IPUtil.ip2long(request.getRemoteAddr());
if (!(ip>=IPUtil.ip2long(ip1) && ip<=IPUtil.ip2long(ip2))) {
%>
	IP is invalid.
<%
	return;
}
*/
%>
<frameset rows="*" cols="180,*" framespacing="2" border="0">
  <frame src="netdisk_public_share_left.jsp" name="leftPublicShareFrame" >
  <frame src="netdisk_public_attach_list.jsp" name="mainPublicShareFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
