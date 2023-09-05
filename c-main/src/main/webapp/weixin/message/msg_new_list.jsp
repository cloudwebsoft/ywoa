<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="com.redmoon.weixin.Config" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("登录错误"));
        return;
    }
    String skey = pvg.getSkey();
    String url = request.getContextPath() + "/public/message/listNewMsg.do";
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>消息</title>
    <link rel="shortcut icon" href="../../images/favicon.ico"/>
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

        /*标题前图标的大小*/
        .mui-table-view .mui-media-object {
            width: 24px;
            height: 24px;
        }
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">消息</h1>
</header>
<div class="mui-content">
    <div class="mui-input-row mui-search" style="margin: 10px;">
        <input type="search" class="mui-input-clear" placeholder="请输入标题">
    </div>
    <div id="pullrefresh" class="mui-scroll-wrapper"
         style="margin-top: 60px;">
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
    var url = '<%=url%>';
    var isUniWebview = <%=isUniWebview%>;
    var params = {"skey": "<%=skey%>"};
    var options = {"ajax_params": params, "url": url, "ajaxDatasType": "messages", "isUniWebview": isUniWebview};
    var content = document.querySelector('.mui-content');

    if(mui.os.plus) {
        // 使搜索区域下方空白不致过大，因为搜索框中的input的margin-bottom为15px，而在原生手机端中则不会有此margin-bottom
        $('#pullrefresh').css('margin-top', '-15px');
        document.addEventListener('plusready', function() {
            var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
            PullToRefrshListApi.loadListDate();
        });

        // 注册beforeback方法，以使得在流程处理完后退至待办列表页面时能刷新页面
        if (isUniWebview) {
            $('.mui-bar').remove();
        }
    }
    else {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();

        var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
        PullToRefrshListApi.loadListDate();
    }

    function callJS() {
        return {"btnAddShow":0, "btnBackShow": 0, "btnBackUrl": ""};
    }

    var iosCallJS = '{ "btnAddShow":0, "btnBackShow":0, "btnBackUrl":"" }';
</script>

<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="tabId" value="msg"/>
    <jsp:param name="isUniWebview" value="<%=isUniWebview%>"/>
</jsp:include>
</body>

</html>
