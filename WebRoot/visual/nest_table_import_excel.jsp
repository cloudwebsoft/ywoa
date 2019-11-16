<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>导入Excel</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%
	String formCode = ParamUtil.get(request, "formCode");
	NestTableCtl ntc = new NestTableCtl();
	boolean re = false;
	String op = ParamUtil.get(request, "op");
	String nestType = ParamUtil.get(request, "nestType");
	String nestFieldName = ParamUtil.get(request, "nestFieldName");
	if (op.equals("add")) {
		try {
			JSONArray jsonAry = ntc.uploadExcel(application, request);
			// System.out.println(getClass() + " " + jsonAry);
			if (jsonAry.length()>0) {
%>
				<script>
				<%if (nestType.equals("detaillist")) {%>
				window.opener.insertRow(null, '<%=jsonAry%>', "<%=nestFieldName%>");				
				<%} else {%>
				window.opener.doImportExcel('<%=jsonAry%>');	
				<%}%>
			
				alert("导入成功!");
				window.close();
				</script>
<%			}
			else
				out.print(StrUtil.Alert("文件不能为空！"));
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
	}
%>
</head>
<body>
<form name="form1" action="nest_table_import_excel.jsp?op=add&formCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=nestFieldName%>&nestType=<%=nestType%>" method="post" encType="multipart/form-data">
<table width="409" border="0" align="center" cellspacing="0" class="tabStyle_1 percent98">
	<thead>
    <tr>
      <td align="left" class="right-title">&nbsp;请选择需导入的文件</td>
    </tr>
    </thead>
    <tr>
      <td width="319" align="left">
        <input title="选择附件文件" type="file" size="20" name="excel">
		<input name="formCode" value="<%=formCode%>" type=hidden>
		<input class="btn" name="submit" type="submit" value="确  定" />
        </td>
    </tr>
    <tr>
      <td align="left">
      Excel文件表头：      
      <a target="_blank" href="nest_import_excel_template.jsp?formCode=<%=formCode%>">下载模板</a>
      <br />
        <%
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDbOrInit(formCode);
            // String listField = StrUtil.getNullStr(msd.getString("list_field"));
			String[] fieldCodes = msd.getColAry(false, "list_field");
            
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);
			String str = "";
			for (int i=0; i<fieldCodes.length; i++) {
				if (str.equals(""))
					str = fd.getFormField(fieldCodes[i]).getTitle();
				else
					str += "&nbsp;|&nbsp;" + fd.getFormField(fieldCodes[i]).getTitle();
			}
			out.print(str);
	  %>
      </td>
    </tr>
</table>
</form>
</body>
</html>
