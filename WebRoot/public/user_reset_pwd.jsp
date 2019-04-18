<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String url = "";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>重置密码</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<br/>
<br/>
<br/>
<form id="form1" action="oa_email_setup.jsp?op=set" method="post">
    <table width="70%" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
        <tr>
            <td colspan="2" align="center" class="tabStyle_1_title">请输入用户信息</td>
        </tr>
        <tr>
          <td align="right">用户名</td>
          <td align="left"><input id="userName" name="userName"/></td>
        </tr>
        <tr>
            <td width="50%" align="right">邮箱</td>
            <td align="left">
                <input id="email" name="email"/>
                <script>
                    var lvUserName = new LiveValidation('userName');
                    lvUserName.add(Validate.Presence);
                    var lvEmail = new LiveValidation('email');
                    lvEmail.add(Validate.Presence);
                    lvEmail.add(Validate.Email, {failureMessage: '邮箱格式错误'});

                    $("#email").keydown(function (e) {
                        var e = e || event,
                            keycode = e.which || e.keyCode;
                        if (keycode == 13) {
                            setUserInfo();
                        }
                    });
                </script>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input id="btnOk" type="button" class="btn" value="确定"/>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="button" class="btn" value="返回" onclick="window.location.href='../index.jsp';"/>
                <input name="action" value="" type="hidden"/>
            </td>
        </tr>
    </table>
</form>
</body>
<script>
    $('#btnOk').click(function () {
        setUserInfo();
    })

    function setUserInfo() {
        if (!LiveValidation.massValidate(lvEmail.formObj.fields)) {
            return false;
        }
        $.ajax({
            type: "post",
            url: "resetPwdSendLink.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                userName: $('#userName').val(),
                email: $('#email').val()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret==1) {
                    jAlert("操作成功，请进入邮箱点击重置密码链接", "提示", function() {
                        window.location.href = "../index.jsp";
                    });
                }
                else {
                    jAlert(data.msg, "提示");
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }
</script>
</html>