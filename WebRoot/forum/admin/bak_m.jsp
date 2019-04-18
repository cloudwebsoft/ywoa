<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Bakup</title>
<link href="default.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style1 {
	color: #FFFFFF;
	font-weight: bold;
}
.style2 {color: #FFFFFF}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String rootpath = request.getContextPath();
String bakpath = cfg.getProperty("Application.bak_path");
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="head"><lt:Label res="res.label.forum.admin.bak_file" key="data_manage"/></td>
    </tr>
  </tbody>
</table>
<br>
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><lt:Label res="res.label.forum.admin.bak_file" key="file_bak"/></TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
      <TD height="175" align="center" style="PADDING-LEFT: 10px"><table width="353" border="0" align="center">
        <tr>
          <td height="28">&nbsp;</td>
          <td height="22"><img src="images/arrow.gif" align="absmiddle">&nbsp;<a href="bak_file.jsp"><lt:Label res="res.label.forum.admin.bak_file" key="do_bak"/></a></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td height="33">&nbsp;</td>
          <td height="22"><img src="images/arrow.gif" align="absmiddle">&nbsp;<a href="<%=rootpath+"/"+bakpath%>/bak_file.zip"><lt:Label res="res.label.forum.admin.bak_file" key="download_zip"/></a></td>
          <td>&nbsp;</td>
        </tr>
      </table></TD>
    </TR>
    <TR>
      <TD class=tfoot align=right><DIV align=right> </DIV></TD>
    </TR>
  </TBODY>
</TABLE>
</body>
</html>