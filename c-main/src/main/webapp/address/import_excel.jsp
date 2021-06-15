<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.address.*"%>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.util.ExcelUploadUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>导入通讯录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<%
	String group = ParamUtil.get(request, "group");
	int type = ParamUtil.getInt(request, "type", AddressDb.TYPE_USER);
	if (type==AddressDb.TYPE_PUBLIC) {
		if (!privilege.isUserPrivValid(request, "admin.address.public")) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}
	String username = privilege.getUser(request);
	ExcelUploadUtil fum = new ExcelUploadUtil();
	boolean re = false;
	String op = ParamUtil.get(request, "op");
	String excelFile="";
	if (op.equals("add")) {
		try {
			excelFile = fum.uploadExcel(application, request);
			// System.out.println("import_excel.jsp " + excelFile);
			// ExcelRead er = new ExcelRead(excelFile, username, type, group);
			if (!excelFile.equals("")) {
				ExcelRead er = new ExcelRead();
				er.Excelhad(excelFile, username, type, group);
				//out.print(excelFile);
				File file = new File(excelFile);
				file.delete();
				//jAlert("导入成功，点击确定后请刷新页面查看导入结果!","提示");
				//window.close();
				out.print("<script>var dlg = window.opener ? window.opener : dialogArguments;dlg.location.reload();window.close();</script>");
			} else {
				out.print(StrUtil.jAlert("文件不能为空！","提示"));
			}
		} catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		}
		return;
	}
%>
</head>
<body>
<form name="form1" action="?op=add&type=<%=type%>&group=<%=group%>" method="post" encType="multipart/form-data">
<table width="325" border="0" align="center" cellspacing="0" class="tabStyle_1 percent98">
	<thead>
    <tr>
      <td class="right-title">&nbsp;请选择需导入的文件</td>
    </tr>
    </thead>
    <tr>
      <td width="319" align="center">
        <input title="选择附件文件" type="file" size="20" name="excel">
		<input name="type" value="<%=type%>" type=hidden>
		<input class="btn" name="submit" type="submit" value="确  定" /></td>
    </tr>
</table>
</form>
</body>
</html>
