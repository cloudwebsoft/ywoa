<%@ page language="java" import="com.redmoon.oa.android.Privilege" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.dingding.service.auth.AuthService" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>流程</title>
    <script type="text/javascript">
        var _config = <%= AuthService.getConfig(request) %>;
    </script>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../../weixin/css/mui.css">
    <link rel="stylesheet" href="../../weixin/css/my_dialog.css"/>
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
    <script type="text/javascript" src="../../weixin/js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="https://g.alicdn.com/dingding/dingtalk-pc-api/2.3.1/index.js"></script>
    <script type="text/javascript" src="../js/myDingTalkPC.js"></script>
</head>
<body>
<div class="mui-content">

    <!--数据列表-->
    <ul
            class="mui-table-view mui-table-view-striped mui-table-view-condensed">
        <li class="mui-table-view-cell">
            <a class="mui-navigate-right" href="../../weixin/flow/flow_doing_or_return.jsp">
                <div class="mui-table">
                    <div class="mui-table-cell div-col-xs mui-left">
                        <img class="mui-pull-left img-center" align="center"
                             src="../../weixin/images/flow_wait.png">
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
            <a class="mui-navigate-right" href="../../weixin/flow/flow_attend.jsp">
                <div class="mui-table">
                    <div class="mui-table-cell div-col-xs mui-left">
                        <img class="mui-pull-left img-center" align="center"
                             src="../../weixin/images/flow.png">
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
            <a href="../../weixin/flow/flow_initiate.jsp" class="mui-navigate-right">
                <div class="mui-table">
                    <div class="mui-table-cell div-col-xs mui-left">
                        <img class="mui-pull-left img-center" align="center"
                             src="../../weixin/images/start_flow.png">
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
</body>
<script>


</script>

</html>
