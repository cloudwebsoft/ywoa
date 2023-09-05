<%@ page contentType="text/html;charset=utf-8" import = "java.io.File" import = "cn.js.fan.util.*"%><%@page import="org.json.JSONObject"%><%@page import="com.redmoon.oa.pvg.Privilege"%><%
String priv="admin.user";
if (!new Privilege().isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("convert")) {
	String user = ParamUtil.get(request, "user");
	String title = ParamUtil.get(request, "title");
	String userRealName = ParamUtil.get(request, "userRealName");
	String jobCode = ParamUtil.get(request, "jobCode");
	String jobName = ParamUtil.get(request, "jobName");
	String proxyJobCode = ParamUtil.get(request, "proxyJobCode");
	String proxyJobName = ParamUtil.get(request, "proxyJobName");
	String proxyUserName = ParamUtil.get(request, "proxyUserName");
	String proxyUserRealName = ParamUtil.get(request, "proxyUserRealName");
	
	JSONObject json = new JSONObject();
	try {
		json.put("user", new String(user.getBytes(), "GBK"));
		json.put("title", new String(title.getBytes(), "GBK"));
		json.put("userRealName", new String(userRealName.getBytes(), "GBK"));
		json.put("jobCode", new String(jobCode.getBytes(), "GBK"));
		json.put("jobName", new String(jobName.getBytes(), "GBK"));
		json.put("proxyJobCode", new String(proxyJobCode.getBytes(), "GBK"));
		json.put("proxyJobName", new String(proxyJobName.getBytes(), "GBK"));
		json.put("proxyUserName", new String(proxyUserName.getBytes(), "GBK"));
		json.put("proxyUserRealName", new String(proxyUserRealName.getBytes(), "GBK"));
		json.put("ret", 1);
	} catch (Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	out.print(json.toString());
	return;
}
%>
