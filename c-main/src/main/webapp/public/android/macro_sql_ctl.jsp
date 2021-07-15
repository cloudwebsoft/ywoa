<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.android.*"%>
<%@ page import = "java.util.regex.*"%>
<%@ page import = "org.json.*"%>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.api.ISQLCtl" %>
<%
Privilege pvg = new Privilege();
String skey = ParamUtil.get(request, "skey");
JSONObject jsonRet = new JSONObject();
boolean re = pvg.Auth(skey);
if (re) {
	try {
		jsonRet.put("res", "-2");
		jsonRet.put("msg", "时间过期");
		out.print(jsonRet.toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return;
}

pvg.doLogin(request, skey);

int flowId = ParamUtil.getInt(request, "flowId", -1);
String fieldName = ParamUtil.get(request, "fieldName");
String formCode = ParamUtil.get(request, "formCode");

if ("".equals(formCode)) {
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	
	Leaf lf = new Leaf();
	lf = lf.getLeaf(wf.getTypeCode());
	formCode = lf.getFormCode();
}

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

FormField ff = fd.getFormField(fieldName);
if (ff==null) {
	System.out.println("macro_sql_ctl.jsp 表单：" + fd.getName() + " formCode=" + formCode + " 字段： " + fieldName + " is null");
	return;
}

String op = ParamUtil.get(request, "op");
// if (op.equals("onChange")) {
	JSONObject json = new JSONObject();
	MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
	ISQLCtl sqlCtl = macroCtlService.getSQLCtl();

	JSONObject field = null;
	try {
		field = sqlCtl.getCtl(request, flowId, ff);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
	if (field!=null) {
		json.put("res", "1");
		json.put("msg", "操作成功！");
		json.put("field", field);
	}
	else {
		json.put("res", "-1");
		json.put("msg", "操作失败！");
	}
	out.print(json);
// }
%>