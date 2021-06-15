<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import ="com.redmoon.forum.ui.*"%>
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
</style></head>
<body>
<table width="100%" border="0" align="center" style="padding:0px;">
<%
BrowMgr bm = new BrowMgr();
String[] brows = bm.getBrows();

int col = 3;
int m = 0;
int n = brows.length;
for (int i=0; i<n; i++) {
if (m==0)
	out.print("<tr>");
%>
<td style="padding:0px">
<input type="radio" value="<%=brows[i]%>" name="expression" onClick="setEmote(this)">
<img src="images/brow/<%=brows[i]%>.gif">
</td>
<%
m ++;
if (m==col) {
	out.print("</tr>");
	m = 0;
}
}
if (m!=col)
out.print("</tr>");
%>
</table>
<script>
function setEmote(emoteObj) {
	window.parent.setBrow(emoteObj.value);
}
</script>
</body>
</html>
