<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.base.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDAO"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
//String macroType = ParamUtil.get(request, "macroType");
//String objId = ParamUtil.get(request, "objId");
//String oldValue = ParamUtil.get(request, "oldValue");
//String oldShowValue = ParamUtil.get(request, "oldShowValue");
//String formCode = ParamUtil.get(request, "formCode");
//String fieldName = ParamUtil.get(request, "fieldName");

String macroType = request.getParameter("macroType");
String objId = request.getParameter("objId");
String oldValue = request.getParameter("oldValue");
String oldShowValue = request.getParameter("oldShowValue");
String formCode = request.getParameter("formCode");
String fieldName = request.getParameter("fieldName");

MacroCtlMgr mam = new MacroCtlMgr();
MacroCtlUnit mcu = mam.getMacroCtlUnit(macroType);
IFormMacroCtl imc = mcu.getIFormMacroCtl();
JSONObject json = new JSONObject();
if (imc!=null) {
	String str = imc.ajaxOnNestTableCellDBClick(request, formCode, fieldName, oldValue, oldShowValue, objId);
	
	json.put("ret", "1");
	json.put("html", str);
	out.print(json);
}
else {
	json.put("ret", "0");
	json.put("html", "控件不存在");
	out.print(json);
}
%>