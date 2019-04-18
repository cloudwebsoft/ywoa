<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %><%@page import="cn.js.fan.util.ParamUtil"%><%@page import="com.redmoon.oa.notice.NoticeDb"%><%@page import="net.sf.json.JSONObject"%><%@page import="cn.js.fan.util.DateUtil"%><%@page import="com.redmoon.oa.person.UserDb"%><%@page import="com.redmoon.oa.pvg.Privilege"%><%@page import="com.redmoon.oa.android.NoticeReadFlagAction"%><%@page import="com.redmoon.oa.notice.NoticeReplyDb"%><%@page import="java.util.Vector"%><%@page import="java.util.Iterator"%>
<%@page import="com.redmoon.weixin.mgr.FlowDoMgr"%>
<% 
JSONObject json = new JSONObject();
String op = ParamUtil.get(request,"op");
FlowDoMgr flowDoMgr = new FlowDoMgr();
if(op.equals("flow_init_list")){
	String userName = ParamUtil.get(request,"userName");
	json = flowDoMgr.flowInitList(request,userName);
}else if(op.equals("user_init_list")){
	json = flowDoMgr.usersInitList();
}

out.print(json.toString());
%>