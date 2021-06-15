<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.DeptUserDb" %>
<%@ page import="java.util.Vector" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");

	String code = ParamUtil.get(request, "code");
    String pageType = ParamUtil.get(request, "pageType");
	long myActionId = ParamUtil.getLong(request, "myActionId", -1);
	String flowId = ParamUtil.get(request,"flowId");
	Privilege pvg = new Privilege();
	String op = ParamUtil.get(request, "op");
	if ("search".equals(op)) {
		String formCode = ParamUtil.get(request, "form_code");
		if (!"".equals(formCode)) {
			response.setContentType("text/javascript;charset=utf-8");
%>
$(function() {
	setTimeout(function() {
		$("input[name='form_code']").attr("readonly", "readonly");
		$("input[name='form_code']").css("background-color", "#eee");
	}, 0);
});
<%
		}
	}
%>