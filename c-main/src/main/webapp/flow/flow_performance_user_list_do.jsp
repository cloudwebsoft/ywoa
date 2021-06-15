<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("modifyPerformance")) {
	MyActionDb mad = new MyActionDb();
	String performance = ParamUtil.get(request,"performance");
	String performanceReason = ParamUtil.get(request,"performanceReason");
	String userName = ParamUtil.get(request,"userName");
	int mid = ParamUtil.getInt(request,"mid");

	mad = mad.getMyActionDb(mid);
	mad.setPerformance(StrUtil.toDouble(performance));
	mad.setPerformanceReason(performanceReason);
	mad.setPerformanceModify(userName);
	
	boolean re = mad.save();

	if (re) {
		out.print("{\"re\":true, \"msg\":\"操作成功\"}");
	}
	else {
		out.print("{\"re\":false, \"msg\":\"操作失败\"}");
	}
	return;
}
%>