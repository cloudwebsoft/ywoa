<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudweb.oa.service.LoginService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    LoginService loginService = SpringUtil.getBean(LoginService.class);
    String url = loginService.getUIModePage("");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>完善用户信息</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body>
<br/>
<br/>
<br/>
<form id="form1" method="post">
    <table width="70%" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
        <tr>
            <td colspan="2" align="center" class="tabStyle_1_title">请完善用户信息</td>
        </tr>
        <tr>
            <td width="50%" align="right">邮箱</td>
            <td align="left">
                <input id="email" name="email"/>
                <script>
                    var lvEmail = new LiveValidation('email');
                    lvEmail.add(Validate.Presence);
                    lvEmail.add(Validate.Email, {failureMessage: 'Email格式错误'});
                </script>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input type="button" class="btn" value="确定"/>
                <input name="action" value="" type="hidden"/>
            </td>
        </tr>
    </table>
</form>
</body>
<script>
    $('.btn').click(function () {
        if (!LiveValidation.massValidate(lvEmail.formObj.fields)) {
            return false;
        }
        setUserInfo();
    })

    function setUserInfo() {
        $.ajax({
            type: "post",
            url: "setUserInfo.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                email: $('#email').val()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 1) {
                    jAlert_Redirect('操作成功', '提示', '<%=request.getContextPath() + "/" + url%>');
                } else {
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