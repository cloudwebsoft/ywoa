<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>登录</title>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <style type="text/css">
        <!--
        body, td, th {
            font-size: 14px;
        }

        * {
            margin: 0;
            padding: 0;
        }

        .login_con {
            list-style: none;
            width: 240px;
            margin: 0 auto;
        }

        .login_con li {
            padding: 8px 0;
            overflow: hidden;
            zoom: -1
        }

        .login_con li label {
            float: left;
        }

        .login_con li .input {
            border: 1px solid #CCCCCC;
            color: #868686;
            font-size: 16px;
            outline: medium none;
            padding: 2px;
            width: 170px;
        }

        .login_con li span {
            display: inline-block;
            width: 60px;
            float: left;
            font-family: "微软雅黑", "黑体", "宋体", "幼圆";
            color: #333
        }

        .verify_text, .pic {
            font-size: 12px;
            padding-left: 60px;
            color: #333;
        }

        .pic a {
            color: #069;
        }

        .logins {
            margin-top: 15px;
            text-align: center;
        }

        .signin-btn {
            background: url("theme/default/images/login_btn.png") no-repeat scroll -111px 0 transparent;
            border: 0 none;
            color: #0C4E7C;
            cursor: pointer;
            height: 36px;
            line-height: 20px;
            /*margin-left: 14px;*/
            text-align: center;
            width: 111px;
        }

        -->
    </style>
</head>

<body>
<form id="loginform" method="post" name="loginform">
    <ul class="login_con">
        <li><span>账号:</span>
            <label>
                <input name="name" type="text" class="input" id="name" onkeydown="name_presskey()"/>
            </label>
        </li>
        <li><span>密码:</span> <label>
            <input type="password" name="pwd" id="pwd" autocomplete="off" class="input" onkeydown="pwd_presskey()"/>
        </label></li>
        <li>
            <div id="errMsg" style="color:red"></div>
        </li>
        <div class="logins">
            <input type="button" id="login_button" tabindex="6" value="" class="signin-btn">
        </div>
        </li>
    </ul>
</form>
</body>
<script>
    function name_presskey() {
        if (window.event.keyCode == 13) {
            window.event.keyCode = 9;
        }
    }

    function pwd_presskey() {
        if (window.event.keyCode == 13) {
            submitLogin();
        }
    }

    $("#login_button").click(function () {
        submitLogin();
    });

    function submitLogin() {
        $.ajax({
            type: "post",
            url: "../doLogin.do",
            data: {
                name: $('#name').val(),
                pwd: $('#pwd').val()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                // $('#sortable').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "0") {
                    $('#errMsg').html(data.msg);
                } else {
                    window.parent.location.reload();
                }
            },
            complete: function (XMLHttpRequest, status) {
                // $('#sortable').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    $(function () {
        <%
        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        if (scfg.isRememberUserName()) {
        %>
        o("name").value = unescape(get_cookie("name"));
        if (o("name").value == "")
            o("name").focus();
        else
            o("pwd").focus();
        <%}%>
    });
</script>
</html>
