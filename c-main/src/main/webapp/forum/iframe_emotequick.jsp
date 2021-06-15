<html>
<head>
<META http-equiv=Content-Type content=text/html; charset=utf-8>
<title>表情</title>
</head>
<body>
<table width="300" border="0" align="left" cellpadding="0" cellspacing="0">
  <tr>
    <td height="35">
	<script language="javascript">
	function insertsmilie(smilieface){
		window.parent.frmAnnounce.Content.value += smilieface;
	}
   </script>
	<%
	int i;
	for (i=1; i<=20; i++)
	{
		out.println("<img src=\"images/emot/em"+i+".gif\" border=0 onclick=\"insertsmilie('[em"+i+"]')\" style=\"CURSOR: hand\">&nbsp;");
	}
	%></td>
  </tr>
  <tr>
    <td height="35"><%
	for (i=21;i<=40;i++)
	{
		out.println("<img src=\"images/emot/em"+i+".gif\" border=0 onclick=\"insertsmilie('[em"+i+"]')\" style=\"CURSOR: hand\">&nbsp;");
	}
	%></td>
  </tr>
  <tr>
    <td height="35"><%
	for (i=41 ;i<=59 ; i++)
		out.println("<img src=\"images/emot/em"+i+".gif\" border=0 onclick=\"insertsmilie('[em"+i+"]')\" style=\"CURSOR: hand\">&nbsp;");
	%></td>
  </tr>
  <tr>
    <td height="35"><%
	for (i=60; i<=73; i++)
		out.println("<img src=\"images/emot/em"+i+".gif\" border=0 onclick=\"insertsmilie('[em"+i+"]')\" style=\"CURSOR: hand\">&nbsp;");
  %></td>
  </tr>
</table>
</body>
</html>
