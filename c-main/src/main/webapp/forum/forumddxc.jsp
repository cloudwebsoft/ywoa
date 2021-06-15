<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.forum.ForumUploadDdxc"%>
<%@ page import="cn.js.fan.util.ErrMsgException"%>
<%
boolean re = false;
try {
	ForumUploadDdxc ud = new ForumUploadDdxc();
	String ret = ud.receive(application, request);
	out.print(ret);
}
catch(ErrMsgException e) {
	out.print(e.getMessage());
}
%>