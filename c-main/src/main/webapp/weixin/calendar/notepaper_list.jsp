<%@page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXMenuMgr" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <meta charset="utf-8">
    <title>便笺</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <style type="text/css">
        .createdate {
            font-size: 12px;
            color: #8f8f94;
        }
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <a class="mui-icon mui-icon-plusempty mui-pull-right mui-a-color"></a>
    <h1 class="mui-title">便笺</h1>
</header>
<div class="mui-content">
    <div class="mui-input-row mui-search" style="margin: 10px;">
        <input type="search" id="title" name="title" class="mui-input-clear" placeholder="请输入标题">
    </div>
    <div id="pullrefresh" class="mui-scroll-wrapper" style="margin-top: 60px;">
        <div class="mui-scroll">
            <!--数据列表-->
            <ul class="mui-table-view mui-table-view-chevron">

            </ul>
        </div>
    </div>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/mui.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
<script type="text/javascript" src="../js/mui.PullToRefresh.wx.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script>
    <%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    %>
    var skey = '<%=skey%>';
    var params = {"skey": skey};
    var options = {"ajax_params": params, "url": "../../public/plan/getNotepapers.do", "ajaxDatasType": "notepapers"};
    var content = document.querySelector('.mui-content');
    // 用于HBuilderX手机端
    if(mui.os.plus) {
        // 使搜索区域下方空白不致过大，因为搜索框中的input的margin-bottom为15px，而在原生手机端中则不会有此margin-bottom
        $('#pullrefresh').css('margin-top', '-15px');
        document.addEventListener('plusready', function() {
            var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
            PullToRefrshListApi.loadListDate();
        });
    }
    else {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();

        var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
        PullToRefrshListApi.loadListDate();
    }

    function callJS() {
        return {"btnAddShow": 1, "btnAddUrl": "weixin/calendar/calendar_add.jsp?refer=notepaper", "btnBackUrl": ""};
    }

    var iosCallJS = '{ "btnAddShow":1, "btnAddUrl":"weixin/calendar/calendar_add.jsp?refer=notepaper", "btnBackUrl":"" }';

    if(mui.os.plus) {
        mui('.mui-bar').on("tap", '.mui-icon-plusempty', function (e) {
            mui.openWindow({
                "url": "calendar_add.jsp?refer=notepaper&skey=<%=skey%>",
                "id": "noticeAddWin"
            });
        });
    }
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBtnAddShow" value="true"/>
    <jsp:param name="barBtnAddUrl" value="calendar_add.jsp?refer=notepaper"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
