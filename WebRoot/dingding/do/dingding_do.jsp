<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %><%@page import="cn.js.fan.util.ParamUtil"%><%@page import="com.redmoon.oa.notice.NoticeDb"%><%@page import="net.sf.json.JSONObject"%><%@page import="cn.js.fan.util.DateUtil"%><%@page import="com.redmoon.oa.person.UserDb"%><%@page import="com.redmoon.oa.pvg.Privilege"%><%@page import="com.redmoon.oa.android.NoticeReadFlagAction"%><%@page import="com.redmoon.oa.notice.NoticeReplyDb"%><%@page import="java.util.Vector"%><%@page import="java.util.Iterator"%><%@ page import="com.redmoon.dingding.service.user.UserService" %>
<%
JSONObject json = new JSONObject();
String _skey = "";
com.redmoon.oa.android.Privilege _privilege = new com.redmoon.oa.android.Privilege();
boolean _flag = _privilege.authDingDing(request);
if(_flag)
 _skey =  _privilege.getSkey();
json.put("skey",_skey);
out.print(json.toString());
%>