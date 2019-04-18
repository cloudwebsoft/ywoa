<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>待办流程</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <style>
        .mui-table h4, .mui-table h5, .mui-table .mui-h5, .mui-table .mui-h6, .mui-table p {
            margin-top: 0;
        }

        .mui-table h4 {
            line-height: 21px;
            font-weight: 500;
        }

        .mui-table .oa-icon {
            position: absolute;
            right: 0;
            bottom: 0;
        }

        .mui-table .oa-icon-star-filled {
            color: #f14e41;
        }

        .div-col-xs {
            width: 42px;
            text-align: center;
            vertical-align: middle;

        }

    </style>
</head>
<body>
<div class="mui-content">
    <div class="mui-input-row mui-search" style="margin: 10px;">
        <input type="search" class="mui-input-clear" placeholder="请输入标题"
               id="search">
    </div>
    <div id="pullrefresh" class="mui-scroll-wrapper"
         style="margin-top: 60px;">
        <div class="mui-scroll">
            <!--数据列表-->
            <ul
                    class="mui-table-view mui-table-view-striped mui-table-view-condensed">
            </ul>
        </div>
    </div>
</div>

<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/mui.min.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript"
        src="../js/mui.pullToRefresh.material.js"></script>
<script type="text/javascript" src="../js/mui.PullToRefresh.wx.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script>
    <%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    %>
    var skey = '<%=skey%>';
    var options = {
        "ajax_params": {"skey": skey},
        "url": "../../public/android/flow/doingorreturn?",
        "ajaxDatasType": "flows"
    };
    var content = document.querySelector('.mui-content');
    var PullToRefrshListApi = new mui.PullToRefrshList(
            content, options);
    PullToRefrshListApi.loadListDate();

    function callJS() {
        return {"btnAddShow": 0, "btnBackUrl": "weixin/flow/flow_main.jsp"};
    }
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
