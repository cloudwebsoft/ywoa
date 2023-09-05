<%@page language="java" import="com.redmoon.oa.android.Privilege" pageEncoding="utf-8" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
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

        .mui-pull-top-pocket {
            top: 180px !important;
        }

        .mui-icon-plus {
            touch-action: none;
        }

        .btn-add-wrap {
            position: fixed;
            margin-bottom: 20px;
            bottom: 0;
            display: flex;
            width: 100%;
            justify-content: center;
            z-index: 10000;
        }
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <a class="mui-icon mui-icon-plusempty mui-pull-right mui-a-color"></a>
    <h1 class="mui-title">通知公告</h1>
</header>
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
<div class="btn-add-wrap">
    <button id="btnAdd" class="btn-add">添加</button>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/mui.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
<script type="text/javascript" src="../js/mui.PullToRefresh.wx.js"></script>
<script src="../js/jq_mydialog.js"></script>

<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBtnAddShow" value="true"/>
    <jsp:param name="barBtnAddUrl" value="notice_add.jsp"/>
</jsp:include>
<script>
    var isUniWebview = <%=isUniWebview%>;
    var skey = '<%=skey%>';
    var params = {"skey": skey};
    // ios下url只能用<%=request.getContextPath()%>/public/notice/list，而android下可以用相对路径../../public/notice/list.do
    var options = {
        "ajax_params": params,
        "url": "<%=request.getContextPath()%>/public/notice/list.do",
        "ajaxDatasType": "notices",
        "isUniWebview": isUniWebview
    };
    var content = document.querySelector('.mui-content');

    // 用于HBuilderX手机端
    if (mui.os.plus) {
        // 使搜索区域下方空白不致过大，因为搜索框中的input的margin-bottom为15px，而在原生手机端中则不会有此margin-bottom
        $('#pullrefresh').css('margin-top', '-15px');
        document.addEventListener('plusready', function () {
            var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
            PullToRefrshListApi.loadListDate();
        });

        // 注册beforeback方法，以使得在流程处理完后退至待办列表页面时能刷新页面
        if (isUniWebview) {
            $('.mui-bar').remove();
        }
    } else {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();

        var PullToRefrshListApi = new mui.PullToRefrshList(content, options);
        PullToRefrshListApi.loadListDate();
    }

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
        window.location.href = "notice_edit.jsp?id=" + id + "&skey=" + skey + "&isUniWebview=<%=isUniWebview%>";
    });

    jQuery(function() {
        jQuery("#btnAdd").click(function() {
            window.location.href = "notice_add.jsp?skey=<%=skey%>&isUniWebview=<%=isUniWebview%>";
        })
    })

    // 在uniapp的webview中调用
    function add() {
        // 回退有问题，会回到九宫格
        // window.location.href = "notice_add.jsp?skey=<%=skey%>&isUniWebview=<%=isUniWebview%>";
        // 模拟点击回退仍有问题
        // $("#btnAdd").trigger("click");

        // pushState也会
        // var state = {
        //     title: document.title,
        //     url: window.location.href
        // };
        // window.history.pushState(state, "title", "#");

        // 如果用mui.openWindow，可能会引起webview混乱，退回时，直接回到了九宫格，偶尔会正常
        // 但是用window.location.href的时候，也有问题，退回时是直接到了九宫格页面
        /*mui.openWindow({
            "url": "notice_add.jsp?skey=<%=skey%>&isUniWebview=<%=isUniWebview%>",
            "id": "noticeAddWin",
            "styles": {
                // top: '80px'
                top: '43px'
            }
        });*/
    }

    if (!isUniWebview) {
        $('#btnAdd').hide();
    }

    function callJS() {
        return {"btnAddShow": 1, "btnAddUrl": "weixin/notice/notice_add.jsp", "btnBackUrl": "main"};
    }

    var iosCallJS = '{ "btnAddShow":1, "btnAddUrl":"weixin/notice/notice_add.jsp", "btnBackUrl":"main" }';

    if (mui.os.plus) {
        mui('.mui-bar').on("tap", '.mui-icon-plusempty', function (e) {
            e.preventDefault();
            // console.log('btnAddUrl:' + callJS().btnAddUrl);
            mui.openWindow({
                "url": "notice_add.jsp?skey=<%=skey%>",
                // "url": "<%=request.getContextPath()%>/" + callJS().btnAddUrl + "?skey=<%=skey%>",
                "id": "noticeAddWin"
            });
        });
    }
</script>
</body>
</html>
