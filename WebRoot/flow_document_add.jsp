<%@ page contentType="text/html;charset=utf-8"
import="cn.js.fan.util.*"
import="com.redmoon.oa.flow.*"%><%response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
WorkflowMgr wfm = new WorkflowMgr();
try {
	if (wfm.addNewDocument(application, request))
		out.print("上传成功！");}
catch (ErrMsgException e) {
	out.print(e.getMessage());
}%>