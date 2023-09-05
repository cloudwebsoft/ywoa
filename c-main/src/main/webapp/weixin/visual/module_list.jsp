<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="org.json.JSONObject" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%@page import="com.redmoon.oa.visual.*" %>
<%
    String moduleCode = ParamUtil.get(request, "moduleCode");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(moduleCode);
    if (msd == null) {
        out.print("模块不存在！");
        return;
    }
    // 通过uniapp的webview载入
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title><%=msd.getString("name")%></title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css"/>
</head>
<style>
    .mui-input-row .input-icon {
        width: 50%;
        float: left;
    }

    .mui-input-row a {
        margin-right: 10px;
        float: right;
        text-align: left;
        line-height: 1.5;
    }

    .search_div {
        height: 40px;
        border-bottom: 1px solid gainsboro;
        margin-left: 15px;
        vertical-align: middle;
    }

    .search_div select {
        color: #007aff;
        height: 37px;
    }

    .search_div label {
        color: #aaa;
    }

    #op_add {
        z-index: 9999;
    }
</style>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <a class="mui-icon mui-pull-right mui-a-color"></a>
    <h1 class="mui-title"><%=msd.getString("name")%></h1>
</header>
<div class="mui-content">
    <div class="mui-content-padded">
    </div>
    <div class="mui-scroll-wrapper">
        <div id="pullrefresh" class="mui-scroll">
            <form class="search_form mui-input-group" id="search_content">

            </form>
            <!--数据列表-->
            <ul class="mui-table-view mui-table-view-chevron">

            </ul>
        </div>
    </div>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/macro/open_window_macro.js"></script>
<script type="text/javascript" src="../js/mui.js"></script>
<script src="../js/mui.picker.min.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
<script type="text/javascript" src="../js/config.js"></script>
<script src="../js/visual/module_list.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script src="../../js/jquery.raty.min.js"></script>
<script>
    function getContextPath() {
        var strFullPath = document.location.href;
        var strPath = document.location.pathname;
        var pos = strFullPath.indexOf(strPath);
        var prePath = strFullPath.substring(0, pos);
        var postPath = strPath.substring(0, strPath.substr(1).indexOf('/') + 1);
        return (prePath + postPath);
    }

    <%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    %>
    var skey = '<%=skey%>';
    var moduleCode = '<%=moduleCode%>';

    // 将路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
    var url = "../../public/android/module/list";
    if(mui.os.plus && mui.os.ios) {
        url = getContextPath() + "/public/android/module/list";
    }

    var options = {"ajax_params": {"skey": skey, "moduleCode": moduleCode}, "url": url};
    var content = document.querySelector('.mui-content');

    if(!mui.os.plus) {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        jQuery('.mui-bar').remove();
        mui.init();
    }
    else {
        // 如果是通过uniapp的webview载入
        if (isUniWebview) {
            $('.mui-bar').remove();
        }

        // 注册beforeback方法，以使得在流程处理完后退至待办列表页面时能刷新页面
        if (isUniWebview) {
            mui.init({
                keyEventBind: {
                    backbutton: false // 关闭back按键监听
                }
            });
        }
    }

    (function($) {
        $.ready(function() {
            var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
            PullToRefrshListApi.loadListData();
        });
    })(mui);

    // 当5+app中添加完毕返回此页面时执行刷新
    window.addEventListener('refreshList', function(e){
        location.reload();
    });
</script>

<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
