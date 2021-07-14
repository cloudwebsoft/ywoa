<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>页面样式</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style type="text/css" media="screen">
        html {
            height: 100%;
            _height: 100%;
        }
        body {
            height: 100%;
            _height: 100%;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body style="padding:0px; margin:0px">
<%@ include file="module_setup_inc_menu_top.jsp"%>
<script>
    o("menu12").className = "current";
</script>
<%
    String code = ParamUtil.get(request, "code");
%>
<div class="spacerH" style="margin:0px; padding:0px"></div>
<iframe src="module_css.jsp?code=<%=code%>" style="padding:0px; margin:0px; width:100%; height:100%; border:0"></iframe>
</body>
</html>