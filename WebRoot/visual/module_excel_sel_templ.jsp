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
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String op = ParamUtil.get(request, "op");
	String code = ParamUtil.get(request, "code");
	if ("".equals(code)) {
		code = ParamUtil.get(request, "formCode");
	}
	
	String mode = ParamUtil.get(request, "mode");
	
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
	
	if ("subTagRelated".equals(mode)) {
		code = ParamUtil.get(request, "code"); // 如果是关联选项卡，则code为主模块的编码
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
	String url = "module_excel.jsp";
    if (isRelate) {
        url = "module_excel_relate.jsp";
    }
%>
<form id="form1" name="form1" method="post" action="<%=url%>" target="_blank">
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
        <%
		}
    
        // 将request中其它参数也传至url中
        String param = "";
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = ParamUtil.getParameters(request, paramName);
            if (paramValues.length == 1) {
                String paramValue = ParamUtil.getParam(request, paramName);
          %>
                <input name="<%=paramName%>" value="<%=paramValue%>" type="hidden"/>
          <%
            }
            else {
                for (int i=0; i<paramValues.length; i++) {
                    param += "&" + paramName + "=" + StrUtil.UrlEncode(paramValues[i]);
          %>
                    <input name="<%=paramName%>" value="<%=paramValues[i]%>" type="hidden"/>
          <%
                }
            }
        }
        %>
		<input class="btn btn-ok" type="submit" value="确定" /></td>
    </tr>
</table>
</form>
</body>
<script>
// IE11中用window.open方式时，当含有coo_address、coo_address_cond时，接收到coo_address的值为?_address_cond=0?_address=，而chrome中不会，故改用form的submit方式
/*$('.btn-ok').click(function() {
	<%
	if (!isRelate) {
	%>
		window.open('<%=request.getContextPath()%>/visual/module_excel.jsp?code=<%=code%>&<%=param%>&templateId=' + o('templateId').value);
	<%}else{%>
		window.open('<%=request.getContextPath()%>/visual/module_excel_relate.jsp?code=<%=code%>&<%=param%>&templateId=' + o('templateId').value);
	<%}%>
	window.close();
});*/
</script>
</html>