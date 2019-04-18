<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="backup" scope="page" class="cn.js.fan.util.Backup"/>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_BACKUP))
{
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理登录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td class="tdStyle_1">数据管理</td>
  </tr>
</table>
<table class="tabStyle_1 percent60" align="center">
  <TBODY>
  	<tr><td class="tabStyle_1_title">数据库及文件备份</td></tr>
    <TR>
          <td align="center" valign="middle"><br />
    <%
	String op = ParamUtil.get(request, "op");
	String rootpath = Global.getRealPath();
	String srcpath = Global.getRealPath() + "upfile/";
	String bakpath = cfg.getProperty("Application.bak_path");;
	if (op.equals("file")) {
		backup.copyDirectory(Global.getRealPath()+bakpath+"/file",srcpath);//拷贝至file目录下
		out.print(StrUtil.p_center("拷贝文件成功！"));
		String zipfilepath = Global.getRealPath()+bakpath+"/bak_file.zip";
		try {
			backup.generateZipFile(srcpath, zipfilepath);
		}
		catch (java.util.zip.ZipException e) {
			
		}
		catch (Exception e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
		out.print(StrUtil.p_center("压缩ZIP文件成功！"));
		%>
		<a href="<%=rootpath+"/"+bakpath%>/bak_file.zip">下载ZIP文件</a>
	<%}
	if (op.equals("db")) {
		String dbfile = Global.getRealPath() + bakpath + "/bak_db.bak";
		String dbname = "zjrj";
		if (backup.BackupDB(dbname, Global.getDefaultDB(), dbfile))
			out.print(StrUtil.p_center("备份数据库成功！"));
		else
			out.print(StrUtil.p_center("备份数据库失败！"));
		%>
		<a href="<%=rootpath+"/"+bakpath%>/bak_db.bak">下载数据库备份文件</a>
	<%}%><br /><br /></td>
  </TBODY>
</TABLE>
</body>
</html>