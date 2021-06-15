<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%
    String nestFieldName = ParamUtil.get(request, "nestFieldName");
    String rootPath = request.getContextPath();
%>
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>辅助表格</title>
    <link href="<%=rootPath%>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="<%=rootPath%>/inc/common.js"></script>
    <script src="<%=rootPath%>/js/jquery-1.9.1.min.js"></script>
    <script src="<%=rootPath%>/js/jquery-migrate-1.2.1.min.js"></script>
    <script src="<%=rootPath%>/inc/flow_dispose.jsp"></script>
    <script src="<%=rootPath%>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="<%=rootPath%>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <script src="<%=rootPath%>/inc/livevalidation_standalone.js"></script>
</head>
<body>
<div id="container">
</div>
<script>
    $(function () {
        var html = window.parent.getIframeContent<%=nestFieldName%>();
        $('#container').append(html);

        var $nestTr = $('#container').find("#tableHelper tr");
        window.parent.setNestTr<%=nestFieldName%>($nestTr.prop('outerHTML'));
    });
</script>
</body>
</html>
