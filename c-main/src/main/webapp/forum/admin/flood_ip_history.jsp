<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.forum.security.flood.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Flood List</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../../inc/common.js"></script>
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
</head>
<body>
<%@ include file="flood_nav.jsp"%>
<%
	String ip = ParamUtil.get(request, "ip");
	Action action = FloodMonitor.getAction(FloodMonitor.FLOOD_HTTP_REQUEST);
	Map ipMap = action.getIPMap();
	ActionIP aip = (ActionIP)ipMap.get(ip);
	if (aip==null) {
		out.print(StrUtil.Alert_Back("该IP不存在！"));
		return;
	}
	ArrayList arl = aip.getActionHistoryList();
%>
<table width="98%" border="0" align="center" cellpadding="3" cellspacing="0" class="frame_gray">
  <tr>
    <td width="34%" class="thead"><%=ip%>&nbsp;访问时间(最近一小时共<%=arl.size()%>次)</td>
    <td width="66%" class="thead">访问链接</td>
  </tr>
<%
Iterator ir3 = arl.iterator();
java.util.Date t = null;
String uri = null;
while (ir3.hasNext()) {
	ActionHistory ah = (ActionHistory)ir3.next();
	t = DateUtil.parse(""+ah.getTime());
	uri = ah.getUri();
%>  
  <tr class="highlight">
    <td height="20">
		<%=DateUtil.format(t, "yyyy-MM-dd HH:mm:ss")%>	</td>
    <td>
		<a href="<%=uri%>" target="_blank"><%=uri%></a>
	</td>
  </tr>
  <%
	}
%>
</table>
</body>
</html>
