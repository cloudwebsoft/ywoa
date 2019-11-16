<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="com.redmoon.weixin.Config" %>
<%
    String code = ParamUtil.get(request, "code");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>签名</title>
    <meta name="viewport" content="width=device-width,initial-scale=1, maximum-scale=1"/>
    <style type="text/css">
        * {
            margin: 0;
            padding: 0;
        }

        .content {
            width: 100%;
            overflow: hidden;
            position: relative;
            background-color: #ffffee;
        }

        .signWraper {
            height: 100%;
            width: 100%;
            overflow: hidden;
            display: flex;
            position: absolute;
            left: 100%;
            z-index: 2;
        }

        .signWraper .handlerDiv {
            flex: 1;
            height: 100%;
            display: flex;
            justify-content: space-around;
            align-items: center;
            flex-direction: column;
            background: #fff;
        }

        .signWraper .handlerDiv button {
            transform: rotate(90deg);
            -webkit-transform: rotate(90deg);
            -moz-transform: rotate(90deg);
            -o-transform: rotate(90deg);
            -ms-transform: rotate(90deg);
            background: -moz-linear-gradient(left, #f9E29C, #E7BD75);
            background: -webkit-linear-gradient(left, #f9E29C, #E7BD75);
            background: -o-linear-gradient(left, #f9E29C, #E7BD75);
            width: 100%;
            height: 35px;
            border: 0;
            border-radius: 5px;
        }

        .row-btn button {
            background: -moz-linear-gradient(left, #f9E29C, #E7BD75);
            background: -webkit-linear-gradient(left, #f9E29C, #E7BD75);
            background: -o-linear-gradient(left, #f9E29C, #E7BD75);
            width: 100%;
            height: 35px;
            border: 0;
            border-radius: 5px;
        }

        .signImg {
            width: 180px;
            display: none;
        }

        .signatureparent {
            color: darkblue;
            background-color: darkgrey;
            width: 75%;
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .signatureparent > div {
            border: 2px dotted black;
            background-color: lightgrey;
            height: 90%;
            width: 80%;
        }
    </style>
</head>
<body>
<div class="content">
    <div class="signWraper">
        <div class="handlerDiv">
            <button id="reset" type="button" onclick="reset()">重置签名</button>
            <button id="getSign" type="button" onclick="getSign()">生成签名</button>
        </div>
        <div class="signatureparent">
            <div id="signature"></div>
        </div>
    </div>
    <div class="row-btn">
        <button id="goSign" type="button" style="width:100px;height:40px;margin:20px" onclick="goSign()">手写签名</button>
        <button id="applySign" type="button" style="width:100px;height:40px;margin:20px" onclick="applySign('<%=code%>')">应用签名</button>
    </div>
    <div>
        <div style="margin-left: 20px">签名图片预览：</div>
        <div style="text-align:center;">
            <img id="signImg" class="signImg" src="">
        </div>
    </div>
</div>
<script src="../../js/jquery-1.9.1.min.js"></script>
<script src="jSignature.js"></script>
<script src="writepad.js"></script>
<script type="text/javascript" src="../../js/mui.min.js"></script>
<script src="../../js/macro/open_window_macro.js"></script>
<script>
    $(function() {
        goSign();
    })
</script>
</body>
</html>
