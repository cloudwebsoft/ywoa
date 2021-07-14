<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>流程</title>
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
            line-height: 30px;
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
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">流程</h1>
</header>
<div class="mui-content">

    <!--数据列表-->
    <ul
            class="mui-table-view mui-table-view-striped mui-table-view-condensed">
        <li class="mui-table-view-cell">
            <a class="mui-navigate-right" href="flow_doing_or_return.jsp?skey=<%=skey %>">
                <div class="mui-table">
                    <div class="mui-table-cell div-col-xs mui-left">
                        <img class="mui-pull-left img-center" align="center"
                             src="../images/flow_wait.png">
                    </div>
                    <div class="mui-table-cell mui-col-xs-11">
                        <h4 class="mui-ellipsis">
                            待办流程
                        </h4>
                    </div>
                </div>
            </a>
        </li>
        <li class="mui-table-view-cell">
            <a class="mui-navigate-right" href="flow_attend.jsp?skey=<%=skey %>">
                <div class="mui-table">
                    <div class="mui-table-cell div-col-xs mui-left">
                        <img class="mui-pull-left img-center" align="center"
                             src="../images/flow.png">
                    </div>
                    <div class="mui-table-cell mui-col-xs-11">
                        <h4 class="mui-ellipsis">
                            我的流程
                        </h4>
                    </div>
                </div>
            </a>
        </li>
        <li class="mui-table-view-cell">
            <a href="flow_initiate.jsp?skey=<%=skey %>" class="mui-navigate-right">
                <div class="mui-table">
                    <div class="mui-table-cell div-col-xs mui-left">
                        <img class="mui-pull-left img-center" align="center"
                             src="../images/start_flow.png">
                    </div>
                    <div class="mui-table-cell mui-col-xs-11">
                        <h4 class="mui-ellipsis">
                            发起流程
                        </h4>
                    </div>
                </div>
            </a>
        </li>
    </ul>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/mui.js"></script>
<script>
    if(!mui.os.plus) {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();
    }

    var appProp = {"btnAddShow": 0, "btnBackUrl": ""};
    function callJS() {
        return appProp;
    }
    var iosCallJS = JSON.stringify(appProp);
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
