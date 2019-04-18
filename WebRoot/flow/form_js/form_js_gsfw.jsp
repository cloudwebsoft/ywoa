<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%
Privilege pvg = new Privilege();
/*
System.out.println(getClass() + " " + ParamUtil.get(request, "myActionId"));
System.out.println(getClass() + " " + request.getAttribute("cwsId"));
System.out.println(getClass() + " " + session.getId());
System.out.println(getClass() + " " + pvg.getUser(request));
*/
// test
// int flowId = ParamUtil.getInt(request, "flowId");
String userName = ParamUtil.get(request, "userName");
// System.out.println(getClass() + " userName=" + userName);
%>
