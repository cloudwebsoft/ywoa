<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    // 通过uniapp的webview载入
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);

    JSONArray params = new JSONArray();
    Enumeration reqParamNames = request.getParameterNames();
    while (reqParamNames.hasMoreElements()) {
        String paramName = (String) reqParamNames.nextElement();
        if (!"isUniWebview".equals(paramName) && !"skey".equals(paramName)) {
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = ParamUtil.getParam(request, paramName);
                JSONObject json = new JSONObject();
                json.put("name", paramName);
                json.put("value", paramValue);
                params.put(json);
            }
        }
    }
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>待办流程</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
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

        .mui-pull-top-pocket {
            top: 180px !important;
        }
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">待办流程</h1>
</header>
<div class="mui-content">
    <div class="mui-input-row mui-search" style="margin: 10px;">
        <input type="search" class="mui-input-clear" placeholder="请输入标题或流程号" id="search">
    </div>
    <div id="pullrefresh" class="mui-scroll-wrapper"
         style="margin-top: 60px;">
        <div class="mui-scroll">
            <!--数据列表-->
            <ul class="mui-table-view mui-table-view-striped mui-table-view-condensed">
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
    function getContextPath() {
        var strFullPath = document.location.href;
        var strPath = document.location.pathname;
        var pos = strFullPath.indexOf(strPath);
        var prePath = strFullPath.substring(0, pos);
        var postPath = strPath.substring(0, strPath.substr(1).indexOf('/') + 1);
        return (prePath + postPath);
    }

    // 将路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
    var url = "../../public/android/flow/doingorreturn?";
    if(mui.os.plus && mui.os.ios) {
        // url = getContextPath() + "/public/android/flow/doingorreturn?";

        var rootPath = getContextPath();
        var p = rootPath.indexOf('/weixin');
        if ( p != -1) {
            rootPath = rootPath.substring(0, p);
        }
        url = rootPath + "/public/android/flow/doingorreturn?";
    }

    var isUniWebview = <%=isUniWebview%>;
    var skey = '<%=skey%>';
    var options = {
        "ajax_params": {"skey": skey},
        "url": url,
        "ajaxDatasType": "flows",
        "isUniWebview": isUniWebview,
        "params": <%=params.toString()%>
    };
    var content = document.querySelector('.mui-content');
    /*var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
    PullToRefrshListApi.loadListDate();*/

    if(mui.os.plus) {
        // 如果是通过uniapp的webview载入
        if (isUniWebview) {
            $('.mui-bar').remove();
        }
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

    var appProp = {"btnAddShow": 0, "btnBackUrl": "weixin/flow/flow_main.jsp"};

    function callJS() {
        return appProp;
    }

    var iosCallJS = JSON.stringify(appProp);

    // 当5+app中流程处理完毕返回此页面时执行刷新，注意不能用refresh字符串，否则页面会不停地刷新
    window.addEventListener('refreshList', function(e){
        location.reload();
    });
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
