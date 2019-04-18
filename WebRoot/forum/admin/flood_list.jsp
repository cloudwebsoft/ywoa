<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.forum.security.flood.*" %>
<%@ page import="com.redmoon.forum.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("reset")) {
	String ip = ParamUtil.get(request, "ip");
	FloodMonitor.resetAction(FloodMonitor.FLOOD_HTTP_REQUEST, ip);
	out.print(StrUtil.Alert_Redirect("操作成功!", "flood_list.jsp"));
	return;
}
%>
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
<script>
$("menu2").className="active";
</script>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="frame_gray">
  <tr>
    <td width="10%" class="thead">IP</td>
    <td width="10%" class="thead">最后位置</td>
    <td width="10%" class="thead">一小时次数</td>
    <td width="14%" class="thead">最后访问时间</td>
    <td width="47%" class="thead">最后访问链接</td>
    <td width="9%" class="thead">操作</td>
  </tr>
<%
	Action action = FloodMonitor.getAction(FloodMonitor.FLOOD_HTTP_REQUEST);
	Map ipMap = action.getIPMap();
	Collection li = ipMap.values();
	IPStoreDb ips = new IPStoreDb();
	for (Iterator ir2=li.iterator(); ir2.hasNext();) {
		ActionIP aip = (ActionIP)ir2.next();
		String ip = aip.getIp();
		ArrayList arl = aip.getActionHistoryList();	
		Iterator ir3 = arl.iterator();
		java.util.Date t = null;
		String uri = null;
		if (ir3.hasNext()) {
			ActionHistory ah = (ActionHistory)ir3.next();
			t = DateUtil.parse(""+ah.getTime());
			uri = ah.getUri();
		}
		%>
  <tr class="highlight">
    <td height="20" align="center">
	<%
	String style = "";
	if (arl.size()>=FloodMonitor.getAction(FloodMonitor.FLOOD_HTTP_REQUEST).getActionsMaxPerHour()) {
		style = "style=\"color:red\"";
	}%>
	<a href="flood_ip_history.jsp?ip=<%=aip.getIp()%>" <%=style%>><%=aip.getIp()%></a>
	</td>
    <td align="center">
        <%=ips.getPosition(ip)%> </td>
    <td align="center">
	<%=arl.size()%>	</td>
    <td align="center">
		<%if (uri!=null) {%>
		<%=DateUtil.format(t, "yyyy-MM-dd HH:mm:ss")%>
		<%}%>	</td>
    <td>
		<%if (uri!=null) {%>
		<a href="<%=uri%>" target="_blank"><%=uri%></a>
		<%}%>	</td>
    <td align="center"><a href="flood_list.jsp?op=reset&amp;ip=<%=ip%>">重置</a></td>
  </tr>
  <%
	}
%>
</table>
</body>
</html>
