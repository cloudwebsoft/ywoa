<%@ page contentType="text/html;charset=utf-8"
import="cn.js.fan.util.*"
import="com.redmoon.oa.fileark.*"%><%
response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
DocumentMgr dm = new DocumentMgr();
try {
	if (dm.uploadDocument(application, request))
		out.print(cn.js.fan.web.SkinUtil.LoadString(request, "info_op_success"));
}
catch (ErrMsgException e) {
	out.print(e.getMessage());
}
%>