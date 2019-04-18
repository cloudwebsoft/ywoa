<%@ page contentType="text/html; charset=utf-8" %><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="cn.js.fan.module.cms.*"%><%@ page import="cn.js.fan.module.cms.plugin.wiki.*"%><%@ page import="cn.js.fan.security.*"%><%@ page import="cn.js.fan.module.pvg.*"%><%@ page import="com.redmoon.forum.Config"%><%@ page import="org.json.*"%>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("wikiEdit")) {
	WikiDocumentAction ada = new WikiDocumentAction();
	int re = 0;
	try {
		re = ada.edit(application, request);
	}
	catch (ErrMsgException e) {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	// System.out.println(getClass() + " re=" + re);
	// response.setContentType("application/x-json");
	if (re==-1) {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", "操作失败！");
		out.print(json);
	}
	else {
		JSONObject json = new JSONObject();
		json.put("ret", re);
		if (re==WikiDocUpdateDb.CHECK_STATUS_PASSED) {
			json.put("msg", "操作成功！");
		}
		else if (re==WikiDocUpdateDb.CHECK_STATUS_WAIT) {
			json.put("msg", "编辑成功，请等待审核通过！");
		}
		else
			json.put("msg", "编辑成功！");
		out.print(json);
	}
	return;
}
%>