<%@ page contentType="text/html;charset=utf-8" import="cn.js.fan.util.*" import="com.redmoon.oa.netdisk.*"%><%
response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
/*
DocumentMgr dm = new DocumentMgr();
try {
if (dm.uploadDocument(application, request))
	out.print("上传成功！");
}catch (ErrMsgException e) {
	out.print(e.getMessage());
}
*/

try {
PublicAttachmentMgr pam = new PublicAttachmentMgr();
if (pam.uploadOffice(application, request))
	out.print("上传成功！");
}catch (ErrMsgException e) {
	out.print(e.getMessage());
}
%>