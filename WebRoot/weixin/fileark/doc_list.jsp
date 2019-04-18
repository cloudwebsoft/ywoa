<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.raq.dm.Param" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();

    String dirCode = ParamUtil.get(request, "dirCode");
    String url = "../../fileark/list.do?dirCode=" + StrUtil.UrlEncode(dirCode);
%>
<html>
<head>
    <title>文件柜</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
</head>
<body>
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
<script type="text/javascript" src="../js/mui.min.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
<script type="text/javascript" src="../js/mui.PullToRefresh.wx.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script>
    var url = '<%=url%>';
    var params = {"skey": "<%=skey%>", "dirCode":"<%=dirCode%>"};
    var options = {"ajax_params": params, "url": url, "ajaxDatasType": "documents"};
    var content = document.querySelector('.mui-content');

    mui.init({
        pullRefresh: {
            up: {
                contentnomore: '' // 去掉“没有更多数据了”
            }
        }
    });

    var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
    PullToRefrshListApi.loadListDate();

    function callJS() {
        return {"btnBackShow": 0, "btnBackUrl": ""};
    }

    var iosCallJS = '{ "btnBackShow":0, "btnBackUrl":"" }';
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
