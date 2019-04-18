<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>备份</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_BACKUP)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String rootpath = request.getContextPath();
String bakpath = cfg.getProperty("Application.bak_path");
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">数据管理</td>
    </tr>
  </tbody>
</table>
<table width="332" border="0" align="center" class="tabStyle_1 percent60">
  <tr>
    <td class="tabStyle_1_title">文件备份</td>
  </tr>
  <tr>
    <td>&nbsp;<a href="bak_do.jsp?op=file">备份文件</a></td>
  </tr>
  <!--
        <tr>
          <td height="33">&nbsp;</td>
          <td height="22"><img src="images/arrow.gif" align="absmiddle">&nbsp;<a href="bak_do.jsp?op=db">备份数据库</a></td>
          <td>&nbsp;</td>
        </tr>
		-->
  <tr>
    <td>&nbsp;<a href="<%=rootpath+"/"+bakpath%>/bak_file.zip">下载ZIP文件</a></td>
  </tr>
  <!--
        <tr>
          <td height="24">&nbsp;</td>
          <td height="22"><img src="images/arrow.gif" align="absmiddle">&nbsp;<a href="<%=rootpath+"/"+bakpath%>/bak_db.bak">下载数据库备份文件</a></td>
          <td>&nbsp;</td>
        </tr>-->
</table>
</body>
</html>