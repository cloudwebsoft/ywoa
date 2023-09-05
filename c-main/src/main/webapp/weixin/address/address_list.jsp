<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.address.AddressDb" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);

    int type = ParamUtil.getInt(request, "type", AddressDb.TYPE_PUBLIC);
    String title;
    if (type == AddressDb.TYPE_USER) {
        title = "我的通讯录";
    }
    else {
        title = "公共通讯录";
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title><%=title%></title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css"/>
    <link href="../css/mui.indexedlist.css" rel="stylesheet"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>
</head>
<style>
    html, body {
        height: 100%;
    }

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

    .div_opinion {
        text-align: left;
    }

    .opinionContent {
        margin: 10px;
        width: 65%;
        float: right;
        font-weight: normal;
    }

    .opinionContent div {
        text-align: right;
    }

    .opinionContent div span {
        padding: 10px;
    }

    .opinionContent .content_h5 {
        color: #000;
        font-size: 17px;
    }
</style>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title"><%=title%></h1>
</header>
<div class="mui-content">
    <div id='list' class="mui-indexed-list">
        <div class="mui-indexed-list-search mui-input-row mui-search">
            <input type="search"
                   class="mui-input-clear mui-indexed-list-search-input"
                   placeholder="搜索"/>
        </div>
        <div class="mui-indexed-list-bar">
            <a>A</a>
            <a>B</a>
            <a>C</a>
            <a>D</a>
            <a>E</a>
            <a>F</a>
            <a>G</a>
            <a>H</a>
            <a>I</a>
            <a>J</a>
            <a>K</a>
            <a>L</a>
            <a>M</a>
            <a>N</a>
            <a>O</a>
            <a>P</a>
            <a>Q</a>
            <a>R</a>
            <a>S</a>
            <a>T</a>
            <a>U</a>
            <a>V</a>
            <a>W</a>
            <a>X</a>
            <a>Y</a>
            <a>Z</a>
        </div>
        <div class="mui-indexed-list-alert"></div>
        <div class="mui-indexed-list-inner">
            <div class="mui-indexed-list-empty-alert">
                没有数据
            </div>
            <ul class="mui-table-view">
            </ul>
        </div>
    </div>
</div>

<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script type="text/javascript" src="../js/newPopup.js"></script>
<script src="../js/macro/macro.js"></script>
<script src="../js/macro/open_window_macro.js"></script>
<script src="../js/mui.js"></script>
<script src="../js/mui.indexedlist.js"></script>
<script type="text/javascript" src="../js/mui.address.wx.js"></script>
<script type="text/javascript" charset="utf-8">
    var isUniWebview = <%=isUniWebview%>;

    if(!mui.os.plus || isUniWebview) {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();
    }

    mui.init({
        keyEventBind: {
            backbutton: !isUniWebview //关闭back按键监听
        }
    });

    mui.ready(function () {
        var list = document.getElementById('list');
        list.style.height = document.body.offsetHeight + 'px';
        var op = {"skey": "<%=skey%>", "type": <%=type%>, "isUniWebview": isUniWebview};
        window.addr = new mui.Address(list, op);
        window.addr.addrInit();
    })

    function callJS() {
        return {"btnAddShow": 0, "btnAddUrl": "", "btnBackUrl": ""};
    }

    var iosCallJS = '{ "btnAddShow":0, "btnAddUrl":"", "btnBackUrl":"" }';
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
