<%@page language="java" contentType="text/html;charset=utf-8" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
    UserDb user = new UserDb();
    user = user.getUserDb(userName);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>我</title>
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <link rel="stylesheet" href="css/mui.css">
    <style>
        .photo {
            max-width: 80px;
            max-height: 80px;
            vertical-align: middle;
        }
    </style>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="js/mui.min.js"></script>
</head>
<body>
<div class="mui-content">
    <div class="mui-table mui-table-view" style="line-height: 1.0;">
        <form class="mui-input-group" id="form1">
            <div class="mui-table-view-cell mui-title"><span>修改密码</span>
            </div>
            <div class="mui-input-row" id="row_realname" data-code="realname" data-isnull="false"><label>
                <span>旧密码</span><span style="color:red;">*</span></label>
                <input type="password" name="oldPassword" id="oldPassword" value="" class="mui-input-clear" placeholder="请输入旧密码">
            </div>
            <div class="mui-input-row" ><label><span>新密码</span></label>
                <input type="password" name="password" id="password" value="" class="mui-input-clear" placeholder="请输入密码"></div>
            <div class="mui-input-row"><label><span>确认密码</span></label>
                <input type="password" name="pwdConfirm" id="pwdConfirm" value="" class="mui-input-clear" placeholder="请输入确认密码"></div>
            <div class="mui-button-row">
                <button type="button" class="mui-btn mui-btn-primary mui-btn-outlined form_submit">提交</button>
            </div>
        </form>
    </div>

    <jsp:include page="inc/navbar.jsp">
        <jsp:param name="skey" value="<%=skey%>"/>
        <jsp:param name="tabId" value="me"/>
    </jsp:include>
</body>
<script>
    function logout() {
        var expdate = new Date();
        expdate.setTime(-1000);

        document.cookie = "isMyLoginAuto=;expires=" + expdate.toGMTString() + ";path=/";
        document.cookie = "pwd=;expires=" + expdate.toGMTString() + ";path=/";

        window.location.href = "../logout?redirectUrl=wap/index.jsp";
    }

    $('.mui-btn').click(function() {
        if ($('#oldPassword').val()=='') {
            mui.toast('请输入旧密码！');
            return;
        }
        if ($('#password').val()=='') {
            mui.toast('请输入新密码！');
            return;
        }
        if ($('#password').val()!=$('#pwdConfirm').val()) {
            mui.toast('密码与确认密码不一致！');
            return;
        }
        var btnArray = ['确认', '取消'];
        mui.confirm('您确定要修改么？', '提示', btnArray, function(e) {
            if(e.index == 0){
                mui.post("../public/android/i/resetPassword", {
                            "skey": "<%=skey%>",
                            "wap": true,
                            "password": $('#password').val(),
                            "oldPassword": $('#oldPassword').val()
                        }, function (data) {
                            var res = data.res;
                            var returnCode = data.result.returnCode;
                            if (res == "0") {
                                if (returnCode == 0) {
                                    mui.toast("修改成功!");
                                    window.location.href = "me.jsp";
                                }
                                else if (returnCode == 1) {
                                    mui.toast("旧密码输入不正确");
                                }
                                else {
                                    mui.toast("操作失败");
                                }
                            } else {
                                mui.toast("操作失败：" + returnCode);
                            }
                        }, "json");
            }
        });
    })
</script>
</html>
