<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.address.*"%>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.visual.ModuleSetupDb"%>
<%@ page import = "com.redmoon.oa.visual.ModuleExportTemplateDb"%>
<%@ page import = "com.redmoon.oa.visual.ModuleExportTemplateMgr"%>
<%@ page import = "com.redmoon.oa.visual.FormDAO"%> 
<%@ page import = "com.cloudwebsoft.framework.util.LogUtil"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.person.UserDb"%>
<%@ page import = "com.redmoon.oa.dept.DeptUserDb"%>
<%@ page import = "com.redmoon.oa.dept.DeptDb"%>
<%@ page import = "com.redmoon.oa.person.UserCache"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String op = ParamUtil.get(request, "op");
	String code = ParamUtil.get(request, "code");
	if ("".equals(code)) {
		code = ParamUtil.get(request, "formCode");
	}
	
	boolean isRelate = ParamUtil.getBoolean(request, "isRelate", false);	
	if (isRelate) {
		code = ParamUtil.get(request, "formCodeRelated");
	}
	
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(code);
	if (msd==null) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
		return;
	}	
	
	String formCode = msd.getString("form_code");	
	String userName = privilege.getUser(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>导出-选择模板</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<%	
	String username = privilege.getUser(request);
	
	// 将request中其它参数也传至url中
	String param = "";
	Enumeration paramNames = request.getParameterNames();
	while (paramNames.hasMoreElements()) {
		String paramName = (String) paramNames.nextElement();
		String[] paramValues = request.getParameterValues(paramName);
		if (paramValues.length == 1) {
			String paramValue = ParamUtil.get(request, paramName);
			// 过滤掉code
			if (paramName.equals("code"))
				;
			else
				param += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
		}
		else {
			for (int i=0; i<paramValues.length; i++) {
				param += "&" + paramName + "=" + StrUtil.UrlEncode(paramValues[i]);
			}
		}
	}
%>
<form name="form1" method="post">
<table width="525" border="0" align="center" cellspacing="0" class="tabStyle_1 percent60">
	<thead>
    <tr>
      <td class="tabStyle_1_title">请选择模板文件</td>
    </tr>
    </thead>
    <tr>
      <td width="319" align="center">
        <%
		Vector v = ModuleExportTemplateMgr.getTempaltes(request, formCode);
		if (v.size()>0) {
		%>
      	模板
        <select id="templateId" name="templateId" title="默认按显示的列">
        <option value="">默认</option>
        <%
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			ModuleExportTemplateDb mid = (ModuleExportTemplateDb)ir.next();
			%>
			<option value="<%=mid.getLong("id")%>"><%=mid.getString("name")%></option>
			<%
		}
		%>
        </select>
        <%}%>
		<input class="btn btn-ok" type="button" value="确定" /></td>
    </tr>
</table>
</form>
</body>
<script>
$('.btn-ok').click(function() {
	<%
	if (!isRelate) {
	%>
		window.open('<%=request.getContextPath()%>/visual/module_excel.jsp?code=<%=code%>&<%=param%>&templateId=' + o('templateId').value);
	<%}else{%>
		window.open('<%=request.getContextPath()%>/visual/module_excel_relate.jsp?code=<%=code%>&<%=param%>&templateId=' + o('templateId').value);
	<%}%>
	window.close();
});
</script>
</html>