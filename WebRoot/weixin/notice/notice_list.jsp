<%@page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXMenuMgr" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="cn.js.fan.web.Global" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>通知公告</title>
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
<%--<header id="barTop" class="mui-bar mui-bar-nav">
    <h1 class="mui-title"><%=Global.AppName%></h1>
</header>--%>
<div class="mui-content">
    <div class="mui-input-row mui-search" style="margin: 10px;">
        <input type="search" class="mui-input-clear" placeholder="请输入标题">
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
<script type="text/javascript" src="../js/mui.min.js"></script>
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
    var options = {"ajax_params": params, "url": "../../public/android/notice/getlist", "ajaxDatasType": "notices"};
    var content = document.querySelector('.mui-content');
    var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
    PullToRefrshListApi.loadListDate();

    $(".mui-content").on('tap', '.op-del', function (event) {
        var elem = this;
        var li = elem.parentNode.parentNode;
        var id = li.getAttribute("id");
        var btnArray = ['确认', '取消'];
        mui.confirm('您确定要删除么？', '提示', btnArray, function (e) {
            if (e.index == 0) {
                mui.get("../../public/notice/del.do", {"skey": skey, "id": id}, function (data) {
                    var ret = data.ret;
                    var msg = data.msg;
                    if (ret == "1") {
                        mui.toast("删除成功!");
                        li.parentNode.removeChild(li);
                    } else {
                        mui.toast(msg);
                    }
                }, "json");
            }
        });
    });

    $(".mui-content").on('tap', '.op-edit', function (event) {
        var elem = this;
        var li = elem.parentNode.parentNode;
        var id = li.getAttribute("id");
        window.location.href = "notice_edit.jsp?id=" + id + "&skey=" + skey;
    });
</script>

<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>" />
    <jsp:param name="isBarBtnAddShow" value="true" />
    <jsp:param name="barBtnAddUrl" value="notice_add.jsp" />
</jsp:include>

</body>
<script>
    function callJS() {
        return {"btnAddShow": 1, "btnAddUrl": "weixin/notice/notice_add.jsp", "btnBackUrl": "main"};
    }
    var iosCallJS = '{ "btnAddShow":1, "btnAddUrl":"weixin/notice/notice_add.jsp", "btnBackUrl":"main" }';
</script>
</html>
