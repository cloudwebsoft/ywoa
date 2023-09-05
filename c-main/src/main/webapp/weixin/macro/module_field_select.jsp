<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.flow.macroctl.*" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="org.json.JSONObject" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.api.IModuleFieldSelectCtl" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>表单域选择</title>
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
</style>
<body>
<%
    String desc = ParamUtil.get(request,"desc");
    if ("".equals(desc)) {
        return;
    }
    String skey = ParamUtil.get(request,"skey");
%>
<header class="mui-bar mui-bar-nav">
    <h1 class="mui-title">请选择</h1>
    <a id='done' class="mui-btn mui-btn-link mui-pull-right mui-btn-blue ">完成</a>
</header>
<div id="pullrefresh" class="mui-scroll-wrapper" style="margin-top: 90px;">
    <div class="mui-scroll">
        <form class="search_form mui-input-group" id="search_content">

        </form>
        <!--数据列表-->
        <ul class="mui-table-view mui-table-view-chevron" id="form_field_ul">

        </ul>
    </div>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/macro/open_window_macro.js"></script>
<script type="text/javascript" src="../js/mui.js"></script>
<script src="../js/mui.picker.min.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
<script type="text/javascript" src="../js/config.js"></script>
<script src="../js/mui.module_list_sel.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script src="../../js/jquery.raty.min.js"></script>
<script>
    mui('.mui-scroll-wrapper').scroll({
        bounce: true, // 是否启用回弹
        indicators: true, //是否显示滚动条
        deceleration: 0.0006 //阻尼系数,系数越小滑动越灵敏，默认0.0006
    });

    <%
            MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
            IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
            desc = moduleFieldSelectCtl.formatJSONString(desc);
            // System.out.println(getClass() + " desc=" + desc);

            JSONObject obj = new JSONObject(desc);
            String formCode = "", idField = "", showField = "", openerFormCode = "";
            if (obj.has("sourceFormCode")) {
                formCode = obj.getString("sourceFormCode");
            }
            if (obj.has("idField")) {
                idField = obj.getString("idField");
            }
            if (obj.has("showField")) {
                showField = obj.getString("showField");
            }
            if (obj.has("formCode")) {
                openerFormCode = obj.getString("formCode");
            }
            String openerFieldName = ParamUtil.get(request,"openerFieldName");
            String parentFields = ParamUtil.get(request,"parentFields");
            int isWx = ParamUtil.getInt(request,"isWx",0);
        %>
    var skey = '<%=skey%>';
    var params = {
        "skey": "<%=skey%>",
        // "op":"search",
        "action": "",
        "formCode": "<%=formCode%>",
        "orderBy": "<%=idField%>",
        "sort": "desc",
        "filter": "",
        "byFieldName": "<%=idField%>",
        "showFieldName": "<%=showField%>",
        "openerFormCode": "<%=openerFormCode%>",
        "openerFieldName": "<%=openerFieldName%>",
        "action": "afterGetClientValue"
    };

    console.log('openerFieldName', '<%=openerFieldName%>');

    var parentFields = '<%=parentFields%>';
    if (parentFields != "") {
        var par = JSON.parse(parentFields);
        for (var o in par) {
            params[o] = par[o];
        }
    }
    var options = {
        "isWx":<%=isWx%>,
        "ulContainer": "#form_field_ul",
        "ajax_params": params,
        "url": AJAX_REQUEST_URL.MODULE_LIST_SEL,
        "ajaxDatasType": LIST_TYPE.FORM_FIELD
    };
    // console.log(params);
    var content = document.querySelector('.mui-content');
    var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
    PullToRefrshListApi.loadListDate();
</script>
</body>
</html>
