<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.cloudweb.oa.utils.SysUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>重置密码</title>
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
<%
    com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
    if (scfg.getIntProperty("isPwdCanReset") == 0) {
        out.print(SkinUtil.makeErrMsg(request, "禁止操作！"));
        return;
    }
    String action = ParamUtil.get(request, "action");
    com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
    action = cn.js.fan.security.ThreeDesUtil.decrypthexstr(ssoCfg.getKey(), action);
    String[] ary = StrUtil.split(action, "\\|");
    if (ary == null) {
        out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
        return;
    }
    int len = ary.length;
    Map map = new HashMap();
    for (int i = 0; i < len; i++) {
        String[] pair = ary[i].split("=");
        if (pair.length == 2) {
            map.put(pair[0], pair[1]);
        }
    }
    String timestamp = (String) map.get("timestamp");
    Date dt = DateUtil.parse(timestamp);
    if (dt == null) {
        out.print(SkinUtil.makeErrMsg(request, "时间戳非法！"));
        return;
    }
    dt = DateUtil.addMinuteDate(dt, 5); // 链接在5分钟有效
    if (dt.before(new Date())) {
        out.print(SkinUtil.makeErrMsg(request, "操作已超时，请重新重置密码！"));
        return;
    }

    String userName = (String) map.get("userName");

    SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
    String loginPath = sysUtil.getFrontPath() + "login";
%>
<form id="form1" action="?op=set" method="post">
    <table width="70%" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
        <tr>
            <td colspan="2" align="center" class="tabStyle_1_title">请输入新密码</td>
        </tr>
        <tr>
            <td align="right">密码</td>
            <td align="left"><input id="pwd" name="pwd" type="password" onkeyup="checkPwd()"/></td>
        </tr>
        <tr>
            <td width="50%" align="right">确认密码</td>
            <td align="left">
                <input id="pwdConfirm" name="pwdConfirm" type="password"/>
                <script>
                    var lvPwd = new LiveValidation('pwd');
                    lvPwd.add(Validate.Presence);
                    var lvPwdConfirm = new LiveValidation('pwdConfirm');
                    lvPwdConfirm.add(Validate.Presence);
                </script>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input id="btnOk" type="button" class="btn" value="确定"/>
                <input name="action" value="" type="hidden"/>
            </td>
        </tr>
    </table>
</form>
</body>
<script>
    $('#btnOk').click(function () {
        if (!LiveValidation.massValidate(lvPwd.formObj.fields)) {
            return false;
        }
        if ($('#pwd').val() != $('#pwdConfirm').val()) {
            jAlert('密码与确认密码必须一致！', '提示');
            return false;
        }
        resetPwd();
    })

    function resetPwd() {
        $.ajax({
            type: "post",
            url: "resetPwd.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                userName: '<%=userName%>',
                pwd: $('#pwd').val(),
                pwdConfirm: $('#pwdConfirm').val()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 1) {
                    jAlert("重置密码成功，请登录", "提示", function () {
                        // window.location.href = "../index";
                        window.location.href = "<%=loginPath%>";
                    });
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

    function checkPwd() {
        $.ajax({
            type: "post",
            url: "checkPwd.do",
            data: {
                pwd: $('#pwd').val()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 0) {
                    $('#pwd').parent().append("<span id='errMsgBox' class='LV_validation_message LV_invalid'>" + data.msg + "</span>");
                } else {
                    $('#errMsgBox').remove();
                }
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }
</script>
</html>