<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.kit.util.*"%>
<%@ page import="cn.js.fan.util.ErrMsgException"%>
<%
try {
	UploadDdxc ud = new UploadDdxc();
	String ret = ud.receive(application, request);
	out.print(ret);
}
catch(ErrMsgException e) {
	out.print(e.getMessage());
}
%>