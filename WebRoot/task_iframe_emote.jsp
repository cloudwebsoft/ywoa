<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<html xmlns="http://www.wk.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
<title>表情列表</title>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
}
-->
</style>
<%
String expression = ParamUtil.get(request, "expression").trim();
%>
<script>
var expr = "<%=expression%>";
function window_onload() {
	var rng = document.body.createTextRange();
	var sb = "expr" + expr + "end";
	if (rng.findText(sb, 1, 6)==true) // 向前搜索大小写敏感，匹配整字
	{
		rng.scrollIntoView();
	}
}

function changeexpression(i)
{
	window.parent.frmAnnounce.expression.value = i;
	if (i==0)
	{
		window.parent.expressspan.innerHTML = "无";
		return;
	}
	window.parent.expressspan.innerHTML = "<img align=absmiddle src=forum/images/emot/em"+i+".gif>";
}
</script>
</head>
<body onLoad="window_onload()">
<table width="100%" align="center">
  <tr>
    <td height="25">
	<%
	int i;
	for (i=1; i<=20; i++)
	{
		out.println("<img src=\"forum/images/emot/em"+i+".gif\" border=0 onclick=\"changeexpression("+i+")\" style=\"CURSOR: hand\"><span style='display:none'>expr" + i + "end</span>&nbsp;");
	}
	%></td>
  </tr>
  <tr>
    <td height="25"><%
	for (i=21;i<=40;i++)
	{
		out.println("<img src=\"forum/images/emot/em"+i+".gif\" border=0 onclick=\"changeexpression("+i+")\" style=\"CURSOR: hand\"><span style='display:none'>expr" + i + "end</span>&nbsp;");
	}
	%></td>
  </tr>
  <tr>
    <td height="25"><%
	for (i=41 ;i<=59 ; i++)
		out.println("<img src=\"forum/images/emot/em"+i+".gif\" border=0 onclick=\"changeexpression("+i+")\" style=\"CURSOR: hand\"><span style='display:none'>expr" + i + "end</span>&nbsp;");
	%></td>
  </tr>
  <tr>
    <td height="25"><%
	for (i=60; i<=73; i++)
		out.println("<img src=\"forum/images/emot/em"+i+".gif\" border=0 onclick=\"changeexpression("+i+")\" style=\"CURSOR: hand\"><span style='display:none'>expr" + i + "end</span>&nbsp;");
  %></td>
  </tr>
</table>		  
</body>
</html>
