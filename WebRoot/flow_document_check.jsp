<%@ page contentType="text/html;charset=utf-8"
import="cn.js.fan.util.*"
import="com.redmoon.oa.flow.*"%>
<%@page import="com.redmoon.oa.ui.LocalUtil"%><%
response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
WorkflowMgr wfm = new WorkflowMgr();
try {
	String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	if (wfm.uploadDocument(application, request))
		out.print(str);
}
catch (ErrMsgException e) {
	out.print(e.getMessage());
}
%>