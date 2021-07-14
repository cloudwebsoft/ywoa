<%@ page contentType="text/html; charset=utf-8"%><%@ page import="java.util.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="cn.js.fan.db.*"%><%@ page import="org.json.*"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsMgr"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String op = ParamUtil.get(request, "op");
MobileSkinsMgr mobileSkins = new MobileSkinsMgr();
if(op.equals("del")){
	String codes = ParamUtil.get(request,"codes");
	out.print(mobileSkins.deleteMobileSkinsByBatch(codes));
}
%>