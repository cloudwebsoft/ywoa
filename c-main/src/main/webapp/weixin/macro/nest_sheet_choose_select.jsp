<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.oa.visual.*" %>
<%@page import="com.redmoon.oa.flow.WorkflowDb" %>
<%@page import="com.redmoon.oa.flow.Leaf" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%@page import="nl.bitwalker.useragentutils.*" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>嵌套表格-拉单</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css"
          href="../css/mui.picker.min.css"/>

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
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">嵌套表格-选择</h1>
    <a id='done' class="mui-btn mui-btn-link mui-pull-right mui-btn-blue ">完成</a>


</header>

<div id="pullrefresh" class="mui-scroll-wrapper" style="margin-top: 90px;">

    <div class="mui-scroll">
        <form class="search_form mui-input-group" id="search_content">

        </form>
        <!--数据列表-->
        <ul class="mui-table-view mui-table-view-chevron" id="ul_nest_sheet_choose_select">

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
<script>
    <%
    String skey = ParamUtil.get(request,"skey");
    String fieldCode = ParamUtil.get(request,"code");
    int flowId = ParamUtil.getInt(request,"flowId", -1);
   String actionId = ParamUtil.get(request,"actionId");
   String dFormCode = ParamUtil.get(request,"dFormCode");
   String sFormCode = ParamUtil.get(request,"sFormCode");
   String parentCode = StrUtil.getNullStr(ParamUtil.get(request,"parentCode"));
   String parentId = ParamUtil.get(request,"parentId");

   String parentFields = "";
   boolean isMobile = false;
   UserAgent ua = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
   OperatingSystem os = ua.getOperatingSystem();
   if (DeviceType.MOBILE.equals(os.getDeviceType())) {
       isMobile = true;
   }
   if (isMobile) {
       parentFields = StrUtil.getNullStr(request.getParameter("parentFields"));
       parentFields = new String(parentFields.getBytes("ISO-8859-1"), "UTF-8");
   }
   else {
       parentFields = ParamUtil.get(request, "parentFields");
   }

   String parentModuleCode = ParamUtil.get(request, "parentModuleCode");

   if(parentCode.equals("")){
       if (flowId!=-1) {
           WorkflowDb wf = new WorkflowDb();
           wf = wf.getWorkflowDb(flowId);
           Leaf lf = new Leaf();
           lf = lf.getLeaf(wf.getTypeCode());
           parentCode = lf.getFormCode();
       }
       else {
           if (!"".equals(parentModuleCode)) {
               ModuleSetupDb pMsd = new ModuleSetupDb();
               pMsd = pMsd.getModuleSetupDb(parentModuleCode);
               parentCode = pMsd.getString("form_code");
           }
       }
   }
   int isWx = ParamUtil.getInt(request,"isWx",0);
   boolean isEditable = ParamUtil.getBoolean(request,"isEditable",false);
   String urlParams = "?skey="+skey+"&flowId=" + flowId + "&cwsId=" + parentId + "&parentModuleCode=" + parentModuleCode + "&code="+fieldCode+"&flowId="+flowId+"&actionId="+actionId+"&dFormCode="+dFormCode+"&sFormCode="+sFormCode+"&parentCode="+parentCode+"&isEditable="+isEditable+"&parentFields="+StrUtil.UrlEncode(parentFields);

%>
    var skey = '<%=skey%>';
    var parentFields = '<%=parentFields%>';

    var params = {
        "parentId": "<%=parentId%>",
        "formCode": "<%=sFormCode%>",
        "orderBy": "id",
        "sort": "desc",
        "parentFormCode": "<%=parentCode%>",
        "nestFieldName": "<%=fieldCode%>",
        "nestType": "",
        "action": "afterGetClientValue",
        "skey": "<%=skey%>",
        "flowId":<%=flowId%>
    };

    if (parentFields != "") {
        console.log("parentFields=" + parentFields);
        var par = JSON.parse(parentFields);
        for (var o in par) {
            params[o] = par[o];
            // console.log(o + "=" + params[o]);
        }
    }
    var options = {
        "isWx":<%=isWx%>,
        "ulContainer": "#ul_nest_sheet_choose_select",
        "ajax_params": params,
        "url": AJAX_REQUEST_URL.MODULE_LIST.NEST_SEL_LIST,
        "ajaxDatasType": LIST_TYPE.NEST_SHEET_CHOOSE_SELECT,
        "urlParams": '<%=urlParams%>'
    };

    // 使mui-scroll-wrapper可以滚动
    mui('.mui-scroll-wrapper').scroll({
        bounce: true, // 是否启用回弹
        indicators: true, //是否显示滚动条
        deceleration: 0.0006 //阻尼系数,系数越小滑动越灵敏，默认0.0006
    });

    var content = document.querySelector('#pullrefresh');
    var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
    PullToRefrshListApi.loadListDate();

    mui(".mui-bar").on("tap", ".mui-action-back", function () {
        if (options.isWx == 1) {
            nestSheetJump("请选择", "../macro/nest_sheet_select.jsp" + options.urlParams + "&isWx=1", "");
        } else {
            mui.back();
        }
    });
</script>
</body>
</html>
