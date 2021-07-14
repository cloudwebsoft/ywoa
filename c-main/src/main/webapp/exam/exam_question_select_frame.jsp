<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@page import="cn.js.fan.util.ParamUtil" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>题目选择</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="This is my page">
</head>
<%
    String type = ParamUtil.get(request, "type");
    String major = ParamUtil.get(request, "major");
%>
<frameset id="uiframe" cols="20%,*" cols="*" frameborder="0" framespacing="0">
    <frame noresize="noresize" name="leftFrame" src="question_select_left_menu.jsp?type=<%=type %>&major=<%=major %>" scrolling="auto" marginwidth="0" marginheight="0" frameborder="0"></frame>
    <frame id="questionListId" noresize="noresize" src="exam_question_select.jsp?type=<%=type %>&op=search&major=<%=major %>" name="rFrame" style="float: left;margin: 20px 0px;" marginwidth="0" marginheight="0" frameborder="0"></frame>
</frameset>
<body>
</body>
</html>
