<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.Properties" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>登录处理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="inc/common.js"></script>
</head>
<body>
<%
String aUserName = ParamUtil.get(request, "aUserName");
String qUserName = ParamUtil.get(request, "qUserName");

String question = ParamUtil.get(request, "question");
String answer = ParamUtil.get(request, "answer");
String thread = ParamUtil.get(request, "thread");


System.out.println(getClass() + " " + aUserName);
System.out.println(getClass() + " " + qUserName);
System.out.println(getClass() + " " + question);
System.out.println(getClass() + " " + answer);
System.out.println(getClass() + " " + thread);
%>
</body>
</head>