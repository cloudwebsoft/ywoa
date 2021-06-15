<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("check")) {
	String formCode = ParamUtil.get(request, "formCode");
	String fieldName = ParamUtil.get(request, "field");
	JSONObject json = new JSONObject();
	ParamChecker pck = new ParamChecker(request);
	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	// System.out.println(getClass() + " fieldName=" + fieldName);
	FormField ff = fd.getFormField(fieldName);
	try {
		// LogUtil.getLog(getClass()).info("ruleStr=" + ruleStr);
		FormDAOMgr.checkField(request, null, pck, ff, fieldName, null);
	} catch (CheckErrException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	
	// System.out.println(getClass() + " pck.getMsgs().size()=" + pck.getMsgs().size());
				
	if (pck.getMsgs().size()!=0) {
		json.put("ret", "0");
		json.put("msg", pck.getMessage(false));
		out.print(json);
		return;
	}

	json.put("ret", "1");
	json.put("msg", "检测通过！");
	out.print(json);
}
%>